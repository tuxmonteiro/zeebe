/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.workflow.state;

import io.zeebe.logstreams.rocksdb.ZeebeStateConstants;
import io.zeebe.logstreams.state.StateController;
import io.zeebe.msgpack.spec.MsgPackReader;
import io.zeebe.msgpack.spec.MsgPackToken;
import io.zeebe.msgpack.spec.MsgPackWriter;
import io.zeebe.util.collection.Tuple;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.Int2IntHashMap;
import org.agrona.collections.Int2IntHashMap.EntryIterator;
import org.agrona.collections.ObjectHashSet;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

public class VariablesState {

  // column family offsets
  private static final int VARIABLES_SCOPE_KEY_OFFSET = 0;
  private static final int VARIABLES_NAME_OFFSET = BitUtil.SIZE_OF_LONG;

  private final MsgPackReader reader = new MsgPackReader();
  private final MsgPackWriter writer = new MsgPackWriter();
  private final ExpandableArrayBuffer documentResultBuffer = new ExpandableArrayBuffer();
  private final DirectBuffer resultView = new UnsafeBuffer(0, 0);

  private final StateController stateController;

  // (child scope key) => (parent scope key)
  private final ColumnFamilyHandle elementChildParentHandle;
  // (scope key, variable name) => (variable value)
  private final ColumnFamilyHandle variablesHandle;

  private final ExpandableArrayBuffer stateKeyBuffer = new ExpandableArrayBuffer();
  private final ExpandableArrayBuffer stateValueBuffer = new ExpandableArrayBuffer();

  private final LocalVariableIterator localVariableIterator = new LocalVariableIterator();

  // collecting variables
  private ObjectHashSet<DirectBuffer> collectedVariables = new ObjectHashSet<>();
  private ObjectHashSet<DirectBuffer> variablesToCollect = new ObjectHashSet<>();

  // setting variables
  private IndexedDocument indexedDocument = new IndexedDocument();

  public VariablesState(
      StateController stateController,
      ColumnFamilyHandle elementChildParentHandle,
      ColumnFamilyHandle variablesHandle) {
    this.stateController = stateController;
    this.elementChildParentHandle = elementChildParentHandle;
    this.variablesHandle = variablesHandle;
  }

  public void setVariablesLocalFromDocument(long scopeKey, DirectBuffer document) {
    reader.wrap(document, 0, document.capacity());

    final int variables = reader.readMapHeader();

    for (int i = 0; i < variables; i++) {
      final MsgPackToken variableName = reader.readToken();
      final int nameLength = variableName.getValueBuffer().capacity();
      final int nameOffset = reader.getOffset() - nameLength;

      final int valueOffset = reader.getOffset();
      reader.skipValue();
      final int valueLength = reader.getOffset() - valueOffset;

      setVariableLocal(
          scopeKey, document, nameOffset, nameLength, document, valueOffset, valueLength);
    }
  }

  public void setVariablesFromDocument(long scopeKey, DirectBuffer document) {
    // 1. index entries in the document
    indexedDocument.index(document);

    long currentScope = scopeKey;
    long parentScope;

    // 2. overwrite any variables in the scope hierarchy
    while (indexedDocument.hasEntries() && (parentScope = getParent(currentScope)) > 0) {
      final DocumentEntryIterator entryIterator = indexedDocument.iterator();

      while (entryIterator.hasNext()) {
        entryIterator.next();

        final boolean variableSet =
            setVariableLocalIfExists(
                currentScope,
                document,
                entryIterator.getNameOffset(),
                entryIterator.getNameLength(),
                document,
                entryIterator.getValueOffset(),
                entryIterator.getValueLength());

        if (variableSet) {
          entryIterator.remove();
        }

        currentScope = parentScope;
      }
      currentScope = parentScope;
    }

    // 3. set remaining variables on top scope
    if (indexedDocument.hasEntries()) {
      final DocumentEntryIterator entryIterator = indexedDocument.iterator();

      while (entryIterator.hasNext()) {
        entryIterator.next();

        setVariableLocal(
            currentScope,
            document,
            entryIterator.getNameOffset(),
            entryIterator.getNameLength(),
            document,
            entryIterator.getValueOffset(),
            entryIterator.getValueLength());
      }
    }
  }

