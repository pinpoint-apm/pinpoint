/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
class PinpointZookeeperConnectionStateListener implements ConnectionStateListener {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AtomicBoolean connected = new AtomicBoolean(false);

    private final AtomicReference<ConnectionState> currentState = new AtomicReference<>();

    private final String objectId;
    private final ZookeeperEventWatcher zookeeperEventWatcher;

    public PinpointZookeeperConnectionStateListener(ZookeeperEventWatcher zookeeperEventWatcher) {
        this.zookeeperEventWatcher = Objects.requireNonNull(zookeeperEventWatcher, "zookeeperEventWatcher");

        final UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        if (uuidString.length() > 8) {
            objectId = uuidString.substring(0, 8);
        } else {
            objectId = uuidString;
        }
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState.isConnected()) {
            boolean changed = connected.compareAndSet(false, true);
            logger.info("{} handleConnected() executed. newState:{}", objectId, newState);
            if (changed) {
                boolean result = zookeeperEventWatcher.handleConnected();
                logger.info("{} handleConnected() completed. result:{}", objectId, result);
            }
        } else {
            boolean changed = connected.compareAndSet(true, false);
            logger.info("{} handleDisconnected() executed. newState:{}", objectId, newState);
            if (changed) {
                boolean result = zookeeperEventWatcher.handleDisconnected();
                logger.info("{} handleDisconnected() completed. result:{}", objectId, result);
            }
        }
        currentState.set(newState);
    }

    public ConnectionState getCurrentState() {
        return currentState.get();
    }
}
