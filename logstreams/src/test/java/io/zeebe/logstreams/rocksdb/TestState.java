package io.zeebe.logstreams.rocksdb;

import static io.zeebe.util.ByteArrayUtil.concat;

import io.zeebe.logstreams.rocksdb.serializers.LongSerializer;
import java.util.Arrays;
import java.util.List;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;

public class TestState extends ZbState {
  private final LongSerializer keySerializer = LongSerializer.INSTANCE;
  private final MutableDirectBuffer keyBuffer = new UnsafeBuffer(new byte[Long.BYTES]);

  private final ExpandableArrayBuffer valueBuffer = new ExpandableArrayBuffer();
  private final TestUnpackedObject.Serializer valueSerializer = new TestUnpackedObject.Serializer();

  public static List<ZbStateColumnDescriptor> getDescriptors(byte[] prefix) {
    return Arrays.asList(
        new ZbStateColumnDescriptor<>(
            concat(prefix, Column1.NAME), new ColumnFamilyOptions(), TestState::newColumn1),
        new ZbStateColumnDescriptor<>(
            concat(prefix, Column2.NAME), new ColumnFamilyOptions(), TestState::newColumn2));
  }

  public TestState(
      ZbRocksDb db,
      List<ColumnFamilyHandle> handles,
      List<ZbStateColumnDescriptor> columnDescriptors) {
    super(db, handles, columnDescriptors);
  }

  public Column1 newColumn1(ZbRocksDb db, ColumnFamilyHandle handle) {
    return new Column1(db, handle, keyBuffer, keySerializer, valueBuffer, valueSerializer);
  }

  public Column2 newColumn2(ZbRocksDb db, ColumnFamilyHandle handle) {
    return new Column2(db, handle, keyBuffer, keySerializer, valueBuffer, valueSerializer);
  }

  static class Column1 extends ZbColumn<Long, TestUnpackedObject> {
    static final byte[] NAME = "column1".getBytes();

    public Column1(
        ZbRocksDb db,
        ColumnFamilyHandle columnFamilyHandle,
        MutableDirectBuffer keyBuffer,
        Serializer<Long> keySerializer,
        MutableDirectBuffer valueBuffer,
        Serializer<TestUnpackedObject> valueSerializer) {
      super(db, columnFamilyHandle, keyBuffer, keySerializer, valueBuffer, valueSerializer);
    }
  }

  static class Column2 extends ZbColumn<Long, TestUnpackedObject> {
    static final byte[] NAME = "column2".getBytes();

    public Column2(
        ZbRocksDb db,
        ColumnFamilyHandle columnFamilyHandle,
        MutableDirectBuffer keyBuffer,
        Serializer<Long> keySerializer,
        MutableDirectBuffer valueBuffer,
        Serializer<TestUnpackedObject> valueSerializer) {
      super(db, columnFamilyHandle, keyBuffer, keySerializer, valueBuffer, valueSerializer);
    }
  }
}
