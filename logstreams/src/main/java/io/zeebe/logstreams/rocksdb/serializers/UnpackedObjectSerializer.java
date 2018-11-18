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
import io.zeebe.msgpack.UnpackedObject;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public abstract class UnpackedObjectSerializer<T extends UnpackedObject> implements Serializer<T> {
  @Override
  public DirectBuffer serialize(T value, MutableDirectBuffer dest, int offset) {
    value.write(dest, offset);
    return new UnsafeBuffer(dest, offset, value.getLength());
  }

  @Override
  public T deserialize(DirectBuffer source, int offset, int length) {
    final T instance = newInstance();
    instance.wrap(source, offset, length);

    return instance;
  }

  @Override
  public int getLength() {
    return VARIABLE_LENGTH;
  }

  public static <R extends UnpackedObject> UnpackedObjectSerializer<R> of(R instance) {
    return new UnpackedObjectSerializer<R>() {
      @Override
      public R newInstance() {
        return instance;
      }
    };
  }
}
