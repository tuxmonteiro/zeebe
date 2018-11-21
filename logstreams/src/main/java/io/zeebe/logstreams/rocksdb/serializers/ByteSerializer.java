package io.zeebe.logstreams.rocksdb.serializers;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class ByteSerializer implements Serializer<Byte> {
  public static final ByteSerializer INSTANCE = new ByteSerializer();
  private static final Byte ZERO = 0;

  @Override
  public int serialize(Byte value, MutableDirectBuffer dest, int offset) {
    dest.putByte(offset, value);
    return getLength();
  }

  @Override
  public Byte deserialize(DirectBuffer source, int offset, int length) {
    return source.getByte(offset);
  }

  @Override
  public int getLength() {
    return 1;
  }

  @Override
  public Byte newInstance() {
    return ZERO;
  }
}
