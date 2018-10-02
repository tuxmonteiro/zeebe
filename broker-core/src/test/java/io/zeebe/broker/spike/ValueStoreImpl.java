package io.zeebe.broker.spike;

import java.util.function.BiConsumer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import io.zeebe.logstreams.state.StateController;
import io.zeebe.util.ReflectUtil;

public class ValueStoreImpl<K extends DbKey, V extends DbValue> implements ValueStore<K, V> {

  private final StateController stateController;
  private final ColumnFamilyHandle columnFamily;

  private final ExpandableArrayBuffer keyBuffer = new ExpandableArrayBuffer();
  private final ExpandableArrayBuffer valueBuffer = new ExpandableArrayBuffer();
  private final UnsafeBuffer keyArrayWrapper = new UnsafeBuffer(0, 0);
  private final UnsafeBuffer valueArrayWrapper = new UnsafeBuffer(0, 0);

  private final K key;
  private final Class<V> valueClass;

  public ValueStoreImpl(StateController stateController, ColumnFamilyHandle columnFamily, Class<K> keyClass, Class<V> valueClass) {
    this.stateController = stateController;
    this.columnFamily = columnFamily;
    this.key = ReflectUtil.newInstance(keyClass);
    this.valueClass = valueClass;
  }

  @Override
  public void put(K key, V value) {
    final int keyLength = key.getLength();
    key.write(keyBuffer, 0);

    final int valueLength = value.getLength();
    value.write(valueBuffer, 0);

    stateController.put(
        columnFamily,
        keyBuffer.byteArray(),
        0,
        keyLength,
        valueBuffer.byteArray(),
        0,
        valueLength);
  }

  @Override
  public V get(K key) {
    final int keyLength = key.getLength();
    key.write(keyBuffer, 0);

    final int valueLength = stateController.get(columnFamily,
        keyBuffer.byteArray(),
        0,
        keyLength,
        valueBuffer.byteArray(),
        0,
        valueBuffer.capacity());

    if (valueLength == RocksDB.NOT_FOUND)
    {
      return null;
    }
    else if (valueLength > valueBuffer.capacity())
    {
      valueBuffer.checkLimit(valueLength);
      stateController.get(columnFamily,
          keyBuffer.byteArray(),
          0,
          keyLength,
          valueBuffer.byteArray(),
          0,
          valueBuffer.capacity());
    }


    final V value = ReflectUtil.newInstance(valueClass);
    value.wrap(valueBuffer, 0, valueLength);
    return value;
  }

  public void forEach(BiConsumer<K, V> consumer)
  {
    stateController.foreach(columnFamily, (k, v) ->
    {
      consumeKeyValuePair(consumer, k, v);
    });
  }

  private void consumeKeyValuePair(BiConsumer<K, V> consumer, byte[] k, byte[] v) {
    keyArrayWrapper.wrap(k);
    valueArrayWrapper.wrap(v);

    key.wrap(keyArrayWrapper, 0, k.length);
    final V value = ReflectUtil.newInstance(valueClass);
    value.wrap(valueArrayWrapper, 0, v.length);

    consumer.accept(key, value);
  }

  @Override
  public void forEach(DbKeyPrefix<K> prefix, BiConsumer<K, V> consumer) {
    final int prefixLength = prefix.getLength();
    prefix.write(keyBuffer, 0);

    // can probably be made allocation- and copy-free
    final byte[] prefixArray = new byte[prefixLength];
    keyBuffer.getBytes(0, prefixArray);

    stateController.whileEqualPrefix(columnFamily, prefixArray, (k, v) -> {
      consumeKeyValuePair(consumer, k, v);
    });
  }

}
