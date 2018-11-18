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

import static org.assertj.core.api.Assertions.assertThat;

import io.zeebe.logstreams.rocksdb.Serializer;
import io.zeebe.logstreams.rocksdb.TestUnpackedObject;
import io.zeebe.util.buffer.BufferUtil;
import io.zeebe.util.collection.Triple;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.Test;

public class TripleSerializerTest {
  @Test
  public void shouldSerializeAndDeserialize() {
    // given
    final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
    final Triple<Long, DirectBuffer, TestUnpackedObject> original =
        new Triple<>(
            1L,
            BufferUtil.wrapString("Max Mustermann"),
            new TestUnpackedObject().setKey(3L).setName("John Smith"));
    final Serializer<Triple<Long, DirectBuffer, TestUnpackedObject>> serializer =
        new TripleSerializer<>(
            new LongSerializer(),
            new DirectBufferSerializer(),
            new TestUnpackedObject.Serializer());

    // when
    final DirectBuffer serialized = serializer.serialize(original, buffer);
    final Triple<Long, DirectBuffer, TestUnpackedObject> deserialized =
        serializer.deserialize(serialized);

    // then
    assertThat(deserialized).isEqualToComparingOnlyGivenFields(original, "left", "middle", "right");
  }

  @Test
  public void shouldSerializePrefixLeft() {
    // given
    final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
    final Triple<Long, DirectBuffer, TestUnpackedObject> data =
        new Triple<>(
            1L,
            BufferUtil.wrapString("Max Mustermann"),
            new TestUnpackedObject().setKey(3L).setName("John Smith"));
    final TripleSerializer<Long, DirectBuffer, TestUnpackedObject> serializer =
        new TripleSerializer<>(
            new LongSerializer(),
            new DirectBufferSerializer(),
            new TestUnpackedObject.Serializer());

    // when
    final DirectBuffer serialized = serializer.serialize(data, buffer, 0);
    final DirectBuffer prefix = serializer.serializePrefix(1L, buffer, serialized.capacity());

    // then
    assertThat(BufferUtil.startsWith(serialized, prefix)).isTrue();
  }

  @Test
  public void shouldSerializePrefixLeftMiddle() {
    // given
    final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
    final Triple<Long, DirectBuffer, TestUnpackedObject> data =
        new Triple<>(
            1L,
            BufferUtil.wrapString("Max Mustermann"),
            new TestUnpackedObject().setKey(3L).setName("John Smith"));
    final TripleSerializer<Long, DirectBuffer, TestUnpackedObject> serializer =
        new TripleSerializer<>(
            new LongSerializer(),
            new DirectBufferSerializer(),
            new TestUnpackedObject.Serializer());

    // when
    final DirectBuffer serialized = serializer.serialize(data, buffer, 0);
    final DirectBuffer prefix =
        serializer.serializePrefix(
            1L, BufferUtil.wrapString("Max Mustermann"), buffer, serialized.capacity());

    // then
    assertThat(BufferUtil.startsWith(serialized, prefix)).isTrue();
  }
}
