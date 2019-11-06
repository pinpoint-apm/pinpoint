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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
class PinpointZookeeperConnectionStateListener implements ConnectionStateListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicBoolean connected = new AtomicBoolean(false);

    private final ZookeeperEventWatcher zookeeperEventWatcher;

    public PinpointZookeeperConnectionStateListener(ZookeeperEventWatcher zookeeperEventWatcher) {
        this.zookeeperEventWatcher = Objects.requireNonNull(zookeeperEventWatcher, "zookeeperEventWatcher");
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState.isConnected()) {
            boolean changed = connected.compareAndSet(false, true);
            if (changed) {
                logger.info("handleConnected() started.");
                boolean result = zookeeperEventWatcher.handleConnected();
                logger.info("handleConnected() completed. result:{}", result);
            }
        } else {
            boolean changed = connected.compareAndSet(true, false);
            if (changed) {
                logger.info("handleDisconnected() started.");
                boolean result = zookeeperEventWatcher.handleDisconnected();
                logger.info("handleDisconnected() completed. result:{}", result);
            }
        }
    }

}
