package io.zeebe.logstreams.rocksdb.serializers;

import static io.zeebe.logstreams.rocksdb.ZeebeStateConstants.STATE_BYTE_ORDER;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class FloatSerializer implements Serializer<Float> {
  public static final FloatSerializer INSTANCE = new FloatSerializer();
  private static final Float ZERO = 0.0f;

  @Override
  public int serialize(Float value, MutableDirectBuffer dest, int offset) {
    dest.putFloat(offset, value, STATE_BYTE_ORDER);
    return getLength();
  }

  @Override
  public Float deserialize(DirectBuffer source, int offset, int length) {
    return source.getFloat(offset, STATE_BYTE_ORDER);
  }

  @Override
  public int getLength() {
    return Integer.BYTES;
  }

  @Override
  public Float newInstance() {
    return ZERO;
  }
}
