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
package io.zeebe.broker.util;

import static io.zeebe.util.StringUtil.getBytes;

import io.zeebe.logstreams.state.State;
import io.zeebe.logstreams.state.StateLifecycleListener;

public class KeyState implements StateLifecycleListener {
  private static final byte[] KEY_HANDLE_NAME = getBytes("keyColumn");
  private static final byte[] NEXT_KEY_BUFFER = getBytes("nextKey");

  private final State keyState;

  public KeyState() {
    keyState = new State(KEY_HANDLE_NAME);
  }

  public State getKeyState() {
    return keyState;
  }

  public long getNextKey() {
    return keyState.getLong(NEXT_KEY_BUFFER, 0, NEXT_KEY_BUFFER.length);
  }

  public void putNextKey(long key) {
    keyState.putLong(NEXT_KEY_BUFFER, 0, NEXT_KEY_BUFFER.length, key);
  }
}