  public void setVariableLocal(long scopeKey, DirectBuffer name, DirectBuffer value) {
    setVariableLocal(scopeKey, name, 0, name.capacity(), value, 0, value.capacity());
  }

  private void setVariableLocal(
      long scopeKey,
      DirectBuffer name,
      int nameOffset,
      int nameLength,
      DirectBuffer value,
      int valueOffset,
      int valueLength) {
    stateKeyBuffer.putLong(
        VARIABLES_SCOPE_KEY_OFFSET, scopeKey, ZeebeStateConstants.STATE_BYTE_ORDER);
    stateKeyBuffer.putBytes(VARIABLES_NAME_OFFSET, name, nameOffset, nameLength);

    final int keyLength = VARIABLES_NAME_OFFSET + nameLength;

    stateValueBuffer.putBytes(0, value, valueOffset, valueLength);

    stateController.put(
        variablesHandle,
        stateKeyBuffer.byteArray(),
        0,
        keyLength,
        stateValueBuffer.byteArray(),
        0,
        valueLength);
  }

  /** @return true if the variable was set */
  private boolean setVariableLocalIfExists(
      long scopeKey,
      DirectBuffer name,
      int nameOffset,
      int nameLength,
      DirectBuffer value,
      int valueOffset,
      int valueLength) {
    stateKeyBuffer.putLong(
        VARIABLES_SCOPE_KEY_OFFSET, scopeKey, ZeebeStateConstants.STATE_BYTE_ORDER);
    stateKeyBuffer.putBytes(VARIABLES_NAME_OFFSET, name, nameOffset, nameLength);

    final int keyLength = VARIABLES_NAME_OFFSET + nameLength;

    if (stateController.exist(variablesHandle, stateKeyBuffer.byteArray(), 0, keyLength)) {
      stateValueBuffer.putBytes(0, value, valueOffset, valueLength);

      stateController.put(
          variablesHandle,
          stateKeyBuffer.byteArray(),
          0,
          keyLength,
          stateValueBuffer.byteArray(),
          0,
          valueLength);

      return true;
    } else {
      return false;
    }
  }

  public DirectBuffer getVariableLocal(long scopeKey, DirectBuffer name) {
    stateKeyBuffer.putLong(
        VARIABLES_SCOPE_KEY_OFFSET, scopeKey, ZeebeStateConstants.STATE_BYTE_ORDER);
    stateKeyBuffer.putBytes(VARIABLES_NAME_OFFSET, name, 0, name.capacity());
    final int keyLength = VARIABLES_NAME_OFFSET + name.capacity();

    final int valueLength =
        stateController.get(
            variablesHandle,
            stateKeyBuffer.byteArray(),
            0,
            keyLength,
            stateValueBuffer.byteArray(),
            0,
            stateValueBuffer.capacity());

    if (valueLength == RocksDB.NOT_FOUND) {
      return null;
    } else if (valueLength > stateValueBuffer.capacity()) {
      stateValueBuffer.checkLimit(valueLength);
      stateController.get(
          variablesHandle,
          stateKeyBuffer.byteArray(),
          0,
          keyLength,
          stateValueBuffer.byteArray(),
          0,
          stateValueBuffer.capacity());
    }

    resultView.wrap(stateValueBuffer, 0, valueLength);
    return resultView;
  }

  private long getParent(long scopeKey) {
    stateKeyBuffer.putLong(0, scopeKey, ZeebeStateConstants.STATE_BYTE_ORDER);
    final int bytesRead =
        stateController.get(
            elementChildParentHandle,
            stateKeyBuffer.byteArray(),
            0,
            BitUtil.SIZE_OF_LONG,
            stateValueBuffer.byteArray(),
            0,
            BitUtil.SIZE_OF_LONG);

    return bytesRead >= 0 ? stateValueBuffer.getLong(0, ZeebeStateConstants.STATE_BYTE_ORDER) : -1;
  }

