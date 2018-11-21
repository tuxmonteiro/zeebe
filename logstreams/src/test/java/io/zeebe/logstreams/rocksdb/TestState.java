package io.zeebe.logstreams.rocksdb;

import static io.zeebe.util.ByteArrayUtil.concat;

import io.zeebe.logstreams.rocksdb.serializers.IntSerializer;
import io.zeebe.logstreams.rocksdb.serializers.LongSerializer;
import io.zeebe.logstreams.rocksdb.serializers.TupleSerializer;
import java.util.Arrays;
import java.util.List;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;

public class TestState extends ZbState {
  private final TupleSerializer<Long, Integer> keySerializer =
      new TupleSerializer<>(LongSerializer.INSTANCE, IntSerializer.INSTANCE);
  private final MutableDirectBuffer keyBuffer =
      new UnsafeBuffer(new byte[Long.BYTES + Integer.BYTES]);

  private final ExpandableArrayBuffer valueBuffer = new ExpandableArrayBuffer();
  private final TestUnpackedObject.Serializer valueSerializer = new TestUnpackedObject.Serializer();

  public static List<ZbStateColumnDescriptor> getDescriptors(byte[] prefix) {
    return Arrays.asList(
        new ZbStateColumnDescriptor<>(
            concat(prefix, TestColumn.NAME), new ColumnFamilyOptions(), TestState::newTestColumn));
  }

  public TestState(
      ZbRocksDb db,
      List<ColumnFamilyHandle> handles,
      List<ZbStateColumnDescriptor> columnDescriptors) {
    super(db, handles, columnDescriptors);
  }

  public TestColumn newTestColumn(ZbRocksDb db, ColumnFamilyHandle handle) {
    return new TestColumn(db, handle, keyBuffer, keySerializer, valueBuffer, valueSerializer);
  }
}
