package io.zeebe.logstreams.state;

import static io.zeebe.logstreams.rocksdb.ZeebeStateConstants.STATE_BYTE_ORDER;

import io.zeebe.logstreams.rocksdb.ZbRocksDb;
import io.zeebe.util.buffer.BufferReader;
import io.zeebe.util.buffer.BufferWriter;
import java.util.function.BiConsumer;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.ColumnFamilyHandle;

public class State implements StateLifecycleListener {

  public static final Long MISSING_VALUE = Long.MIN_VALUE;
  private final byte[] columnFamilyName;
  private final MutableDirectBuffer longBuffer;
  private final MutableDirectBuffer valueBuffer = new ExpandableArrayBuffer();

  private PersistenceHelper persistenceHelper;
  private ZbRocksDb zbRocksDb;
  private ColumnFamilyHandle columnFamilyHandle;

  public State(byte[] columnFamilyName) {
    this.columnFamilyName = columnFamilyName;
    longBuffer = new UnsafeBuffer(new byte[Long.BYTES]);
  }

  @Override
  public void onOpenedDb(ZbRocksDb zbRocksDb, ColumnFamilyHandle columnFamilyHandle) {
    this.zbRocksDb = zbRocksDb;
    this.persistenceHelper = new PersistenceHelper(zbRocksDb);
    this.columnFamilyHandle = columnFamilyHandle;
  }

  public void put(long key, byte[] value, int offset, int length) {
    setLong(key);

    zbRocksDb.put(columnFamilyHandle, longBuffer.byteArray(), 0, Long.BYTES, value, offset, length);
  }

  public void put(long key, BufferWriter bufferWriter) {
    setLong(key);

    final int length = bufferWriter.getLength();
    bufferWriter.write(valueBuffer, 0);

    zbRocksDb.put(
        columnFamilyHandle,
        longBuffer.byteArray(),
        0,
        Long.BYTES,
        valueBuffer.byteArray(),
        0,
        length);
  }

  public <T extends BufferReader> T get(long key, Class<T> valueClass) {
    setLong(key);

    return persistenceHelper.getValueInstance(
        valueClass, columnFamilyHandle, longBuffer, 0, Long.BYTES);
  }

  private void setLong(long longValue) {
    longBuffer.putLong(0, longValue, STATE_BYTE_ORDER);
  }

  public byte[] getColumnFamilyName() {
    return columnFamilyName;
  }

  final PersistedLong persistedLong = new PersistedLong();

  public long getLong(byte[] key, int offset, int length) {
    final boolean success =
        persistenceHelper.readInto(persistedLong, columnFamilyHandle, key, offset, length);
    return success ? persistedLong.getValue() : MISSING_VALUE;
  }

  public void putLong(byte[] key, int offset, int length, long value) {
    setLong(value);

    zbRocksDb.put(columnFamilyHandle, key, offset, length, longBuffer.byteArray(), 0, Long.BYTES);
  }

  public void delete(long key) {
    setLong(key);

    zbRocksDb.delete(columnFamilyHandle, longBuffer.byteArray(), 0, Long.BYTES);
  }

  public <K extends BufferReader, T extends BufferReader> void foreach(
      K keyReader, T valueReader, BiConsumer<K, T> consumer) {
    zbRocksDb.forEach(
        columnFamilyHandle,
        (zbRocksEntry, iteratorControl) -> {
          final DirectBuffer valueBuffer = zbRocksEntry.getValue();
          valueReader.wrap(valueBuffer, 0, valueBuffer.capacity());

          final DirectBuffer keyBuffer = zbRocksEntry.getKey();
          keyReader.wrap(keyBuffer, 0, keyBuffer.capacity());

          consumer.accept(keyReader, valueReader);
        });
  }
}
