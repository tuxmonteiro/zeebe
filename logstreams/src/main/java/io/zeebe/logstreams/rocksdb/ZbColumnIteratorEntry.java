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
package io.zeebe.logstreams.rocksdb;

import org.agrona.DirectBuffer;

public class ZbColumnIteratorEntry<K, V> extends ZbColumnEntry<K, V> {
  private DirectBuffer keyBuffer;
  private DirectBuffer valueBuffer;

  public ZbColumnIteratorEntry(ZbColumn<K, V> column) {
    super(column);
  }

  public void set(DirectBuffer keyBuffer, DirectBuffer valueBuffer) {
    setKeyBuffer(keyBuffer);
    setValueBuffer(valueBuffer);
  }

  public void setKeyBuffer(DirectBuffer keyBuffer) {
    this.keyBuffer = keyBuffer;
    setKey(keyBuffer, 0, keyBuffer.capacity());
  }

  public DirectBuffer getKeyBuffer() {
    return keyBuffer;
  }

  public void setValueBuffer(DirectBuffer valueBuffer) {
    this.valueBuffer = valueBuffer;
    setKey(valueBuffer, 0, valueBuffer.capacity());
  }

  public DirectBuffer getValueBuffer() {
    return valueBuffer;
  }
}
