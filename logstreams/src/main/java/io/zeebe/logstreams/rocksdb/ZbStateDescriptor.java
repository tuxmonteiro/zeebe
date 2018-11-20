package io.zeebe.logstreams.rocksdb;

import java.util.List;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;

public class ZbStateDescriptor<S extends ZbState> {
  private final byte[] prefix;
  private final ZbStateSupplier<S> stateSupplier;

  public ZbStateDescriptor(byte[] prefix, ZbStateSupplier<S> stateSupplier, ZbStateColumnDescriptorSupplier<S>) {
    this.prefix = prefix;
    this.stateSupplier = stateSupplier;
  }

  public S get(ZbRocksDb db, List<ColumnFamilyHandle> handles) {
    return stateSupplier.get(db, handles);
  }

  public List<ColumnFamilyDescriptor> getColumnFamilyDescriptors() {
    
  }
}