  public DirectBuffer getVariablesAsDocument(long scopeKey) {

    collectedVariables.clear();
    writer.wrap(documentResultBuffer, 0);

    writer.reserveMapHeader();

    visitVariables(
        scopeKey,
        name -> !collectedVariables.contains(name),
        (name, value) -> {
          writer.writeString(name);
          writer.writeRaw(value);

          // must create a new name wrapper, because we keep them all in the hashset at the same
          // time
          final MutableDirectBuffer nameView = new UnsafeBuffer(name);
          collectedVariables.add(nameView);
        },
        () -> false);

    writer.writeReservedMapHeader(0, collectedVariables.size());

    resultView.wrap(documentResultBuffer, 0, writer.getOffset());
    return resultView;
  }

  public DirectBuffer getVariablesAsDocument(long scopeKey, Collection<DirectBuffer> names) {

    variablesToCollect.clear();
    variablesToCollect.addAll(names);

    writer.wrap(documentResultBuffer, 0);

    writer.reserveMapHeader();

    visitVariables(
        scopeKey,
        name -> variablesToCollect.contains(name),
        (name, value) -> {
          writer.writeString(name);
          writer.writeRaw(value);

          variablesToCollect.remove(name);
        },
        () -> variablesToCollect.isEmpty());

    writer.writeReservedMapHeader(0, names.size() - variablesToCollect.size());

    resultView.wrap(documentResultBuffer, 0, writer.getOffset());
    return resultView;
  }

  /**
   * Like {@link #visitVariablesLocal(long, Predicate, BiConsumer, BooleanSupplier)} but walks up
   * the scope hierarchy.
   */
  private void visitVariables(
      long scopeKey,
      Predicate<DirectBuffer> filter,
      BiConsumer<DirectBuffer, DirectBuffer> variableConsumer,
      BooleanSupplier completionCondition) {
    long currentScope = scopeKey;

    boolean completed;
    do {
      completed = visitVariablesLocal(currentScope, filter, variableConsumer, completionCondition);

      currentScope = getParent(currentScope);

    } while (!completed && currentScope >= 0);
  }

  /**
   * Provides all variables of a scope to the given consumer until a condition is met.
   *
   * @param scopeKey
   * @param filter evaluated with the name of each variable; the variable is consumed only if the
   *     filter returns true
   * @param variableConsumer a consumer that receives variable name and value
   * @param completionCondition evaluated after every consumption; if true, consumption stops.
   * @return true if the completion condition was met
   */
  private boolean visitVariablesLocal(
      long scopeKey,
      Predicate<DirectBuffer> filter,
      BiConsumer<DirectBuffer, DirectBuffer> variableConsumer,
      BooleanSupplier completionCondition) {
    localVariableIterator.init(scopeKey);

    while (localVariableIterator.hasNext()) {
      final Tuple<DirectBuffer, DirectBuffer> currentVariable = localVariableIterator.next();

      if (filter.test(currentVariable.getLeft())) {
        variableConsumer.accept(currentVariable.getLeft(), currentVariable.getRight());

        if (completionCondition.getAsBoolean()) {
          return true;
        }
      }
    }

    localVariableIterator.close();

    return false;
  }

  public void removeAllVariables(long scopeKey) {
    localVariableIterator.init(scopeKey);

    while (localVariableIterator.hasNext()) {
      localVariableIterator.next();
      localVariableIterator.remove();
    }

    localVariableIterator.close();
  }

  private class LocalVariableIterator implements Iterator<Tuple<DirectBuffer, DirectBuffer>> {
    private final MutableDirectBuffer scopeKeyBuffer =
        new UnsafeBuffer(new byte[BitUtil.SIZE_OF_LONG]);
    private long scopeKey;
    private RocksIterator rocksDbIterator;

    private final MutableDirectBuffer currentScopeKeyBuffer = new UnsafeBuffer(0, 0);

