package io.zeebe.broker.spike;

import io.zeebe.util.buffer.BufferReader;
import io.zeebe.util.buffer.BufferWriter;

public interface DbValue extends BufferReader, BufferWriter {

  // distinction between key and value is probably not necessary
}
