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
package io.zeebe.logstreams.rocksdb;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public interface Serializer<T> {
  int VARIABLE_LENGTH = -1;

  T newInstance();

  int getLength();

  int serialize(T value, MutableDirectBuffer dest, int offset);

  default int serialize(T value, MutableDirectBuffer dest) {
    return serialize(value, dest, 0);
  }

  T deserialize(DirectBuffer source, int offset, int length);

  default T deserialize(DirectBuffer source, int length) {
    return deserialize(source, 0, length);
  }

  default T deserialize(DirectBuffer source) {
    return deserialize(source, source.capacity());
  }
}
