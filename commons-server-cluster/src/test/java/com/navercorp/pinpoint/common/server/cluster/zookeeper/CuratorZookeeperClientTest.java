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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.BadOperationException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import org.apache.commons.io.IOUtils;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.TestSocketUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Taejin Koo
 */
public class CuratorZookeeperClientTest {

    private static final Logger LOGGER = LogManager.getLogger(CuratorZookeeperClientTest.class);

    private ConditionFactory awaitility() {
        return Awaitility.await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .timeout(3000, TimeUnit.MILLISECONDS);
    }

    private static TestingServer ts = null;
    private static EventHoldingZookeeperEventWatcher eventHoldingZookeeperEventWatcher;
    private static CuratorZookeeperClient curatorZookeeperClient;

    private static final String PARENT_PATH = "/a/b/c/";

    private static final AtomicInteger TEST_NODE_ID = new AtomicInteger();

    @BeforeAll
    public static void setUpClass() throws Exception {
        int availablePort = TestSocketUtils.findAvailableTcpPort();
        ts = ZKServerFactory.create(availablePort);

        eventHoldingZookeeperEventWatcher = new EventHoldingZookeeperEventWatcher();
        curatorZookeeperClient = createCuratorZookeeperClient(ts.getConnectString(), eventHoldingZookeeperEventWatcher);
        curatorZookeeperClient.createPath(PARENT_PATH);
    }

    @BeforeEach
    public void setUp() throws Exception {
        List<String> nodeList = curatorZookeeperClient.getChildNodeList(PARENT_PATH, false);
        for (String node : nodeList) {
            String fullPath = ZKPaths.makePath(PARENT_PATH, node);
            curatorZookeeperClient.delete(fullPath);
        }
        eventHoldingZookeeperEventWatcher.eventClear();
    }

    private static CuratorZookeeperClient createCuratorZookeeperClient(String connectString, EventHoldingZookeeperEventWatcher zookeeperEventWatcher) throws PinpointZookeeperException {
        CuratorZookeeperClient curatorZookeeperClient = new CuratorZookeeperClient(connectString, 3000, zookeeperEventWatcher);
        curatorZookeeperClient.connect();
        curatorZookeeperClient.connect();
        return curatorZookeeperClient;
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        IOUtils.closeQuietly(curatorZookeeperClient);

        IOUtils.closeQuietly(ts);
    }

    @Test
    public void createAndDeleteTest() throws Exception {
        try (ZooKeeper zooKeeper = createZookeeper()) {
            String message = createTestMessage();
            String testNodePath = createTestNodePath();

            curatorZookeeperClient.createPath(testNodePath);
            curatorZookeeperClient.createNode(new CreateNodeMessage(testNodePath, message.getBytes()));

            byte[] result = curatorZookeeperClient.getData(testNodePath);
            Assertions.assertEquals(message, new String(result));

            curatorZookeeperClient.delete(testNodePath);

            Assertions.assertFalse(isExistNode(zooKeeper, testNodePath));
        }
    }

    @Test
    public void createOrSetNodeTest() throws Exception {
        try (ZooKeeper zooKeeper = createZookeeper()) {
            String message = createTestMessage();
            String testNodePath = createTestNodePath() + "/temp";

            ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(testNodePath);
            String path = pathAndNode.getPath();

            Assertions.assertThrows(Exception.class, () -> {
                curatorZookeeperClient.createOrSetNode(new CreateNodeMessage(testNodePath, message.getBytes()));
            });

            boolean existNode = isExistNode(zooKeeper, path);
            Assertions.assertFalse(existNode);

            existNode = isExistNode(zooKeeper, testNodePath);
            Assertions.assertFalse(existNode);

            curatorZookeeperClient.createOrSetNode(new CreateNodeMessage(testNodePath, message.getBytes(), true));

            existNode = isExistNode(zooKeeper, testNodePath);
            Assertions.assertTrue(existNode);

            curatorZookeeperClient.delete(testNodePath);
        }
    }

