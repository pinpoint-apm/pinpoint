/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.cluster.zookeeper;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.web.cluster.ClusterId;
import com.navercorp.pinpoint.web.config.WebClusterProperties;
import com.navercorp.pinpoint.web.util.PinpointWebTestUtils;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.TestSocketUtils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class ZookeeperClusterTest {

    private static final Logger logger = LogManager.getLogger(ZookeeperClusterTest.class);

    private static final String DEFAULT_IP = PinpointWebTestUtils.getRepresentationLocalV4Ip();

    private static int zookeeperPort;
    private static WebClusterProperties webClusterProperties;

    private static final String COLLECTOR_TEST_NODE_PATH
            = ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.COLLECTOR_LEAF_PATH, "test");
    private static String CLUSTER_NODE_PATH;

    private static TestingServer ts = null;

    private ConditionFactory awaitility() {
        return Awaitility.await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .timeout(10000, TimeUnit.MILLISECONDS);
    }

    @BeforeAll
    public static void setUp() throws Exception {
        int acceptorPort = TestSocketUtils.findAvailableTcpPort();
        zookeeperPort = TestSocketUtils.findAvailableTcpPort();

        CLUSTER_NODE_PATH
                = ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.WEB_LEAF_PATH, DEFAULT_IP + ":" + acceptorPort);

        ts = createZookeeperServer(zookeeperPort);

        webClusterProperties = getWebClusterProperties();
    }

    private static WebClusterProperties getWebClusterProperties() {
        WebClusterProperties mockWebClusterProperties = Mockito.mock(WebClusterProperties.class);
        when(mockWebClusterProperties.getClusterZookeeperAddress()).thenReturn(DEFAULT_IP + ":" + zookeeperPort);
        when(mockWebClusterProperties.getClusterZookeeperSessionTimeout()).thenReturn(5000);
        when(mockWebClusterProperties.getClusterZookeeperRetryInterval()).thenReturn(60000);
        when(mockWebClusterProperties.getWebZNodePath()).
                thenReturn(ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.WEB_LEAF_PATH));
        when(mockWebClusterProperties.getCollectorZNodePath()).
                thenReturn(ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.COLLECTOR_LEAF_PATH));
        when(mockWebClusterProperties.getPullRetryIntervalTimeMillis()).
                thenReturn(15000);
        return mockWebClusterProperties;
    }

    @AfterAll
    public static void tearDown() {
        closeZookeeperServer(ts);
    }

    @AfterEach
    public void after() throws Exception {
        ts.restart();
    }

    // test for zookeeper agents to be registered correctly at the cluster as expected
    @Test
    public void clusterTest1() throws Exception {
        ZooKeeper zookeeper = null;
        ZookeeperClusterDataManager manager = null;
        try {
            zookeeper = new ZooKeeper(DEFAULT_IP + ":" + zookeeperPort, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    logger.debug("event:{}", event);
                }
            });
            createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".getBytes(), -1);

            manager = new ZookeeperClusterDataManager(webClusterProperties);
            manager.start();
            awaitClusterManagerConnected(manager);

            awaitCheckAgentRegistered(manager, new ClusterKey("a", "b", 1L));
            List<ClusterId> agentList = manager.getRegisteredAgentList(new ClusterKey("a", "b", 1L));
            assertThat(agentList).hasSize(1);
            Assertions.assertEquals("test", agentList.get(0).getCollectorId());

            agentList = manager.getRegisteredAgentList(new ClusterKey("b", "c", 1L));
            assertThat(agentList).isEmpty();
            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "".getBytes(), -1);
            awaitCheckAgentUnRegistered(manager, new ClusterKey("a", "b", 1L));

        } finally {
            closeZk(zookeeper);
            closeManager(manager);
        }
    }

    private void closeManager(ZookeeperClusterDataManager manager) {
        if (manager != null) {
            manager.stop();
        }
    }

    private void closeZk(ZooKeeper zookeeper) throws InterruptedException {
        if (zookeeper != null) {
            zookeeper.close();
        }
    }

    @Test
    public void clusterTest2() throws Exception {
        ZooKeeper zookeeper = null;
        ZookeeperClusterDataManager manager = null;
        try {
            zookeeper = new ZooKeeper(DEFAULT_IP + ":" + zookeeperPort, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    logger.debug("process:{}", watchedEvent);
                }
            });
            createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".getBytes(), -1);

            manager = new ZookeeperClusterDataManager(webClusterProperties);
            manager.start();
            awaitClusterManagerConnected(manager);

            awaitCheckAgentRegistered(manager, new ClusterKey("a", "b", 1L));
            List<ClusterId> agentList = manager.getRegisteredAgentList(new ClusterKey("a", "b", 1L));
            assertThat(agentList).hasSize(1);
            Assertions.assertEquals("test", agentList.get(0).getCollectorId());

            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1\r\nc:d:2".getBytes(), -1);
            awaitCheckAgentRegistered(manager, new ClusterKey("c", "d", 2L));

            agentList = manager.getRegisteredAgentList(new ClusterKey("a", "b", 1L));
            assertThat(agentList).hasSize(1);
            Assertions.assertEquals("test", agentList.get(0).getCollectorId());

            agentList = manager.getRegisteredAgentList(new ClusterKey("c", "d", 2L));
            assertThat(agentList).hasSize(1);
            Assertions.assertEquals("test", agentList.get(0).getCollectorId());

            zookeeper.delete(COLLECTOR_TEST_NODE_PATH, -1);
            Thread.sleep(10000);

            awaitCheckAgentUnRegistered(manager, new ClusterKey("a", "b", 1L));

            agentList = manager.getRegisteredAgentList(new ClusterKey("a", "b", 1L));
            assertThat(agentList).isEmpty();

            agentList = manager.getRegisteredAgentList(new ClusterKey("c", "d", 2L));
            assertThat(agentList).isEmpty();
        } finally {
            closeZk(zookeeper);
            closeManager(manager);
        }
    }

    private void awaitClusterManagerConnected(final ZookeeperClusterDataManager manager) {
        awaitility()
                .until(manager::isConnected);
    }

    private void awaitCheckAgentRegistered(final ZookeeperClusterDataManager manager, ClusterKey clusterKey) {
        awaitility()
                .untilAsserted(() -> assertThat(getRegisteredAgentList(manager, clusterKey).call()).isNotEmpty());
    }

    private void awaitCheckAgentUnRegistered(final ZookeeperClusterDataManager manager, ClusterKey clusterKey) {
        awaitility()
                .untilAsserted(() -> assertThat(getRegisteredAgentList(manager, clusterKey).call()).isEmpty());
    }

    private Callable<List<ClusterId>> getRegisteredAgentList(ZookeeperClusterDataManager manager, ClusterKey clusterKey) {
        return () -> manager.getRegisteredAgentList(clusterKey);
    }

    private static TestingServer createZookeeperServer(int port) throws Exception {
        TestingServer mockZookeeperServer = new TestingServer(port);
        mockZookeeperServer.start();

        return mockZookeeperServer;
    }

    private static void closeZookeeperServer(TestingServer mockZookeeperServer) {
        try {
            if (mockZookeeperServer != null) {
                mockZookeeperServer.close();
            }
        } catch (Exception e) {
            logger.warn("closeZookeeperServer error", e);
        }
    }

    @SuppressWarnings("unused")
    private void getNodeAndCompareContents(ZooKeeper zookeeper) throws KeeperException, InterruptedException {
        byte[] contents = zookeeper.getData(CLUSTER_NODE_PATH, null, null);

        String[] registeredIpList = new String(contents).split("\r\n");

        List<String> ipList = NetUtils.getLocalV4IpList();

        assertThat(ipList).hasSize(registeredIpList.length);

        for (String ip : registeredIpList) {
            assertThat(ipList).contains(ip);
        }
    }

    public void createPath(ZooKeeper zookeeper, String path, boolean createEndNode) throws PinpointZookeeperException, InterruptedException, KeeperException {
        int pos = 1;
        do {
            pos = path.indexOf('/', pos + 1);

            if (pos == -1) {
                pos = path.length();
            }

            if (pos == path.length()) {
                if (!createEndNode) {
                    return;
                }
            }

            String subPath = path.substring(0, pos);
            if (zookeeper.exists(subPath, false) != null) {
                continue;
            }

            String result = zookeeper.create(subPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            logger.debug("Create path {} success.", result);
        } while (pos < path.length());
    }

}
