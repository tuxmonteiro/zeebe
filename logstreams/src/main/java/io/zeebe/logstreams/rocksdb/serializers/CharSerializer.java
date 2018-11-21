package io.zeebe.logstreams.rocksdb.serializers;

import static io.zeebe.logstreams.rocksdb.ZeebeStateConstants.STATE_BYTE_ORDER;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class CharSerializer implements Serializer<Character> {
  public static final CharSerializer INSTANCE = new CharSerializer();
  private static final Character ZERO = 0;

  @Override
  public int serialize(Character value, MutableDirectBuffer dest, int offset) {
    dest.putChar(offset, value, STATE_BYTE_ORDER);
    return getLength();
  }

  @Override
  public Character deserialize(DirectBuffer source, int offset, int length) {
    return source.getChar(offset, STATE_BYTE_ORDER);
  }

  @Override
  public int getLength() {
    return Integer.BYTES;
  }

  @Override
  public Character newInstance() {
    return ZERO;
  }
}
