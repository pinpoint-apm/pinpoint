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
import com.navercorp.pinpoint.common.server.util.TestAwaitTaskUtils;
import com.navercorp.pinpoint.common.server.util.TestAwaitUtils;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class CuratorZookeeperClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CuratorZookeeperClientTest.class);

    private static TestAwaitUtils AWAIT_UTILS = new TestAwaitUtils(100, 3000);

    private static TestingServer ts = null;
    private static EventHoldingZookeeperEventWatcher eventHoldingZookeeperEventWatcher;
    private static CuratorZookeeperClient curatorZookeeperClient;

    private static final String PARENT_PATH = "/a/b/c/";

    private static final AtomicInteger TEST_NODE_ID = new AtomicInteger();

    @BeforeClass
    public static void setUpClass() throws Exception {
        int availablePort = SocketUtils.findAvailableTcpPort();
        ts = new TestingServer(availablePort);

        eventHoldingZookeeperEventWatcher = new EventHoldingZookeeperEventWatcher();
        curatorZookeeperClient = createCuratorZookeeperClient(ts.getConnectString(), eventHoldingZookeeperEventWatcher);
        curatorZookeeperClient.createPath(PARENT_PATH);
    }

    @Before
    public void setUp() throws Exception {
        List<String> nodeList = curatorZookeeperClient.getChildNodeList(PARENT_PATH, false);
        for (String node : nodeList) {
            String fullPath = ZKPaths.makePath(PARENT_PATH, node);
            curatorZookeeperClient.delete(fullPath);
        }
        eventHoldingZookeeperEventWatcher.eventClear();
    }

    private static CuratorZookeeperClient createCuratorZookeeperClient(String connectString, EventHoldingZookeeperEventWatcher zookeeperEventWatcher) throws IOException {
        CuratorZookeeperClient curatorZookeeperClient = new CuratorZookeeperClient(connectString, 3000, zookeeperEventWatcher);
        curatorZookeeperClient.connect();
        curatorZookeeperClient.connect();
        return curatorZookeeperClient;
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            if (curatorZookeeperClient != null) {
                curatorZookeeperClient.close();
            }
        } catch (Exception e) {
            // skip
        }

        if (ts != null) {
            ts.stop();
            ts.close();
        }
    }

    @Test
    public void createAndDeleteTest() throws Exception {
        ZooKeeper zooKeeper = createZookeeper();
        try {
            String message = createTestMessage();
            String testNodePath = createTestNodePath();

            curatorZookeeperClient.createPath(testNodePath);
            curatorZookeeperClient.createNode(new CreateNodeMessage(testNodePath, message.getBytes()));

            byte[] result = curatorZookeeperClient.getData(testNodePath);
            Assert.assertEquals(message, new String(result));

            curatorZookeeperClient.delete(testNodePath);

            Assert.assertFalse(isExistNode(zooKeeper, testNodePath));
        } finally {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        }
    }

    @Test
    public void createOrSetNodeTest() throws Exception {
        ZooKeeper zooKeeper = createZookeeper();

        try {
            String message = createTestMessage();
            String testNodePath = createTestNodePath() + "/temp";

            ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(testNodePath);
            String path = pathAndNode.getPath();
            String node = pathAndNode.getNode();

            try {
                curatorZookeeperClient.createOrSetNode(new CreateNodeMessage(testNodePath, message.getBytes()));
                Assert.fail();
            } catch (Exception e) {
            }

            boolean existNode = isExistNode(zooKeeper, path);
            Assert.assertFalse(existNode);

            existNode = isExistNode(zooKeeper, testNodePath);
            Assert.assertFalse(existNode);

            curatorZookeeperClient.createOrSetNode(new CreateNodeMessage(testNodePath, message.getBytes(), true));

            existNode = isExistNode(zooKeeper, testNodePath);
            Assert.assertTrue(existNode);

            curatorZookeeperClient.delete(testNodePath);
        } finally {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        }
    }

    @Test(expected = BadOperationException.class)
    public void alreadyExistNodeCreateTest() throws Exception {
        ZooKeeper zooKeeper = createZookeeper();
        try {
            String message = createTestMessage();
            String testNodePath = createTestNodePath();

            curatorZookeeperClient.createNode(new CreateNodeMessage(testNodePath, message.getBytes()));
            Assert.assertTrue(isExistNode(zooKeeper, testNodePath));

            curatorZookeeperClient.createNode(new CreateNodeMessage(testNodePath, "test".getBytes()));
        } finally {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        }
    }

    @Test
    public void getTest() throws Exception {
        ZooKeeper zooKeeper = createZookeeper();
        try {
            String testNodePath = createTestNodePath();

            curatorZookeeperClient.createNode(new CreateNodeMessage(testNodePath, "".getBytes()));
            Assert.assertTrue(isExistNode(zooKeeper, testNodePath));

            curatorZookeeperClient.getData(testNodePath, true);

            String message = createTestMessage();
            zooKeeper.setData(testNodePath, message.getBytes(), -1);
            assertGetWatchedEvent(testNodePath, message);

            message = createTestMessage();
            curatorZookeeperClient.createOrSetNode(new CreateNodeMessage(testNodePath, message.getBytes(), true));
            assertGetWatchedEvent(testNodePath, message);
        } finally {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        }
    }

    private void assertGetWatchedEvent(String path, String message) throws PinpointZookeeperException {
        boolean await = AWAIT_UTILS.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return eventHoldingZookeeperEventWatcher.getLastWatchedEvent() != null;
            }
        });
        Assert.assertTrue(await);

        WatchedEvent lastWatchedEvent = eventHoldingZookeeperEventWatcher.getLastWatchedEvent();
        Assert.assertEquals(Watcher.Event.EventType.NodeDataChanged, lastWatchedEvent.getType());

        byte[] result = curatorZookeeperClient.getData(path);
        Assert.assertEquals(message, new String(result));
    }

    @Test
    public void getChildrenTest() throws Exception {
        ZooKeeper zooKeeper = createZookeeper();
        try {
            String testNodePath = createTestNodePath();

            ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(testNodePath);

            List<String> childrenNode = curatorZookeeperClient.getChildNodeList(pathAndNode.getPath(), true);
            Assert.assertTrue(childrenNode.isEmpty());

            zooKeeper.create(testNodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            boolean await = AWAIT_UTILS.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return eventHoldingZookeeperEventWatcher.getLastWatchedEvent() != null;
                }
            });
            Assert.assertTrue(await);

            WatchedEvent lastWatchedEvent = eventHoldingZookeeperEventWatcher.getLastWatchedEvent();
            Assert.assertEquals(Watcher.Event.EventType.NodeChildrenChanged, lastWatchedEvent.getType());

            childrenNode = curatorZookeeperClient.getChildNodeList(pathAndNode.getPath(), false);
            Assert.assertTrue(!childrenNode.isEmpty());
        } finally {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        }
    }

    private ZooKeeper createZookeeper() throws IOException {
        ZooKeeper zooKeeper = new ZooKeeper(ts.getConnectString(), 3000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                LOGGER.info("ZooKeeper process:{}", watchedEvent);
            }
        });

        return zooKeeper;
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
