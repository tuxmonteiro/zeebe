/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.workflow.state;

import static io.zeebe.logstreams.rocksdb.ZeebeStateConstants.STATE_BYTE_ORDER;
import static io.zeebe.util.buffer.BufferUtil.readIntoBuffer;
import static io.zeebe.util.buffer.BufferUtil.writeIntoBuffer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class TimerInstance implements Persistable {
  public static final int KEY_LENGTH = 2 * Long.BYTES;

  private final DirectBuffer handlerNodeId = new UnsafeBuffer(0, 0);
  private long key;
  private long elementInstanceKey;
  private long dueDate;
  private int repetitions;

  public long getElementInstanceKey() {
    return elementInstanceKey;
  }

  public void setElementInstanceKey(long elementInstanceKey) {
    this.elementInstanceKey = elementInstanceKey;
  }

  public long getDueDate() {
    return dueDate;
  }

  public void setDueDate(long dueDate) {
    this.dueDate = dueDate;
  }

  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }

  public DirectBuffer getHandlerNodeId() {
    return handlerNodeId;
  }

  public void setHandlerNodeId(DirectBuffer handlerNodeId) {
    this.handlerNodeId.wrap(handlerNodeId);
  }

  public int getRepetitions() {
    return repetitions;
  }

  public void setRepetitions(int repetitions) {
    this.repetitions = repetitions;
  }

  @Override
  public int getLength() {
    return 3 * Long.BYTES + 2 * Integer.BYTES + handlerNodeId.capacity();
  }

  @Override
  public void write(MutableDirectBuffer buffer, int offset) {
    buffer.putLong(offset, elementInstanceKey, STATE_BYTE_ORDER);
    offset += Long.BYTES;

    buffer.putLong(offset, dueDate, STATE_BYTE_ORDER);
    offset += Long.BYTES;

    buffer.putLong(offset, key, STATE_BYTE_ORDER);
    offset += Long.BYTES;

    buffer.putInt(offset, repetitions, STATE_BYTE_ORDER);
    offset += Integer.BYTES;

    offset = writeIntoBuffer(buffer, offset, handlerNodeId);
    assert offset == getLength() : "End offset differs from getLength()";
  }

  @Override
  public void wrap(DirectBuffer buffer, int offset, int length) {
    elementInstanceKey = buffer.getLong(offset, STATE_BYTE_ORDER);
    offset += Long.BYTES;

    dueDate = buffer.getLong(offset, STATE_BYTE_ORDER);
    offset += Long.BYTES;

    key = buffer.getLong(offset, STATE_BYTE_ORDER);
    offset += Long.BYTES;

    repetitions = buffer.getInt(offset, STATE_BYTE_ORDER);
    offset += Integer.BYTES;

    offset = readIntoBuffer(buffer, offset, handlerNodeId);
    assert offset == length : "End offset differs from length";
  }

  @Override
  public void writeKey(MutableDirectBuffer keyBuffer, int offset) {
    int keyOffset = offset;
    keyBuffer.putLong(keyOffset, elementInstanceKey, STATE_BYTE_ORDER);
    keyOffset += Long.BYTES;

    keyBuffer.putLong(keyOffset, key, STATE_BYTE_ORDER);
    keyOffset += Long.BYTES;

    assert (keyOffset - offset) == getKeyLength() : "End offset differs from getKeyLength()";
  }

  @Override
  public int getKeyLength() {
    return KEY_LENGTH;
  }
}
