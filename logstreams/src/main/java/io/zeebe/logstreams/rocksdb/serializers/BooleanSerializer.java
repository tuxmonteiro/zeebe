package io.zeebe.logstreams.rocksdb.serializers;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class BooleanSerializer implements Serializer<Boolean> {
  public static final BooleanSerializer INSTANCE = new BooleanSerializer();
  public static final Byte TRUE = 1;
  public static final Byte FALSE = 0;

  @Override
  public int serialize(Boolean value, MutableDirectBuffer dest, int offset) {
    final byte byteValue = value ? TRUE : FALSE;
    return ByteSerializer.INSTANCE.serialize(byteValue, dest, offset);
  }

  @Override
  public Boolean deserialize(DirectBuffer source, int offset, int length) {
    return ByteSerializer.INSTANCE.deserialize(source, offset, length).equals(TRUE);
  }

  @Override
  public int getLength() {
    return ByteSerializer.INSTANCE.getLength();
  }

  @Override
  public Boolean newInstance() {
    return Boolean.TRUE;
  }
}