    @Test
    public void alreadyExistNodeCreateTest() {
        Assertions.assertThrows(BadOperationException.class, () -> {
            try (ZooKeeper zooKeeper = createZookeeper()) {
                String message = createTestMessage();
                String testNodePath = createTestNodePath();

                curatorZookeeperClient.createNode(new CreateNodeMessage(testNodePath, message.getBytes()));
                Assertions.assertTrue(isExistNode(zooKeeper, testNodePath));

                curatorZookeeperClient.createNode(new CreateNodeMessage(testNodePath, "test".getBytes()));
            }
        });
    }

    @Test
    public void getTest() throws Exception {
        try (ZooKeeper zooKeeper = createZookeeper()) {
            String testNodePath = createTestNodePath();

            curatorZookeeperClient.createNode(new CreateNodeMessage(testNodePath, "".getBytes()));
            Assertions.assertTrue(isExistNode(zooKeeper, testNodePath));

            curatorZookeeperClient.getData(testNodePath, true);

            String message = createTestMessage();
            zooKeeper.setData(testNodePath, message.getBytes(), -1);
            assertGetWatchedEvent(testNodePath, message);

            message = createTestMessage();
            curatorZookeeperClient.createOrSetNode(new CreateNodeMessage(testNodePath, message.getBytes(), true));
            assertGetWatchedEvent(testNodePath, message);
        }
    }

    private void assertGetWatchedEvent(String path, String message) throws PinpointZookeeperException {
        awaitility()
                .untilAsserted(() -> assertThat(eventHoldingZookeeperEventWatcher.getLastWatchedEvent()).isNotNull());

        WatchedEvent lastWatchedEvent = eventHoldingZookeeperEventWatcher.getLastWatchedEvent();
        Assertions.assertEquals(Watcher.Event.EventType.NodeDataChanged, lastWatchedEvent.getType());

        byte[] result = curatorZookeeperClient.getData(path);
        Assertions.assertEquals(message, new String(result));
    }

    @Test
    public void getChildrenTest() throws Exception {
        try (ZooKeeper zooKeeper = createZookeeper()) {
            String testNodePath = createTestNodePath();

            ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(testNodePath);

            List<String> childrenNode = curatorZookeeperClient.getChildNodeList(pathAndNode.getPath(), true);
            Assertions.assertTrue(childrenNode.isEmpty());

            zooKeeper.create(testNodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            awaitility()
                    .untilAsserted(() -> assertThat(eventHoldingZookeeperEventWatcher.getLastWatchedEvent()).isNotNull());

            WatchedEvent lastWatchedEvent = eventHoldingZookeeperEventWatcher.getLastWatchedEvent();
            Assertions.assertEquals(Watcher.Event.EventType.NodeChildrenChanged, lastWatchedEvent.getType());

            childrenNode = curatorZookeeperClient.getChildNodeList(pathAndNode.getPath(), false);
            Assertions.assertFalse(childrenNode.isEmpty());
        }
    }

    private ZooKeeper createZookeeper() throws IOException {
        return new ZooKeeper(ts.getConnectString(), 3000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                LOGGER.debug("process:{}", watchedEvent);
            }
        });
    }

    private boolean isExistNode(ZooKeeper zooKeeper, String path) throws KeeperException, InterruptedException {
        Stat isExists = zooKeeper.exists(path, false);
        return isExists != null;
    }

    private static String createTestMessage() {
        return "message-" + TEST_NODE_ID.incrementAndGet();
    }

    private static String createTestNodePath() {
        return PARENT_PATH + "test-" + TEST_NODE_ID.incrementAndGet();
    }

    private static class EventHoldingZookeeperEventWatcher implements ZookeeperEventWatcher {

        private WatchedEvent watchedEvent;

        @Override
        public boolean handleConnected() {
            return true;
        }

        @Override
        public boolean handleDisconnected() {
            return true;
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            synchronized (this) {
                LOGGER.info("process() event:{}", watchedEvent);
                this.watchedEvent = watchedEvent;
            }
        }

        synchronized WatchedEvent getLastWatchedEvent() {
            return watchedEvent;
        }

        void eventClear() {
            synchronized (this) {
                watchedEvent = null;
            }
        }
    }

}
