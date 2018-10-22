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
package io.zeebe.logstreams.state;

import io.zeebe.logstreams.impl.Loggers;
import io.zeebe.logstreams.rocksdb.ZbRocksDb;
import io.zeebe.util.ByteValue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.agrona.CloseHelper;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.ChecksumType;
import org.rocksdb.ClockCache;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Env;
import org.rocksdb.Filter;
import org.rocksdb.MemTableConfig;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.SkipListMemTableConfig;
import org.rocksdb.TableFormatConfig;
import org.slf4j.Logger;

/**
 * Controls opening, closing, and managing of RocksDB associated resources. Could be argued that db
 * reference should not be made transparent, as it could be closed on its own elsewhere, but for now
 * it's easier.
 *
 * <p>Current suggested method of customizing RocksDB instance per stream processor is to subclass
 * this class and to override the protected methods to your liking.
 *
 * <p>Another option would be to use a Builder class and make StateController entirely controlled
 * through its properties.
 */
public class StateController implements AutoCloseable {
  private static final Logger LOG = Loggers.ROCKSDB_LOGGER;

  protected final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
  protected final List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
  protected final List<AutoCloseable> closeables = new ArrayList<>();

  private final Map<DirectBuffer, State> stateMap = new HashMap<>();
  private final DirectBuffer columnFamilyNameBuffer = new UnsafeBuffer(0, 0);

  private boolean isOpened = false;
  private ZbRocksDb db;
  protected File dbDirectory;

  public ZbRocksDb open(final File dbDirectory, final boolean reopen) {
    if (!isOpened) {
      try {
        this.dbDirectory = dbDirectory;
        final ColumnFamilyOptions columnFamilyOptions = createColumnFamilyOptions();
        createFamilyDescriptors(stateMap.keySet(), columnFamilyOptions);

        final DBOptions dbOptions = createDbOptions(reopen);
        openDatabase(dbDirectory, dbOptions);

        for (ColumnFamilyHandle handle : columnFamilyHandles) {
          final State state = getState(handle.getName());
          if (state != null) {
            state.onOpenedDb(db, handle);
          }
        }

      } catch (final Exception ex) {
        close();
        throw new RuntimeException(ex);
      }

      LOG.trace("Opened RocksDB {}", this.dbDirectory);
    }

    return db;
  }

  private DBOptions createDbOptions(boolean reopen) {
    final DBOptions dbOptions =
        new DBOptions()
            .setEnv(getDbEnv())
            .setCreateMissingColumnFamilies(!reopen)
            .setErrorIfExists(!reopen)
            .setCreateIfMissing(!reopen);

    closeables.add(dbOptions);
    return dbOptions;
  }

  private void openDatabase(File dbDirectory, DBOptions dbOptions) throws RocksDBException {
    db =
        ZbRocksDb.open(
            dbOptions, dbDirectory.getAbsolutePath(), columnFamilyDescriptors, columnFamilyHandles);

    closeables.add(db);
    isOpened = true;
  }

  private State getState(byte[] name) {
    columnFamilyNameBuffer.wrap(name);
    return stateMap.get(columnFamilyNameBuffer);
  }

  private void createFamilyDescriptors(
      Set<DirectBuffer> columnFamilyNames, ColumnFamilyOptions columnFamilyOptions) {
    final ColumnFamilyDescriptor defaultFamilyDescriptor =
        new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, createColumnFamilyOptions());
    columnFamilyDescriptors.add(defaultFamilyDescriptor);

    if (columnFamilyNames != null && columnFamilyNames.size() > 0) {
      for (DirectBuffer name : columnFamilyNames) {
        final ColumnFamilyDescriptor columnFamilyDescriptor =
            new ColumnFamilyDescriptor(name.byteArray(), columnFamilyOptions);
        columnFamilyDescriptors.add(columnFamilyDescriptor);
      }
    }
  }

  protected Options createOptions() {
    final Filter filter = new BloomFilter();
    closeables.add(filter);

    final Cache cache = new ClockCache(ByteValue.ofMegabytes(16).toBytes(), 10);
    closeables.add(cache);

    final TableFormatConfig sstTableConfig =
        new BlockBasedTableConfig()
            .setBlockCache(cache)
            .setBlockSize(ByteValue.ofKilobytes(16).toBytes())
            .setChecksumType(ChecksumType.kCRC32c)
            .setFilter(filter);
    final MemTableConfig memTableConfig = new SkipListMemTableConfig();

    return new Options()
        .setEnv(getDbEnv())
        .setWriteBufferSize(ByteValue.ofMegabytes(64).toBytes())
        .setMemTableConfig(memTableConfig)
        .setTableFormatConfig(sstTableConfig);
  }

  protected ColumnFamilyOptions createColumnFamilyOptions() {
    final Filter filter = new BloomFilter();
    closeables.add(filter);

    final Cache cache = new ClockCache(ByteValue.ofMegabytes(16).toBytes(), 10);
    closeables.add(cache);

    final TableFormatConfig sstTableConfig =
        new BlockBasedTableConfig()
            .setBlockCache(cache)
            .setBlockSize(ByteValue.ofKilobytes(16).toBytes())
            .setChecksumType(ChecksumType.kCRC32c)
            .setFilter(filter);
    final MemTableConfig memTableConfig = new SkipListMemTableConfig();

    final ColumnFamilyOptions columnFamilyOptions =
        new ColumnFamilyOptions()
            .optimizeUniversalStyleCompaction()
            .setWriteBufferSize(ByteValue.ofMegabytes(64).toBytes())
            .setMemTableConfig(memTableConfig)
            .setTableFormatConfig(sstTableConfig);
    closeables.add(columnFamilyOptions);
    return columnFamilyOptions;
  }

  protected Env getDbEnv() {
    return Env.getDefault();
  }

  public boolean isOpened() {
    return isOpened;
  }

  public void delete() throws Exception {
    delete(dbDirectory);
  }

  public void delete(final File dbDirectory) throws Exception {
    final boolean shouldWeClose = isOpened && this.dbDirectory.equals(dbDirectory);
    if (shouldWeClose) {
      close();
    }

    try (Options options = createOptions()) {
      RocksDB.destroyDB(dbDirectory.toString(), options);
    }
  }

  @Override
  public void close() {
    columnFamilyHandles.forEach(CloseHelper::close);
    columnFamilyHandles.clear();
    columnFamilyDescriptors.clear();

    Collections.reverse(closeables);
    closeables.forEach(CloseHelper::close);
    closeables.clear();

    LOG.trace("Closed RocksDB {}", dbDirectory);
    dbDirectory = null;
    isOpened = false;
  }

  public RocksDB getDb() {
    return db;
  }

  public void register(List<State> states) {
    final Map<UnsafeBuffer, State> mapFromList =
        states
            .stream()
            .collect(
                Collectors.toMap(key -> new UnsafeBuffer(key.getColumnFamilyName()), item -> item));
    stateMap.putAll(mapFromList);
  }
}
