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

import java.util.List;
import org.rocksdb.DBOptions;

public abstract class ZbState {
  //  private final ZbRocksDb db;
  //  private final List<ZbColumn<?, ?>> columns;
  //
  //  public ZbState(String path, ) {
  //    final List<ZbColumnDescriptor<?, ?>> descriptors = getColumnFamilyDescriptors();
  //    final List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>(descriptors.size());
  //
  //    final byte[][] mapped
  //
  //    try {
  //      db = ZbRocksDb.open(path, getDBOptions(), descriptors, columnFamilyHandles);
  //    } catch (RocksDBException e) {
  //      throw new RuntimeException(e);
  //    }
  //
  //    columns = new ArrayList<>(descriptors.size());
  //    for (int i = 0; i < columnFamilyHandles.size(); i++) {}
  //  }

  protected abstract List<ZbColumnDescriptor<?, ?>> getColumnFamilyDescriptors();

  protected DBOptions getDBOptions() {
    return new DBOptions();
  }
}
