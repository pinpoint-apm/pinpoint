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

import org.apache.curator.test.TestingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.TestSocketUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test compares the connection status of Curator and Zookeeper.
 *
 * @author Taejin Koo
 */
public class ConnectionTest {

    private final Logger logger = LogManager.getLogger(ConnectionTest.class);

    private ConditionFactory awaitility() {
        return Awaitility.await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .timeout(3000, TimeUnit.MILLISECONDS);
    }

    private static int zookeeperPort;
    private static TestingServer ts = null;

    @BeforeAll
    public static void setUp() throws Exception {
        zookeeperPort = TestSocketUtils.findAvailableTcpPort();
        ts = ZKServerFactory.create(zookeeperPort);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (ts != null) {
            ts.stop();
            ZKUtils.closeQuietly(ts);
        }
    }

    // If the Instance of ZookeeperServer is changed, Zookeeper will not automatically reconnect.
    @Test
    public void zookeeperExpiredTest() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(ts.getConnectString(), 5000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                logger.info("process:{}", watchedEvent);
            }
        });

        try {
            assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper);

            ts.restart();
            assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper);

            ts.stop();
            ZKUtils.closeQuietly(ts);

            assertAwaitState(ZooKeeper.States.CONNECTING, zookeeper);


            ts = ZKServerFactory.create(zookeeperPort);

            Assertions.assertThrows(ConditionTimeoutException.class, () -> assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper));

        } finally {
            ZKUtils.closeQuietly(zookeeper);
        }
    }

    // If the Instance of ZookeeperServer is changed, Zookeeper will not automatically reconnect.
    @Test
    public void zookeeperReconnectTest() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(ts.getConnectString(), 5000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                logger.info("process:{}", watchedEvent);
            }
        });

        try {
            assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper);

            ts.restart();
            assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper);
        } finally {
            ZKUtils.closeQuietly(zookeeper);
        }
    }


    private void assertAwaitState(ZooKeeper.States expectedState, ZooKeeper zookeeper) {
        awaitility()
                .untilAsserted(() -> assertThat(zookeeper.getState()).isEqualTo(expectedState));
    }

    // Even if the Instance of the ZookeeperServer changes, the Curator will automatically reconnect.
    @Test
    public void curatorExpiredTest() throws Exception {
        CuratorZookeeperClient curatorZookeeperClient = new CuratorZookeeperClient(ts.getConnectString(), 5000, new LoggingZookeeperEventWatcher());
        try (curatorZookeeperClient) {
            curatorZookeeperClient.connect();

            assertAwaitState(true, curatorZookeeperClient);

            ts.restart();

            assertAwaitState(true, curatorZookeeperClient);

            ts.stop();
            ZKUtils.closeQuietly(ts);

            assertAwaitState(false, curatorZookeeperClient);

            ts = ZKServerFactory.create(zookeeperPort);

            assertAwaitState(true, curatorZookeeperClient);
        }
    }


    @Test
    public void curatorReconnectTest() throws Exception {
        CuratorZookeeperClient curatorZookeeperClient = new CuratorZookeeperClient(ts.getConnectString(), 5000, new LoggingZookeeperEventWatcher());
        try (curatorZookeeperClient) {
            curatorZookeeperClient.connect();

            assertAwaitState(true, curatorZookeeperClient);

            ts.restart();
            assertAwaitState(true, curatorZookeeperClient);
        }
    }

    private void assertAwaitState(boolean expectedConnected, CuratorZookeeperClient curatorZookeeperClient) {
        awaitility()
                .untilAsserted(() -> assertThat(curatorZookeeperClient.isConnected()).isEqualTo(expectedConnected));
    }

    private static class LoggingZookeeperEventWatcher implements ZookeeperEventWatcher {
        private final Logger logger = LogManager.getLogger(LoggingZookeeperEventWatcher.class);

        @Override
        public boolean handleConnected() {
            logger.info("handleConnected()");
            return true;
        }

        @Override
        public boolean handleDisconnected() {
            logger.info("handleDisconnected()");
            return true;
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            logger.info("process:{}", watchedEvent);
        }
    }

}
