package io.zeebe.broker.spike;

import java.util.List;

public interface MultiValueStore<K extends DbKey, V extends DbValue> {

  /*
   * or better Iterator<V>
   */
  List<V> get(K key);

  void add(K key, V value);
}
