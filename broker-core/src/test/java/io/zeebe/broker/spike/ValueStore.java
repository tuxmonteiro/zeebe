package io.zeebe.broker.spike;

import java.util.function.BiConsumer;

public interface ValueStore<K extends DbKey, V extends DbValue> {

  void put(K key, V value);

  V get(K key);

  void forEach(BiConsumer<K, V> consumer);

  // TODO: braucht man das hier dann überhaupt?
  void forEach(DbKeyPrefix<K> prefix, BiConsumer<K, V> consumer);

}
