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

import java.util.ArrayList;
import java.util.List;
import org.rocksdb.ColumnFamilyHandle;

public class ZbState implements AutoCloseable {
  protected final List<ZbColumn> columns;
  protected final ZbRocksDb db;

  public ZbState(
      ZbRocksDb db,
      List<ColumnFamilyHandle> handles,
      List<ZbStateColumnDescriptor> columnDescriptors) {
    this.db = db;
    this.columns = new ArrayList<>(handles.size());

    for (int i = 0; i < handles.size(); i++) {
      final ZbStateColumnDescriptor descriptor = columnDescriptors.get(i);
      final ColumnFamilyHandle handle = handles.get(i);
      columns.add(i, descriptor.get(this, db, handle));
    }
  }

  @Override
  public void close() {
    columns.forEach(ZbColumn::close);
  }
}
