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
import java.util.ArrayList;
import java.util.List;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class ListSerializer<T> implements Serializer<List<T>> {
  private final List<T> instance = new ArrayList<>(0);
  private final Serializer<T> elementSerializer;

  public ListSerializer(Serializer<T> elementSerializer) {
    this.elementSerializer = elementSerializer;
  }

  @Override
  public List<T> newInstance() {
    return instance;
  }

  @Override
  public int getLength() {
    return VARIABLE_LENGTH;
  }

  @Override
  public int serialize(List<T> value, MutableDirectBuffer dest, int offset) {
    int cursor = offset;
    dest.putInt(cursor, value.size(), STATE_BYTE_ORDER);
    cursor += Integer.BYTES;

    for (final T element : value) {
      final int length = elementSerializer.serialize(element, dest, cursor + Integer.BYTES);
      dest.putInt(cursor, length, STATE_BYTE_ORDER);
      cursor += length + Integer.BYTES;
    }

    return cursor - offset;
  }

  @Override
  public List<T> deserialize(DirectBuffer source, int offset, int length) {
    final List<T> list = newInstance();
    final int size = source.getInt(offset, STATE_BYTE_ORDER);
    int cursor = offset + Integer.BYTES;

    for (int i = 0; i < size; i++) {
      final int elementLength = source.getInt(cursor, STATE_BYTE_ORDER);
      final T element =
          elementSerializer.deserialize(source, cursor + Integer.BYTES, elementLength);

      list.add(element);
      cursor += Integer.BYTES + elementLength;
    }

    assert (cursor - offset) == length : "End offset differs from length";
    return list;
  }
}
