package io.zeebe.broker.spike;

import java.nio.charset.StandardCharsets;
import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class ElementInstanceValue implements DbValue {

  // dummy value
  private byte[] value;
  private long key;

  public void setValue(String value) {
    this.value = value.getBytes(StandardCharsets.UTF_8);
  }

  public String getValue() {
    return new String(value, StandardCharsets.UTF_8);
  }

  public void setKey(long key) {
    this.key = key;
  }

  public long getKey() {
    return key;
  }

  @Override
  public void wrap(DirectBuffer buffer, int offset, int length) {

    key = buffer.getLong(offset);
    value = new byte[length - BitUtil.SIZE_OF_LONG];
    buffer.getBytes(offset + BitUtil.SIZE_OF_LONG, value);
  }

  @Override
  public int getLength() {
    return BitUtil.SIZE_OF_LONG + value.length;
  }

  @Override
  public void write(MutableDirectBuffer buffer, int offset) {
    buffer.putLong(offset, key);
    buffer.putBytes(offset + BitUtil.SIZE_OF_LONG, value);
  }

}
