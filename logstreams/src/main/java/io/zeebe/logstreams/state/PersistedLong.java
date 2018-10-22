package io.zeebe.logstreams.state;

import static io.zeebe.logstreams.rocksdb.ZeebeStateConstants.STATE_BYTE_ORDER;

import io.zeebe.util.buffer.BufferReader;
import io.zeebe.util.buffer.BufferWriter;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class PersistedLong implements BufferReader, BufferWriter {

  private long value;

  public long getValue() {
    return value;
  }

  @Override
  public void wrap(DirectBuffer buffer, int offset, int length) {
    value = buffer.getInt(offset, STATE_BYTE_ORDER);
  }

  @Override
  public int getLength() {
    return Integer.BYTES;
  }

  @Override
  public void write(MutableDirectBuffer buffer, int offset) {
    buffer.putLong(offset, value, STATE_BYTE_ORDER);
  }
}
