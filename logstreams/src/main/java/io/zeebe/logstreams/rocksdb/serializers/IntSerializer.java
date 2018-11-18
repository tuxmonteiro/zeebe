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

public class IntSerializer implements Serializer<Integer> {
  public static final IntSerializer INSTANCE = new IntSerializer();
  private static final Integer ZERO = 0;

  @Override
  public DirectBuffer serialize(Integer value, MutableDirectBuffer dest, int offset) {
    dest.putInt(offset, value, STATE_BYTE_ORDER);
    return new UnsafeBuffer(dest, offset, Integer.BYTES);
  }

  @Override
  public Integer deserialize(DirectBuffer source, int offset, int length) {
    return source.getInt(offset, STATE_BYTE_ORDER);
  }

  @Override
  public int getLength() {
    return Integer.BYTES;
  }

  @Override
  public Integer newInstance() {
    return ZERO;
  }
}