    private UnsafeBuffer nameView = new UnsafeBuffer(0, 0);
    private UnsafeBuffer valueView = new UnsafeBuffer(0, 0);
    private Tuple<DirectBuffer, DirectBuffer> value = new Tuple<>(nameView, valueView);

    private void init(long scopeKey) {
      this.scopeKey = scopeKey;
      scopeKeyBuffer.putLong(0, scopeKey, ZeebeStateConstants.STATE_BYTE_ORDER);

      rocksDbIterator = stateController.getDb().newIterator(variablesHandle);
      rocksDbIterator.seek(scopeKeyBuffer.byteArray());
    }

    @Override
    public boolean hasNext() {
      if (rocksDbIterator.isValid()) {
        final byte[] currentKey = rocksDbIterator.key();
        currentScopeKeyBuffer.wrap(currentKey);

        return currentScopeKeyBuffer.getLong(0, ZeebeStateConstants.STATE_BYTE_ORDER) == scopeKey;
      } else {
        return false;
      }
    }

    @Override
    public Tuple<DirectBuffer, DirectBuffer> next() {

      final byte[] currentKey = rocksDbIterator.key();
      final byte[] currentValue = rocksDbIterator.value();

      final int nameLength = currentKey.length - VARIABLES_NAME_OFFSET;
      nameView.wrap(currentKey, VARIABLES_NAME_OFFSET, nameLength);
      valueView.wrap(currentValue, 0, currentValue.length);

      if (rocksDbIterator.isValid()) {
        rocksDbIterator.next();
      }

      return value;
    }

    @Override
    public void remove() {
      stateController.remove(
          variablesHandle, nameView.byteArray(), 0, VARIABLES_NAME_OFFSET + nameView.capacity());
    }

    private void close() {
      rocksDbIterator.close();
      rocksDbIterator = null;
    }
  }

  private class IndexedDocument implements Iterable<Void> {
    // variable name offset -> variable value offset
    private Int2IntHashMap entries = new Int2IntHashMap(-1);
    private DocumentEntryIterator iterator = new DocumentEntryIterator();
    private DirectBuffer document;

    public void index(DirectBuffer document) {
      this.document = document;
      entries.clear();
      final int documentLength = document.capacity();

      reader.wrap(document, 0, documentLength);

      final int variables = reader.readMapHeader();

      for (int i = 0; i < variables; i++) {
        final int keyOffset = reader.getOffset();
        reader.skipValue();
        final int valueOffset = reader.getOffset();
        reader.skipValue();

        entries.put(keyOffset, valueOffset);
      }
    }

    @Override
    public DocumentEntryIterator iterator() {
      iterator.wrap(document, entries.entrySet().iterator());
      return iterator;
    }

    public boolean hasEntries() {
      return !entries.isEmpty();
    }
  }

  private class DocumentEntryIterator implements Iterator<Void> {
    private EntryIterator iterator;
    private DirectBuffer document;
    private int documentLength;

    // per entry
    private int nameOffset;
    private int nameLength;
    private int valueOffset;
    private int valueLength;

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    public void wrap(DirectBuffer document, EntryIterator iterator) {
      this.iterator = iterator;
      this.document = document;
      this.documentLength = document.capacity();
    }

    /** excluding string header */
    public int getNameOffset() {
      return nameOffset;
    }

    public int getNameLength() {
      return nameLength;
    }

    /** including header */
    public int getValueOffset() {
      return valueOffset;
    }

    public int getValueLength() {
      return valueLength;
    }

    @Override
    public Void next() {
      iterator.next();

      final int keyOffset = iterator.getIntKey();
      valueOffset = iterator.getIntValue();

      reader.wrap(document, keyOffset, documentLength - keyOffset);

      nameLength = reader.readStringLength();
      nameOffset = keyOffset + reader.getOffset();

      reader.wrap(document, valueOffset, documentLength - valueOffset);
      reader.skipValue();
      valueLength = reader.getOffset();

      return null;
    }

    @Override
    public void remove() {
      iterator.remove();
    }
  }
}
