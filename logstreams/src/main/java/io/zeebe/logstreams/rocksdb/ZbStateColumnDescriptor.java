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

import io.zeebe.util.ByteArrayUtil;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;

public class ZbStateColumnDescriptor<S extends ZbState, T extends ZbColumn> {
  private final byte[] name;
  private final ColumnFamilyOptions options;
  private final ZbStateColumnSupplier<S, T> columnSupplier;

  public ZbStateColumnDescriptor(
      byte[] name, ColumnFamilyOptions options, ZbStateColumnSupplier<S, T> columnSupplier) {
    this.name = name;
    this.options = options;
    this.columnSupplier = columnSupplier;
  }

  public T get(S state, ZbRocksDb db, ColumnFamilyHandle handle) {
    return columnSupplier.get(state, db, handle);
  }

  public ColumnFamilyDescriptor getColumnFamilyDescriptor(byte[] prefix) {
    final byte[] prefixedName = ByteArrayUtil.concat(prefix, name);
    return new ColumnFamilyDescriptor(prefixedName, options);
  }
}
