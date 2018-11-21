package io.zeebe.logstreams.rocksdb;

import static io.zeebe.util.ByteArrayUtil.concat;

import io.zeebe.logstreams.rocksdb.serializers.IntSerializer;
import io.zeebe.logstreams.rocksdb.serializers.LongSerializer;
import io.zeebe.logstreams.rocksdb.serializers.TupleSerializer;
import java.util.Collections;
import java.util.List;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.ColumnFamilyHandle;

public class TestState extends ZbState {
  private final TupleSerializer<Long, Integer> keySerializer =
      new TupleSerializer<>(LongSerializer.INSTANCE, IntSerializer.INSTANCE);
  private final MutableDirectBuffer keyBuffer =
      new UnsafeBuffer(new byte[Long.BYTES + Integer.BYTES]);

  private final ExpandableArrayBuffer valueBuffer = new ExpandableArrayBuffer();
  private final TestUnpackedObject.Serializer valueSerializer = new TestUnpackedObject.Serializer();

  private final TestColumn testColumn;

  public static List<ZbStateColumnDescriptor> getDescriptors(byte[] prefix) {
    return Collections.singletonList(
        new ZbStateColumnDescriptor<>(concat(prefix, TestColumn.NAME), TestState::newTestColumn));
  }

  public TestState(
      ZbRocksDb db,
      List<ColumnFamilyHandle> handles,
      List<ZbStateColumnDescriptor> columnDescriptors) {
    super(db, handles, columnDescriptors);

    this.testColumn = (TestColumn) columns.get(0);
  }

  public TestColumn newTestColumn(ZbRocksDb db, ColumnFamilyHandle handle) {
    return new TestColumn(db, handle, keyBuffer, keySerializer, valueBuffer, valueSerializer);
  }

  public TestColumn getTestColumn() {
    return testColumn;
  }
}
