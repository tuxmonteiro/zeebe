package io.zeebe.logstreams.rocksdb;

import java.util.List;
import org.rocksdb.ColumnFamilyHandle;

@FunctionalInterface
public interface ZbStateSupplier<S extends ZbState> {
  S get(
      ZbRocksDb db,
      List<ColumnFamilyHandle> handles,
      List<ZbStateColumnDescriptor> columnDescriptors);
}
