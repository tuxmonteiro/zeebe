/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.logstreams.rocksdb.serializers;

import static io.zeebe.logstreams.rocksdb.ZeebeStateConstants.STATE_BYTE_ORDER;

import io.zeebe.logstreams.rocksdb.Serializer;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class LongSerializer implements Serializer<Long> {
  public static final LongSerializer INSTANCE = new LongSerializer();
  private static final Long ZERO = 0L;

  @Override
  public DirectBuffer serialize(Long value, MutableDirectBuffer dest, int offset) {
    dest.putLong(offset, value, STATE_BYTE_ORDER);
    return new UnsafeBuffer(dest, offset, Long.BYTES);
  }

  @Override
  public Long deserialize(DirectBuffer source, int offset, int length) {
    return source.getLong(offset, STATE_BYTE_ORDER);
  }

  @Override
  public int getLength() {
    return Long.BYTES;
  }

  @Override
  public Long newInstance() {
    return ZERO;
  }
}
