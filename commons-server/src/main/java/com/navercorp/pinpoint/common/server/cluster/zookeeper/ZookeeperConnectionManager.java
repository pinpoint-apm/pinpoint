/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import com.navercorp.pinpoint.common.util.Assert;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class ZookeeperConnectionManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String hostPort;
    private final int sessionTimeout;
    private final ZookeeperEventWatcher zookeeperEventWatcher;

    private volatile ZooKeeper zookeeper;

    public ZookeeperConnectionManager(String hostPort, int sessionTimeout, ZookeeperEventWatcher zookeeperEventWatcher) {
        this.hostPort = Assert.requireNonNull(hostPort, "hostPort must not be null");
        Assert.isTrue(sessionTimeout > 0, "sessionTimeout must be greater than 0");
        this.sessionTimeout = sessionTimeout;
        this.zookeeperEventWatcher = Assert.requireNonNull(zookeeperEventWatcher, "zookeeperEventWatcher must not be null");
    }

    public void start() throws IOException {
        this.zookeeper = createZookeeper();
    }

    /**
     * @return boolean whether a connected Zookeeper exists
     */
    public boolean reconnectWhenSessionExpired() {
        ZooKeeper zookeeper = this.zookeeper;
        if (zookeeper.getState().isConnected()) {
            logger.warn("ZookeeperClient.reconnectWhenSessionExpired() failed. Error: session(0x{}) is connected.", Long.toHexString(zookeeper.getSessionId()));
            return true;
        }

        logger.warn("Execute ZookeeperClient.reconnectWhenSessionExpired()(Expired session:0x{}).", Long.toHexString(zookeeper.getSessionId()));

        try {
            zookeeper.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ZooKeeper newZookeeper = null;
        try {
            newZookeeper = createZookeeper();
            this.zookeeper = newZookeeper;
        } catch (IOException e) {
            logger.warn("Failed to create new Zookeeper instance.");
        }

        return newZookeeper != null;
    }

    public void stop() {
        ZooKeeper zookeeper = this.zookeeper;
        if (zookeeper != null) {
            try {
                zookeeper.close();
            } catch (InterruptedException ignore) {
                logger.debug(ignore.getMessage(), ignore);
            }
        }
    }

    public ZooKeeper getZookeeper() {
        return this.zookeeper;
    }

    public boolean isConnected() {
        return zookeeperEventWatcher.isConnected();
    }

    private ZooKeeper createZookeeper() throws IOException {
        return new ZooKeeper(hostPort, sessionTimeout, zookeeperEventWatcher);
    }

}
