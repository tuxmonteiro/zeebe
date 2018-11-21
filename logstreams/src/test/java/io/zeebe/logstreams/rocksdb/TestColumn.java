package io.zeebe.logstreams.rocksdb;

import io.zeebe.logstreams.rocksdb.serializers.Serializer;
import io.zeebe.logstreams.rocksdb.serializers.TupleSerializer;
import io.zeebe.util.collection.Tuple;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;

public class TestColumn extends ZbColumn<Tuple<Long, Integer>, TestUnpackedObject> {
  public static final byte[] NAME = "test".getBytes();

  private final MutableDirectBuffer prefixBuffer = new UnsafeBuffer(new byte[Long.BYTES]);
  private final TupleSerializer<Long, Integer> keySerializer;

  public TestColumn(
      ZbRocksDb db,
      ColumnFamilyHandle columnFamilyHandle,
      MutableDirectBuffer keyBuffer,
      TupleSerializer<Long, Integer> keySerializer,
      MutableDirectBuffer valueBuffer,
      Serializer<TestUnpackedObject> valueSerializer) {
    super(db, columnFamilyHandle, keyBuffer, keySerializer, valueBuffer, valueSerializer);
    this.keySerializer = keySerializer;
  }

  public void forEachWithPrefix(long prefix, Visitor visitor) {
    keySerializer.serializePrefix(prefix, prefixBuffer, 0);
    try (final ReadOptions options = new ReadOptions();
        final ZbRocksIterator rocksIterator = newIterator(options)) {}
  }

  @FunctionalInterface
  public interface Visitor {
    void visit(Tuple<Long, Integer> key, TestUnpackedObject value);
  }
}
