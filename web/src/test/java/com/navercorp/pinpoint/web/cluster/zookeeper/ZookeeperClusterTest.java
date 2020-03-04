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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import com.navercorp.pinpoint.web.config.WebConfig;
import com.navercorp.pinpoint.web.util.PinpointWebTestUtils;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class ZookeeperClusterTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_IP = PinpointWebTestUtils.getRepresentationLocalV4Ip();

    private static int zookeeperPort;
    private static WebConfig webConfig;

    private static TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 10000);

    private static final String COLLECTOR_NODE_PATH = "/pinpoint-cluster/collector";
    private static final String COLLECTOR_TEST_NODE_PATH = COLLECTOR_NODE_PATH + "/test";
    private static String CLUSTER_NODE_PATH;

    private static TestingServer ts = null;

    @BeforeClass
    public static void setUp() throws Exception {
        int acceptorPort = SocketUtils.findAvailableTcpPort();
        zookeeperPort = SocketUtils.findAvailableTcpPort(acceptorPort + 1);
        
        CLUSTER_NODE_PATH = "/pinpoint-cluster/web/" + DEFAULT_IP + ":" + acceptorPort;
        
        ts = createZookeeperServer(zookeeperPort);

        WebConfig mockWebConfig = Mockito.mock(WebConfig.class);
        when(mockWebConfig.getClusterZookeeperAddress()).thenReturn(DEFAULT_IP + ":" + zookeeperPort);
        when(mockWebConfig.getClusterZookeeperSessionTimeout()).thenReturn(5000);
        when(mockWebConfig.getClusterZookeeperRetryInterval()).thenReturn(60000);
        webConfig = mockWebConfig;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        closeZookeeperServer(ts);
    }

    @After
    public void after() throws Exception {
        ts.restart();
    }

    // test for zookeeper agents to be registered correctly at the cluster as expected
    @Test
    public void clusterTest1() throws Exception {
        ZooKeeper zookeeper = null;
        ZookeeperClusterDataManager manager = null;
        try {
            zookeeper = new ZooKeeper(DEFAULT_IP + ":" + zookeeperPort, 5000, null);
            createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".getBytes(), -1);

            manager = new ZookeeperClusterDataManager(webConfig);
            manager.start();
            awaitClusterManagerConnected(manager);

            awaitCheckAgentRegistered(manager, "a", "b", 1L);
            List<String> agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(1, agentList.size());
            Assert.assertEquals("test", agentList.get(0));

            agentList = manager.getRegisteredAgentList("b", "c", 1L);
            Assert.assertEquals(0, agentList.size());

            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "".getBytes(), -1);
            final ZookeeperClusterDataManager finalManager = manager;
            boolean await = awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return finalManager.getRegisteredAgentList("a", "b", 1L).isEmpty();
                }
            });

            Assert.assertTrue(await);
        } finally {
            if (zookeeper != null) {
                zookeeper.close();
            }

            if (manager != null) {
                manager.stop();
            }
        }
    }

    @Test
    public void clusterTest2() throws Exception {
        ZooKeeper zookeeper = null;
        ZookeeperClusterDataManager manager = null;
        try {
            zookeeper = new ZooKeeper(DEFAULT_IP + ":" + zookeeperPort, 5000, null);
            createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".getBytes(), -1);

            manager = new ZookeeperClusterDataManager(webConfig);
            manager.start();
            awaitClusterManagerConnected(manager);

            awaitCheckAgentRegistered(manager, "a", "b", 1L);
            List<String> agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(1, agentList.size());
            Assert.assertEquals("test", agentList.get(0));

            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1\r\nc:d:2".getBytes(), -1);
            awaitCheckAgentRegistered(manager, "c", "d", 2L);

            agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(1, agentList.size());
            Assert.assertEquals("test", agentList.get(0));

            agentList = manager.getRegisteredAgentList("c", "d", 2L);
            Assert.assertEquals(1, agentList.size());
            Assert.assertEquals("test", agentList.get(0));

            zookeeper.delete(COLLECTOR_TEST_NODE_PATH, -1);
            awaitCheckAgentUnRegistered(manager, "a", "b", 1L);

            agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(0, agentList.size());

            agentList = manager.getRegisteredAgentList("c", "d", 2L);
            Assert.assertEquals(0, agentList.size());
        } finally {
            if (zookeeper != null) {
                zookeeper.close();
            }

            if (manager != null) {
                manager.stop();
            }
        }
    }

    private void awaitClusterManagerConnected(final ZookeeperClusterDataManager manager) {
        boolean await = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return manager.isConnected();
            }
        });
        Assert.assertTrue(await);
    }

    private void awaitCheckAgentRegistered(final ZookeeperClusterDataManager manager, final String applicationName, final String agentId, final long startTimeStamp) {
        boolean await = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !manager.getRegisteredAgentList(applicationName, agentId, startTimeStamp).isEmpty();
            }
        });
        Assert.assertTrue(await);
    }

    private void awaitCheckAgentUnRegistered(final ZookeeperClusterDataManager manager, final String applicationName, final String agentId, final long startTimeStamp) {
        boolean await = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return manager.getRegisteredAgentList(applicationName, agentId, startTimeStamp).isEmpty();
            }
        });
        Assert.assertTrue(await);
    }

    private static TestingServer createZookeeperServer(int port) throws Exception {
        TestingServer mockZookeeperServer = new TestingServer(port);
        mockZookeeperServer.start();

        return mockZookeeperServer;
    }

    private static void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
        try {
            if (mockZookeeperServer != null) {
                mockZookeeperServer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNodeAndCompareContents(ZooKeeper zookeeper) throws KeeperException, InterruptedException {
        byte[] contents = zookeeper.getData(CLUSTER_NODE_PATH, null, null);

        String[] registeredIpList = new String(contents).split("\r\n");

        List<String> ipList = NetUtils.getLocalV4IpList();

        Assert.assertEquals(registeredIpList.length, ipList.size());

        for (String ip : registeredIpList) {
            Assert.assertTrue(ipList.contains(ip));
        }
    }

    private void closeResources(PinpointClientFactory clientFactory, PinpointClient client) {
        if (client != null) {
            client.close();
        }

        if (clientFactory != null) {
            clientFactory.release();
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
