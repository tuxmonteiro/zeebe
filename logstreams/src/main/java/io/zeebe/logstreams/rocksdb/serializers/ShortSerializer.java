package io.zeebe.logstreams.rocksdb.serializers;

import static io.zeebe.logstreams.rocksdb.ZeebeStateConstants.STATE_BYTE_ORDER;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class ShortSerializer implements Serializer<Short> {
  public static final ShortSerializer INSTANCE = new ShortSerializer();
  private static final Short ZERO = 0;

  @Override
  public int serialize(Short value, MutableDirectBuffer dest, int offset) {
    dest.putShort(offset, value, STATE_BYTE_ORDER);
    return getLength();
  }

  @Override
  public Short deserialize(DirectBuffer source, int offset, int length) {
    return source.getShort(offset, STATE_BYTE_ORDER);
  }

  @Override
  public int getLength() {
    return Integer.BYTES;
  }

  @Override
  public Short newInstance() {
    return ZERO;
  }
}
