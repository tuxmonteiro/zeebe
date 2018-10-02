package io.zeebe.broker.spike;

import java.util.ArrayList;
import java.util.List;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.ColumnFamilyHandle;
import io.zeebe.logstreams.state.StateController;
import io.zeebe.util.ReflectUtil;

public class MultiValueStoreImpl<K extends DbKey, V extends DbValue> implements MultiValueStore<K, V> {

  private static final byte[] EMPTY_ARRAY = new byte[1];

  private final StateController stateController;
  private final ColumnFamilyHandle columnFamily;

  private final ExpandableArrayBuffer keyBuffer = new ExpandableArrayBuffer();
  private final UnsafeBuffer valueArrayWrapper = new UnsafeBuffer(0, 0);

  private final Class<V> valueClass;

  public MultiValueStoreImpl(StateController stateController, ColumnFamilyHandle columnFamily, Class<K> keyClass, Class<V> valueClass) {
    this.stateController = stateController;
    this.columnFamily = columnFamily;
    this.valueClass = valueClass;
  }


  @Override
  public List<V> get(DbKey key) {
    final int keyLength = key.getLength();
    key.write(keyBuffer, 0);

    // TODO: copy can certainly be avoided
    final byte[] keyPrefix = new byte[keyLength];
    keyBuffer.getBytes(0, keyPrefix);

    final List<V> values = new ArrayList<>();
    stateController.whileEqualPrefix(columnFamily, keyPrefix, (k, v) -> {
      final V value = ReflectUtil.newInstance(valueClass);
      valueArrayWrapper.wrap(k, keyLength, k.length - keyLength);
      value.wrap(valueArrayWrapper, 0, v.length);
      values.add(value);
    });

    return values;
  }

  @Override
  public void add(K key, V value) {
    final int keyLength = key.getLength();
    key.write(keyBuffer, 0);

    final int valueLength = value.getLength();
    value.write(keyBuffer, keyLength);

    stateController.put(
        columnFamily,
        keyBuffer.byteArray(),
        0,
        keyLength + valueLength,
        EMPTY_ARRAY,
        0,
        EMPTY_ARRAY.length);
  }

}
