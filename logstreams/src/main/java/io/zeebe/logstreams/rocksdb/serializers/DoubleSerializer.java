package io.zeebe.logstreams.rocksdb.serializers;

import static io.zeebe.logstreams.rocksdb.ZeebeStateConstants.STATE_BYTE_ORDER;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class DoubleSerializer implements Serializer<Double> {
  public static final DoubleSerializer INSTANCE = new DoubleSerializer();
  private static final Double ZERO = 0.0;

  @Override
  public int serialize(Double value, MutableDirectBuffer dest, int offset) {
    dest.putDouble(offset, value, STATE_BYTE_ORDER);
    return getLength();
  }

  @Override
  public Double deserialize(DirectBuffer source, int offset, int length) {
    return source.getDouble(offset, STATE_BYTE_ORDER);
  }

  @Override
  public int getLength() {
    return Integer.BYTES;
  }

  @Override
  public Double newInstance() {
    return ZERO;
  }
}
