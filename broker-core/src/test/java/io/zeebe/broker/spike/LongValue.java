package io.zeebe.broker.spike;

import java.nio.ByteOrder;
import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class LongValue implements DbValue {

  private long key;

  public long get()
  {
    return key;
  }

  public void set(long key)
  {
    this.key = key;
  }

  @Override
  public void wrap(DirectBuffer buffer, int offset, int length) {
    key = buffer.getLong(offset, ByteOrder.LITTLE_ENDIAN);
  }

  @Override
  public int getLength() {
    return BitUtil.SIZE_OF_LONG;
  }

  @Override
  public void write(MutableDirectBuffer buffer, int offset) {
    buffer.putLong(offset, key, ByteOrder.LITTLE_ENDIAN);
  }
}
