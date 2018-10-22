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
package io.zeebe.broker.workflow.deployment.distribute.processor.state;

import io.zeebe.broker.util.KeyState;
import io.zeebe.broker.workflow.deployment.distribute.processor.PendingDeploymentDistribution;
import io.zeebe.logstreams.state.PersistedLong;
import io.zeebe.logstreams.state.State;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import org.agrona.concurrent.UnsafeBuffer;

public class DeploymentsState {

  private static final byte[] PENDING_DEPLOYMENT_COLUMN_FAMILY = "pendingDeployment".getBytes();

  private final PendingDeploymentDistribution pendingDeploymentDistribution;
  private final PersistedLong persistedLong = new PersistedLong();

  private final KeyState keyState;
  private final State pendingDeploymentState;

  public DeploymentsState() {
    pendingDeploymentDistribution = new PendingDeploymentDistribution(new UnsafeBuffer(0, 0), -1);
    keyState = new KeyState();
    pendingDeploymentState = new State(PENDING_DEPLOYMENT_COLUMN_FAMILY);
  }

  public List<State> getStates() {
    return Arrays.asList(keyState.getKeyState(), pendingDeploymentState);
  }

  public void putPendingDeployment(
      final long key, final PendingDeploymentDistribution pendingDeploymentDistribution) {
    pendingDeploymentState.put(key, pendingDeploymentDistribution);
  }

  private PendingDeploymentDistribution getPending(final long key) {
    return pendingDeploymentState.get(key, PendingDeploymentDistribution.class);
  }

  public PendingDeploymentDistribution getPendingDeployment(final long key) {
    return getPending(key);
  }

  public PendingDeploymentDistribution removePendingDeployment(final long key) {
    final PendingDeploymentDistribution pending = getPending(key);
    if (pending != null) {
      pendingDeploymentState.delete(key);
    }

    return pending;
  }

  public void foreachPending(final BiConsumer<Long, PendingDeploymentDistribution> consumer) {
    pendingDeploymentState.foreach(
        persistedLong,
        pendingDeploymentDistribution,
        (persistedLong, pendingDeploymentDistribution) -> {
          consumer.accept(persistedLong.getValue(), pendingDeploymentDistribution);
        });
  }
}
