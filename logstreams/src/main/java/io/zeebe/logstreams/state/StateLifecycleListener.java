package io.zeebe.logstreams.state;

import io.zeebe.logstreams.rocksdb.ZbRocksDb;
import java.util.Map;
import org.rocksdb.ColumnFamilyHandle;

public interface StateLifecycleListener {

  default void onOpenedDb(ZbRocksDb rocksDb, Map<byte[], ColumnFamilyHandle> handles) {}

  default void onCloseDb() {}

  default void onClosedDb() {}
}
