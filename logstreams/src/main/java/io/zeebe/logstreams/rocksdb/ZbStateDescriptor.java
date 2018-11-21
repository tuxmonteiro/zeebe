package io.zeebe.logstreams.rocksdb;

import java.util.ArrayList;
import java.util.List;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;

public class ZbStateDescriptor<S extends ZbState> {
  private final ZbStateSupplier<S> stateSupplier;
  private final List<ZbStateColumnDescriptor> stateColumnDescriptors;

  public ZbStateDescriptor(
      ZbStateSupplier<S> stateSupplier, List<ZbStateColumnDescriptor> stateColumnDescriptors) {
    this.stateSupplier = stateSupplier;
    this.stateColumnDescriptors = stateColumnDescriptors;
  }

  public List<ColumnFamilyDescriptor> getColumnFamilyDescriptors() {
    final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
    for (final ZbStateColumnDescriptor stateColumnDescriptor : stateColumnDescriptors) {
      columnFamilyDescriptors.add(stateColumnDescriptor.getColumnFamilyDescriptor());
    }

    return columnFamilyDescriptors;
  }

  public S get(ZbRocksDb db, List<ColumnFamilyHandle> handles) {
    return stateSupplier.get(db, handles, stateColumnDescriptors);
  }
}
