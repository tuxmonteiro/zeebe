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

import io.zeebe.util.collection.Triple;
import io.zeebe.util.collection.Tuple;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class TripleSerializer<L, M, R> extends Composite implements Serializer<Triple<L, M, R>> {
  private final Triple<L, M, R> instance = new Triple<>();
  private final TupleSerializer<L, M> tupleSerializer;
  private final Serializer<R> rightSerializer;

  public TripleSerializer(
      Serializer<L> leftSerializer, Serializer<M> middleSerializer, Serializer<R> rightSerializer) {
    this.tupleSerializer = new TupleSerializer<>(leftSerializer, middleSerializer);
    this.rightSerializer = rightSerializer;
  }

  public int serializePrefix(L left, MutableDirectBuffer dest, int offset) {
    return tupleSerializer.serializePrefix(left, dest, offset);
  }

  public DirectBuffer serializePrefixInto(
      L left, MutableDirectBuffer dest, int offset, DirectBuffer view) {
    return tupleSerializer.serializePrefixInto(left, dest, offset, view);
  }

  public int serializePrefix(L left, M middle, MutableDirectBuffer dest, int offset) {
    return serializePrefix(getTuple(left, middle), dest, offset);
  }

  public DirectBuffer serializePrefixInto(
      L left, M middle, MutableDirectBuffer dest, int offset, DirectBuffer view) {
    return serializePrefixInto(getTuple(left, middle), dest, offset, view);
  }

  public int serializePrefix(Tuple<L, M> tuple, MutableDirectBuffer dest, int offset) {
    return tupleSerializer.serialize(tuple, dest, offset);
  }

  public DirectBuffer serializePrefixInto(
      Tuple<L, M> tuple, MutableDirectBuffer dest, int offset, DirectBuffer view) {
    return tupleSerializer.serializeInto(tuple, dest, offset, view);
  }

  @Override
  public Triple<L, M, R> newInstance() {
    return instance;
  }

  @Override
  public int getLength() {
    return super.getLength(tupleSerializer, rightSerializer);
  }

  @Override
  public int serialize(Triple<L, M, R> value, MutableDirectBuffer dest, int offset) {
    final Tuple<L, M> tuple = getTuple(value.getLeft(), value.getMiddle());
    int cursor = serializeMember(tuple, tupleSerializer, dest, offset);
    cursor = serializeMember(value.getRight(), rightSerializer, dest, cursor);

    assert getLength() == VARIABLE_LENGTH || (cursor - offset) == getLength()
        : "End offset differs from expected length";
    return cursor - offset;
  }

  @Override
  public Triple<L, M, R> deserialize(DirectBuffer source, int offset, int length) {
    final Triple<L, M, R> triple = newInstance();
    int cursor = deserializeMember(triple::setTuple, tupleSerializer, source, offset);
    cursor = deserializeMember(triple::setRight, rightSerializer, source, cursor);

    assert (cursor - offset) == length : "End offset differs from length";
    return triple;
  }

  private Tuple<L, M> getTuple(L left, M middle) {
    final Tuple<L, M> tuple = tupleSerializer.newInstance();
    tuple.setLeft(left);
    tuple.setRight(middle);

    return tuple;
  }
}
