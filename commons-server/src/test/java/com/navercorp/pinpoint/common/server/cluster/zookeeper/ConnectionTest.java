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

import com.navercorp.pinpoint.common.server.util.TestAwaitTaskUtils;
import com.navercorp.pinpoint.common.server.util.TestAwaitUtils;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

/**
 * This test compares the connection status of Curator and Zookeeper.
 *
 * @author Taejin Koo
 */
public class ConnectionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionTest.class);

    private static TestAwaitUtils AWAIT_UTILS = new TestAwaitUtils(100, 3000);

    private static int zookeeperPort;
    private static TestingServer ts = null;

    @BeforeClass
    public static void setUp() throws Exception {
        zookeeperPort = SocketUtils.findAvailableTcpPort();
        ts = createTestingServer();
    }

    private static TestingServer createTestingServer() throws Exception {
        return new TestingServer(zookeeperPort);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (ts != null) {
            ts.stop();
            ts.close();
        }
    }

    // If the Instance of ZookeeperServer is changed, Zookeeper will not automatically reconnect.
    @Test
    public void zookeeperExpiredTest() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(ts.getConnectString(), 5000, null);

        try {
            boolean pass = awaitState(zookeeper, ZooKeeper.States.CONNECTED);
            Assert.assertTrue(pass);

            ts.restart();
            pass = awaitState(zookeeper, ZooKeeper.States.CONNECTED);
            Assert.assertTrue(pass);

            ts.stop();
            ts.close();

            pass = awaitState(zookeeper, ZooKeeper.States.CONNECTING);
            Assert.assertTrue(pass);

            ts = createTestingServer();

            pass = awaitState(zookeeper, ZooKeeper.States.CONNECTED);
            Assert.assertFalse(pass);
        } finally {
            if (zookeeper != null) {
                zookeeper.close();
            }
        }
    }

    // If the Instance of ZookeeperServer is changed, Zookeeper will not automatically reconnect.
    @Test
    public void zookeeperReconnectTest() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(ts.getConnectString(), 5000, null);

        try {
            boolean pass = awaitState(zookeeper, ZooKeeper.States.CONNECTED);
            Assert.assertTrue(pass);

            ts.restart();
            pass = awaitState(zookeeper, ZooKeeper.States.CONNECTED);
            Assert.assertTrue(pass);
        } finally {
            if (zookeeper != null) {
                zookeeper.close();
            }
        }
    }

    private boolean awaitState(ZooKeeper zookeeper, ZooKeeper.States expectedState) {
        return AWAIT_UTILS.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                ZooKeeper.States state = zookeeper.getState();
                return state == expectedState;
            }
        });
    }

    // Even if the Instance of the ZookeeperServer changes, the Curator will automatically reconnect.
    @Test
    public void curatorExpiredTest() throws Exception {
        CuratorZookeeperClient curatorZookeeperClient = new CuratorZookeeperClient(ts.getConnectString(), 5000, new LoggingZookeeperEventWatcher());
        try {
            curatorZookeeperClient.connect();

            boolean pass = awaitState(curatorZookeeperClient, true);
            Assert.assertTrue(pass);

            ts.restart();

            pass = awaitState(curatorZookeeperClient, true);
            Assert.assertTrue(pass);

            ts.stop();
            ts.close();

            pass = awaitState(curatorZookeeperClient, false);
            Assert.assertTrue(pass);

            ts = createTestingServer();

            pass = awaitState(curatorZookeeperClient, true);
            Assert.assertTrue(pass);
        } finally {
            if (curatorZookeeperClient != null) {
                curatorZookeeperClient.close();
            }
        }
    }

    @Test
    public void curatorReconnectTest() throws Exception {
        CuratorZookeeperClient curatorZookeeperClient = new CuratorZookeeperClient(ts.getConnectString(), 5000, new LoggingZookeeperEventWatcher());

        try {
            curatorZookeeperClient.connect();

            boolean pass = awaitState(curatorZookeeperClient, true);
            Assert.assertTrue(pass);

            ts.restart();
            pass = awaitState(curatorZookeeperClient, true);
            Assert.assertTrue(pass);
        } finally {
            if (curatorZookeeperClient != null) {
                curatorZookeeperClient.close();
            }
        }
    }

    private boolean awaitState(CuratorZookeeperClient curatorZookeeperClient, boolean expectedConnected) {
        return AWAIT_UTILS.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return curatorZookeeperClient.isConnected() == expectedConnected;
            }
        });
    }

    private static class LoggingZookeeperEventWatcher implements ZookeeperEventWatcher {
        @Override
        public boolean handleConnected() {
            LOGGER.info("handleConnected()");
            return true;
        }

        @Override
        public boolean handleDisconnected() {
            LOGGER.info("handleDisconnected()");
            return true;
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            LOGGER.info("process:{}", watchedEvent);
        }
    }

}
