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

import com.navercorp.pinpoint.testcase.util.SocketUtils;
import org.apache.curator.test.TestingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;

/**
 * This test compares the connection status of Curator and Zookeeper.
 *
 * @author Taejin Koo
 */
public class ConnectionTest {

    private static final Logger LOGGER = LogManager.getLogger(ConnectionTest.class);

    private ConditionFactory awaitility() {
        ConditionFactory conditionFactory = Awaitility.await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .timeout(3000, TimeUnit.MILLISECONDS);
        return conditionFactory;
    }

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
            assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper);

            ts.restart();
            assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper);

            ts.stop();
            ts.close();

            assertAwaitState(ZooKeeper.States.CONNECTING, zookeeper);


            ts = createTestingServer();

            Assert.assertThrows(ConditionTimeoutException.class, () -> assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper));

        } finally {
            ZKUtils.closeQuietly(zookeeper);
        }
    }

    // If the Instance of ZookeeperServer is changed, Zookeeper will not automatically reconnect.
    @Test
    public void zookeeperReconnectTest() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(ts.getConnectString(), 5000, null);

        try {
            assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper);

            ts.restart();
            assertAwaitState(ZooKeeper.States.CONNECTED, zookeeper);
        } finally {
            ZKUtils.closeQuietly(zookeeper);
        }
    }


    private void assertAwaitState(ZooKeeper.States expectedState, ZooKeeper zookeeper) {
        awaitility().until(zookeeper::getState, is(expectedState));
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
            ts.close();

            assertAwaitState(false, curatorZookeeperClient);

            ts = createTestingServer();

            assertAwaitState(true, curatorZookeeperClient);
        }
    }


    @Test
    public void curatorReconnectTest() throws Exception {
        CuratorZookeeperClient curatorZookeeperClient = new CuratorZookeeperClient(ts.getConnectString(), 5000, new LoggingZookeeperEventWatcher());
        try (curatorZookeeperClient){
            curatorZookeeperClient.connect();

            assertAwaitState(true, curatorZookeeperClient);

            ts.restart();
            assertAwaitState(true, curatorZookeeperClient);
        }
    }

    private void assertAwaitState(boolean expectedConnected, CuratorZookeeperClient curatorZookeeperClient) {
        awaitility()
                .until(curatorZookeeperClient::isConnected, is(expectedConnected));
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
