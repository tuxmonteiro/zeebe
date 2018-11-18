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

import io.zeebe.logstreams.rocksdb.Serializer;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class DirectBufferSerializer implements Serializer<DirectBuffer> {
  private final UnsafeBuffer instance = new UnsafeBuffer(0, 0);

  @Override
  public DirectBuffer newInstance() {
    return instance;
  }

  @Override
  public int getLength() {
    return VARIABLE_LENGTH;
  }

  @Override
  public DirectBuffer serialize(DirectBuffer value, MutableDirectBuffer dest, int offset) {
    dest.putBytes(offset, value, 0, value.capacity());
    return new UnsafeBuffer(dest, 0, value.capacity());
  }

  @Override
  public DirectBuffer deserialize(DirectBuffer source, int offset, int length) {
    instance.wrap(source, offset, length);
    return instance;
  }
}
