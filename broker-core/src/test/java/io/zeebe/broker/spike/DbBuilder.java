package io.zeebe.broker.spike;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.rocksdb.ColumnFamilyHandle;
import io.zeebe.logstreams.state.StateController;

public class DbBuilder {

  private final File dbDirectory;
  private final StateController stateController = new StateController();
  private final List<String> columnFamilyNames = new ArrayList<>();

  public DbBuilder(File dbDirectory)
  {
    this.dbDirectory = dbDirectory;
  }

  public void declareStore(String name)
  {
    columnFamilyNames.add(name);
  }

  public <K extends DbKey, V extends DbValue> ValueStore<K, V> getValueStore(String name, Class<K> keyClass, Class<V> valueClass)
  {
    final ColumnFamilyHandle columnFamily = stateController.getColumnFamilyHandle(name.getBytes());
    return new ValueStoreImpl<>(stateController, columnFamily, keyClass, valueClass);
  }

  public <K extends DbKey, V extends DbValue> MultiValueStore<K, V> getMultiValueStore(String name, Class<K> keyClass, Class<V> valueClass)
  {
    final ColumnFamilyHandle columnFamily = stateController.getColumnFamilyHandle(name.getBytes());
    return new MultiValueStoreImpl<>(stateController, columnFamily, keyClass, valueClass);
  }

  public <K extends DbKey, V extends DbValue> Sequence getSequence(String name, long initialValue, int stepSize)
  {
    return null;
  }

  /**
   * Can no longer declare anything after calling this method.
   * Can only use the declared objects after calling this method.
   */
  public void open()
  {
    final List<byte[]> byteArrayNames = columnFamilyNames.stream().map(s -> s.getBytes())
        .collect(Collectors.toList());

    try {
      stateController.open(dbDirectory, false, byteArrayNames);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
