/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.logstreams.rocksdb;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;

public class ZbColumn<K, V> implements Iterable<ZbColumnEntry<K, V>>, AutoCloseable {
  private final ZbRocksDb db;
  private final ColumnFamilyHandle columnFamilyHandle;

  private final Serializer<K> keySerializer;
  private final MutableDirectBuffer keyBuffer;

  private final Serializer<V> valueSerializer;
  private final MutableDirectBuffer valueBuffer;

  public ZbColumn(
      ZbRocksDb db,
      ColumnFamilyHandle columnFamilyHandle,
      MutableDirectBuffer keyBuffer,
      Serializer<K> keySerializer,
      MutableDirectBuffer valueBuffer,
      Serializer<V> valueSerializer) {
    this.db = db;
    this.columnFamilyHandle = columnFamilyHandle;
    this.keyBuffer = keyBuffer;
    this.keySerializer = keySerializer;
    this.valueBuffer = valueBuffer;
    this.valueSerializer = valueSerializer;
  }

  public void put(K key, V value) {
    db.put(columnFamilyHandle, serializeKey(key), serializeValue(value));
  }

  public V get(K key) {
    final int bytesRead = db.get(columnFamilyHandle, serializeKey(key), valueBuffer);

    if (bytesRead == RocksDB.NOT_FOUND) {
      return null;
    }

    return valueSerializer.deserialize(valueBuffer, bytesRead);
  }

  public void delete(K key) {
    db.delete(columnFamilyHandle, serializeKey(key));
  }

  public void delete(ZbColumnIteratorEntry entry) {
    db.delete(columnFamilyHandle, entry.getKeyBuffer());
  }

  public boolean exists(K key) {
    return db.exists(columnFamilyHandle, serializeKey(key));
  }

  @Override
  public Iterator<ZbColumnEntry<K, V>> iterator() {
    return new ZbColumnIterator<>(this, db.newIterator(columnFamilyHandle));
  }

  @Override
  public Spliterator<ZbColumnEntry<K, V>> spliterator() {
    return Spliterators.spliterator(
        iterator(),
        db.getEstimatedNumberOfKeys(columnFamilyHandle),
        Spliterator.SORTED | Spliterator.NONNULL | Spliterator.DISTINCT);
  }

  @Override
  public void close() {
    columnFamilyHandle.close();
  }

  public Serializer<K> getKeySerializer() {
    return keySerializer;
  }

  public Serializer<V> getValueSerializer() {
    return valueSerializer;
  }

  protected DirectBuffer serializeKey(K key) {
    return keySerializer.serialize(key, keyBuffer);
  }

  protected DirectBuffer serializeValue(V value) {
    return valueSerializer.serialize(value, valueBuffer);
  }
}
