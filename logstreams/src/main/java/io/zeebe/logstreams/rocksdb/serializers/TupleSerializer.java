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
import io.zeebe.util.collection.Tuple;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class TupleSerializer<L, R> extends Composite implements Serializer<Tuple<L, R>> {
  private final Tuple<L, R> instance = new Tuple<>();
  private final Serializer<L> leftSerializer;
  private final Serializer<R> rightSerializer;

  public TupleSerializer(Serializer<L> leftSerializer, Serializer<R> rightSerializer) {
    this.leftSerializer = leftSerializer;
    this.rightSerializer = rightSerializer;
  }

  public int serializePrefix(L left, MutableDirectBuffer dest, int offset) {
    final int cursor = serializeMember(left, leftSerializer, dest, offset);
    return cursor - offset;
  }

  @Override
  public Tuple<L, R> newInstance() {
    return instance;
  }

  @Override
  public int getLength() {
    return super.getLength(leftSerializer, rightSerializer);
  }

  @Override
  public int serialize(Tuple<L, R> value, MutableDirectBuffer dest, int offset) {
    int cursor = serializeMember(value.getLeft(), leftSerializer, dest, offset);
    cursor = serializeMember(value.getRight(), rightSerializer, dest, cursor);

    assert getLength() == VARIABLE_LENGTH || (cursor - offset) == getLength()
        : "End offset differs from expected length";
    return cursor - offset;
  }

  @Override
  public Tuple<L, R> deserialize(DirectBuffer source, int offset, int length) {
    final Tuple<L, R> tuple = newInstance();
    int cursor = deserializeMember(tuple::setLeft, leftSerializer, source, offset);
    cursor = deserializeMember(tuple::setRight, rightSerializer, source, cursor);

    assert (cursor - offset) == length : "End offset differs from length";
    return tuple;
  }
}
