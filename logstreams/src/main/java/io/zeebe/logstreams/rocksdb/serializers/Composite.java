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
import static io.zeebe.logstreams.rocksdb.serializers.Serializer.VARIABLE_LENGTH;

import java.util.function.Consumer;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public abstract class Composite {
  protected int getLength(Serializer<?>... serializers) {
    int length = 0;

    for (Serializer<?> serializer : serializers) {
      final int serializerLength = serializer.getLength();
      if (serializerLength == VARIABLE_LENGTH) {
        return VARIABLE_LENGTH;
      }

      length += serializerLength;
    }

    return length;
  }

  protected <T> int serializeMember(
      T value, Serializer<T> serializer, MutableDirectBuffer dest, int offset) {
    int serializedLength = serializer.getLength();

    if (serializedLength != VARIABLE_LENGTH) {
      serializer.serialize(value, dest, offset);
    } else {
      serializedLength = serializer.serialize(value, dest, offset + Integer.BYTES);
      dest.putInt(offset, serializedLength, STATE_BYTE_ORDER);
      offset += Integer.BYTES;
    }

    return offset + serializedLength;
  }

  protected <T> DirectBuffer serializeMemberInto(T value, Serializer<T> serializer, MutableDirectBuffer dest, int offset, DirectBuffer view) {
    final int length = serializeMember(value, serializer, dest, offset);
    view.wrap(dest, offset, length);

    return view;
  }

  protected <T> int deserializeMember(
      Consumer<T> value, Serializer<T> serializer, DirectBuffer source, int offset) {
    int length = serializer.getLength();

    if (length == VARIABLE_LENGTH) {
      length = source.getInt(offset, STATE_BYTE_ORDER);
      offset += Integer.BYTES;
    }

    value.accept(serializer.deserialize(source, offset, length));
    return offset + length;
  }
}
