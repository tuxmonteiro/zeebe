package io.zeebe.logstreams.rocksdb;

import io.zeebe.util.collection.Tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDBException;

@SuppressWarnings("unchecked")
public class ZbStateDb implements AutoCloseable {
  private final List<ZbStateDescriptor> descriptors;
  private final List<ZbState> states;
  private final DBOptions defaultOptions;

  private ZbRocksDb db;
  private DBOptions options;

  public ZbStateDb(DBOptions options, ZbStateDescriptor... descriptors) {
    this.defaultOptions = options;
    this.descriptors = Arrays.asList(descriptors);
    this.states = new ArrayList<>(descriptors.length);
  }

  public void open(String path, boolean reopen) {
    final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
    final List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
    final List<Tuple<Integer, Integer>> indexRanges = new ArrayList<>();
    options = new DBOptions(defaultOptions).setCreateIfMissing(!reopen);

    int lastIndex = 0;
    for (final ZbStateDescriptor<?> descriptor : descriptors) {
      final List<ColumnFamilyDescriptor> stateColumnFamilyDescriptors =
          descriptor.getColumnFamilyDescriptors();
      indexRanges.add(new Tuple<>(lastIndex, lastIndex + stateColumnFamilyDescriptors.size()));
      columnFamilyDescriptors.addAll(stateColumnFamilyDescriptors);
      lastIndex += stateColumnFamilyDescriptors.size();
    }

    try {
      db = ZbRocksDb.open(options, path, columnFamilyDescriptors, columnFamilyHandles);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }

    for (int i = 0; i < descriptors.size(); i++) {
      final Tuple<Integer, Integer> range = indexRanges.get(i);
      final List<ColumnFamilyHandle> handles =
          columnFamilyHandles.subList(range.getLeft(), range.getRight());
      states.add(i, descriptors.get(i).get(db, handles));
    }
  }

  @Override
  public void close() throws Exception {
    states.forEach(ZbState::close);

    if (options != null) {
      options.close();
    }

    if (db != null) {
      db.close();
    }

    defaultOptions.close();
  }
}
