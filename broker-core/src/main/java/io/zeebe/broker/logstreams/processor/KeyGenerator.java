/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.logstreams.processor;

import io.zeebe.broker.util.KeyState;
import io.zeebe.msgpack.UnpackedObject;
import io.zeebe.msgpack.property.LongProperty;
import io.zeebe.protocol.Protocol;

public class KeyGenerator extends UnpackedObject {

  /*
   * Making sure these entities get unique keys on the same partition
   */
  private static final int STEP_SIZE = 5;
  private static final int WF_OFFSET = 1;
  private static final int JOB_OFFSET = 2;
  private static final int INCIDENT_OFFSET = 3;
  private static final int MESSAGE_OFFSET = 4;

  private final LongProperty nextKey;
  private final int stepSize;
  private final KeyState keyState;

  public KeyGenerator(int partitionId, final long initialValue, final int stepSize) {
    this(partitionId, initialValue, stepSize, null);
  }

  public KeyGenerator(
      int partitionId, final long initialValue, final int stepSize, final KeyState keyState) {
    long startValue = (long) partitionId << Protocol.KEY_BITS;
    startValue += initialValue;

    nextKey = new LongProperty("nextKey", startValue);
    this.stepSize = stepSize;
    declareProperty(nextKey);
    this.keyState = keyState;
    init(startValue);
  }

  private void init(long initialValue) {
    if (keyState != null) {
      keyState.addOnOpenCallback(
          () -> {
            final long latestKey = keyState.getNextKey();
            if (latestKey > 0) {
              setKey(latestKey);
            } else {
              keyState.putNextKey(initialValue);
            }
          });
    }
  }

  public long nextKey() {
    final long key = nextKey.getValue();
    nextKey.setValue(key + stepSize);
    putLatestKeyIntoController(key + stepSize);
    return key;
  }

  public void setKey(final long key) {
    final long nextKey = key + stepSize;
    this.nextKey.setValue(nextKey);
    putLatestKeyIntoController(nextKey);
  }

  private void putLatestKeyIntoController(final long key) {
    if (keyState != null) {
      keyState.putNextKey(key);
    }
  }

  public static KeyGenerator createWorkflowInstanceKeyGenerator(
      int partitionId, final KeyState controller) {
    return new KeyGenerator(partitionId, WF_OFFSET, STEP_SIZE, controller);
  }

  public static KeyGenerator createJobKeyGenerator(int partitionId, final KeyState keyState) {
    return new KeyGenerator(partitionId, JOB_OFFSET, STEP_SIZE, keyState);
  }

  public static KeyGenerator createIncidentKeyGenerator(int partitionId) {
    return new KeyGenerator(partitionId, INCIDENT_OFFSET, STEP_SIZE);
  }

  public static KeyGenerator createMessageKeyGenerator(int partitionId, KeyState keyState) {
    return new KeyGenerator(partitionId, MESSAGE_OFFSET, STEP_SIZE, keyState);
  }
}
