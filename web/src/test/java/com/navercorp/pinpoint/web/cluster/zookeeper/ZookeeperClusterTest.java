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

import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.web.util.PinpointWebTestUtils;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ZookeeperClusterTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_IP = PinpointWebTestUtils.getRepresentationLocalV4Ip();

    private static int acceptorPort;
    private static int zookeeperPort;

    private static final String COLLECTOR_NODE_PATH = "/pinpoint-cluster/collector";
    private static final String COLLECTOR_TEST_NODE_PATH = "/pinpoint-cluster/collector/test";
    private static String CLUSTER_NODE_PATH;;

    private static TestingServer ts = null;

    @BeforeClass
    public static void setUp() throws Exception {
        acceptorPort = PinpointWebTestUtils.findAvailablePort();
        zookeeperPort = PinpointWebTestUtils.findAvailablePort(acceptorPort + 1);
        
        CLUSTER_NODE_PATH = "/pinpoint-cluster/web/" + DEFAULT_IP + ":" + acceptorPort;
        
        ts = createZookeeperServer(zookeeperPort);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        closeZookeeperServer(ts);
    }

    @Before
    public void before() throws IOException {
        ts.stop();
    }

    // test for zookeeper agents to be registered correctly at the cluster as expected
    @Test
    public void clusterTest1() throws Exception {
        ts.restart();

        ZooKeeper zookeeper = null;
        ZookeeperClusterManager manager = null;
        try {
            zookeeper = new ZooKeeper(DEFAULT_IP + ":" + zookeeperPort, 5000, null);
            createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".getBytes(), -1);

            manager = new ZookeeperClusterManager(DEFAULT_IP + ":" + zookeeperPort, 5000, 60000);
            Thread.sleep(3000);

            List<String> agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(1, agentList.size());
            Assert.assertEquals("test", agentList.get(0));

            agentList = manager.getRegisteredAgentList("b", "c", 1L);
            Assert.assertEquals(0, agentList.size());

            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "".getBytes(), -1);
            Thread.sleep(3000);

            agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(0, agentList.size());
        } finally {
            if (zookeeper != null) {
                zookeeper.close();
            }

            if (manager != null) {
                manager.close();
            }
        }
    }

    @Test
    public void clusterTest2() throws Exception {
        ts.restart();

        ZooKeeper zookeeper = null;
        ZookeeperClusterManager manager = null;
        try {
            zookeeper = new ZooKeeper(DEFAULT_IP + ":" + zookeeperPort, 5000, null);
            createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".getBytes(), -1);

            manager = new ZookeeperClusterManager(DEFAULT_IP + ":" + zookeeperPort, 5000, 60000);
            Thread.sleep(3000);

            List<String> agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(1, agentList.size());
            Assert.assertEquals("test", agentList.get(0));

            zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1\r\nc:d:2".getBytes(), -1);
            Thread.sleep(3000);


            agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(1, agentList.size());
            Assert.assertEquals("test", agentList.get(0));

            agentList = manager.getRegisteredAgentList("c", "d", 2L);
            Assert.assertEquals(1, agentList.size());
            Assert.assertEquals("test", agentList.get(0));

            zookeeper.delete(COLLECTOR_TEST_NODE_PATH, -1);
            Thread.sleep(3000);

            agentList = manager.getRegisteredAgentList("a", "b", 1L);
            Assert.assertEquals(0, agentList.size());

            agentList = manager.getRegisteredAgentList("c", "d", 2L);
            Assert.assertEquals(0, agentList.size());
        } finally {
            if (zookeeper != null) {
                zookeeper.close();
            }

            if (manager != null) {
                manager.close();
            }
        }
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

        String[] registeredIplist = new String(contents).split("\r\n");

        List<String> ipList = NetUtils.getLocalV4IpList();

        Assert.assertEquals(registeredIplist.length, ipList.size());

        for (String ip : registeredIplist) {
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
            logger.info("Create path {} success.", result);
        } while (pos < path.length());
    }

}
