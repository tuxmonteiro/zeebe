package io.zeebe.logstreams.rocksdb;

import java.util.ArrayList;
import java.util.List;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDBException;

@SuppressWarnings("unchecked")
public abstract class ZbStateDb implements AutoCloseable {
  private final List<ZbStateDescriptor> stateDescriptors;
  private final List<ZbState> states;
  private final DBOptions defaultOptions;

  private ZbRocksDb db;
  private DBOptions options;

  public ZbStateDb() {
    this.defaultOptions = createOptions();

    this.stateDescriptors = getStateDescriptors();
    assert this.stateDescriptors != null && !this.stateDescriptors.isEmpty()
        : "no states described for state database";

    this.states = new ArrayList<>(this.stateDescriptors.size());
  }

  public void open(String path, boolean reopen) {
    final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
    final List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
    final List<IndexRange> indexRanges = new ArrayList<>();
    options = new DBOptions(defaultOptions).setCreateIfMissing(!reopen);

    buildColumnFamilyDescriptorList(columnFamilyDescriptors, indexRanges);
    openDatabase(path, columnFamilyDescriptors, columnFamilyHandles);
    createStates(columnFamilyHandles, indexRanges);
  }

  @Override
  public void close() {
    states.forEach(ZbState::close);

    if (options != null) {
      options.close();
    }

    if (db != null) {
      db.close();
    }

    defaultOptions.close();
  }

  protected abstract DBOptions createOptions();

  protected abstract List<ZbStateDescriptor> getStateDescriptors();

  private void openDatabase(
      String path,
      List<ColumnFamilyDescriptor> columnFamilyDescriptors,
      List<ColumnFamilyHandle> columnFamilyHandles) {
    try {
      db = ZbRocksDb.open(options, path, columnFamilyDescriptors, columnFamilyHandles);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  private void createStates(
      List<ColumnFamilyHandle> columnFamilyHandles, List<IndexRange> indexRanges) {
    for (int i = 0; i < stateDescriptors.size(); i++) {
      final IndexRange range = indexRanges.get(i);
      final List<ColumnFamilyHandle> handles = columnFamilyHandles.subList(range.begin, range.end);
      final ZbStateDescriptor descriptor = stateDescriptors.get(i);
      states.add(stateDescriptors.get(i).get(db, handles));
    }
  }

  private void buildColumnFamilyDescriptorList(
      List<ColumnFamilyDescriptor> combinedColumnFamilyDescriptors, List<IndexRange> indexRanges) {
    int lastIndex = 0;

    for (final ZbStateDescriptor<?> descriptor : stateDescriptors) {
      final List<ColumnFamilyDescriptor> columnFamilyDescriptors =
          descriptor.getColumnFamilyDescriptors();
      final int descriptorsCount = columnFamilyDescriptors.size();
      indexRanges.add(new IndexRange(lastIndex, lastIndex + descriptorsCount));
      combinedColumnFamilyDescriptors.addAll(columnFamilyDescriptors);
      lastIndex += descriptorsCount;
    }
  }

  static class IndexRange {
    final int begin;
    final int end;

    IndexRange(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }
  }
}
