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

import io.zeebe.logstreams.rocksdb.serializers.DirectBufferSerializer;
import io.zeebe.logstreams.rocksdb.serializers.IntSerializer;
import io.zeebe.logstreams.rocksdb.serializers.LongSerializer;
import io.zeebe.logstreams.rocksdb.serializers.TripleSerializer;
import io.zeebe.util.collection.Triple;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.rocksdb.ColumnFamilyHandle;

public class ZbColumnTest {
  private ZbRocksDb db;
  private TestColumn column;

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setup() throws IOException {
    final File dbFolder = temporaryFolder.newFolder("db");
    final List<ZbStateColumnDescriptor> descriptors = TestState.getDescriptors("test".getBytes());

    // db = ZbRocksDb.open(dbFolder, )
  }

  static class TestColumn
      extends ZbColumn<Triple<Integer, Long, DirectBuffer>, TestUnpackedObject> {
    public TestColumn(ZbRocksDb db, ColumnFamilyHandle columnFamilyHandle) {
      super(
          db,
          columnFamilyHandle,
          new ExpandableArrayBuffer(),
          new TripleSerializer<>(
              IntSerializer.INSTANCE, LongSerializer.INSTANCE, new DirectBufferSerializer()),
          new ExpandableArrayBuffer(),
          new TestUnpackedObject.Serializer());
    }
  }
}
