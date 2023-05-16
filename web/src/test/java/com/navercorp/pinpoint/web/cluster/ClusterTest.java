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

package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.client.SimpleMessageListener;
import com.navercorp.pinpoint.test.client.TestPinpointClient;
import com.navercorp.pinpoint.web.cluster.connection.ClusterConnectionManager;
import com.navercorp.pinpoint.web.cluster.zookeeper.ZookeeperClusterDataManager;
import com.navercorp.pinpoint.web.config.WebClusterProperties;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.TestSocketUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class ClusterTest {

    private static final Logger LOGGER = LogManager.getLogger(ClusterTest.class);

    private static final Charset UTF_8_CHARSET = StandardCharsets.UTF_8;

    // some tests may fail when executed in local environment
    // when failures happen, you have to copy pinpoint-web-root.properties of resource-test to resource-local. Tests will succeed.

    private ConditionFactory awaitility() {
        return Awaitility.await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .timeout(10000, TimeUnit.MILLISECONDS);
    }


    private static final String DEFAULT_IP = NetUtils.LOOPBACK_ADDRESS_V4;
    static ClusterConnectionManager clusterConnectionManager;
    static ZookeeperClusterDataManager clusterDataManager;

    private static String zookeeperAddress;

    private static int acceptorPort;
    private static String CLUSTER_NODE_PATH;

    private static TestingServer ts = null;

    @BeforeAll
    public static void setUp() throws Exception {
        int zookeeperPort = TestSocketUtils.findAvailableTcpPort();
        zookeeperAddress = DEFAULT_IP + ":" + zookeeperPort;
        ts = createZookeeperServer(zookeeperPort);

        WebClusterProperties properties = mock(WebClusterProperties.class);
        when(properties.isClusterEnable()).thenReturn(true);
        when(properties.getHostAddress()).thenReturn(DEFAULT_IP);
        when(properties.getClusterZookeeperAddress()).thenReturn(zookeeperAddress);
        when(properties.getClusterZookeeperRetryInterval()).thenReturn(60000);
        when(properties.getClusterZookeeperSessionTimeout()).thenReturn(3000);
        when(properties.getWebZNodePath()).
                thenReturn(ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.WEB_LEAF_PATH));
        when(properties.getCollectorZNodePath()).
                thenReturn(ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.COLLECTOR_LEAF_PATH));

        acceptorPort = TestSocketUtils.findAvailableTcpPort();
        String acceptorAddress = DEFAULT_IP + ":" + acceptorPort;
        when(properties.getClusterTcpPort()).thenReturn(acceptorPort);

        CLUSTER_NODE_PATH
                = ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.WEB_LEAF_PATH, acceptorAddress);
        LOGGER.debug("CLUSTER_NODE_PATH:{}", CLUSTER_NODE_PATH);

        clusterConnectionManager = new ClusterConnectionManager(properties);
        clusterConnectionManager.start();

        clusterDataManager = new ZookeeperClusterDataManager(properties);
        clusterDataManager.start();

        List<String> localV4IpList = NetUtils.getLocalV4IpList();
        clusterDataManager.registerWebCluster(acceptorAddress, convertIpListToBytes(localV4IpList, "\r\n"));
    }

    @AfterAll
    public static void tearDown() throws Exception {
        closeZookeeperServer(ts);

        try {
            clusterDataManager.stop();
        } catch (Exception ignored) {
        }

        try {
            clusterConnectionManager.stop();
        } catch (Exception ignored) {
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

    private static byte[] convertIpListToBytes(List<String> ipList, String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<String> ipIterator = ipList.iterator();
        while (ipIterator.hasNext()) {
            String eachIp = ipIterator.next();
            stringBuilder.append(eachIp);

            if (ipIterator.hasNext()) {
                stringBuilder.append(delimiter);
            }
        }

        return stringBuilder.toString().getBytes(UTF_8_CHARSET);
    }

    @AfterEach
    public void after() throws Exception {
        ts.restart();
    }

    @Test
    public void clusterTest1() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(zookeeperAddress, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                LOGGER.info("process:{}", watchedEvent);
            }
        });
        awaitZookeeperConnected(zookeeper);

        zookeeper.close();
    }

    @Test
    public void clusterTest2() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(zookeeperAddress, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                LOGGER.info("process:{}", watchedEvent);
            }
        });
        awaitZookeeperConnected(zookeeper);

        ts.stop();

        awaitZookeeperDisconnected(zookeeper);
        try {
            zookeeper.getData(CLUSTER_NODE_PATH, null, null);
            Assertions.fail();
        } catch (KeeperException e) {
            Assertions.assertEquals(KeeperException.Code.CONNECTIONLOSS, e.code());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ts.restart();
        getNodeAndCompareContents(zookeeper);

        zookeeper.close();
    }

    @Test
    public void clusterTest3() throws Exception {
        ZooKeeper zookeeper = null;
        TestPinpointClient testPinpointClient = new TestPinpointClient(SimpleMessageListener.INSTANCE);
        try {
            zookeeper = new ZooKeeper(zookeeperAddress, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    LOGGER.info("process:{}", watchedEvent);
                }
            });
            awaitZookeeperConnected(zookeeper);

            assertThat(clusterConnectionManager.getClusterList()).isEmpty();

            testPinpointClient.connect(DEFAULT_IP, acceptorPort);
            awaitPinpointClientConnected(clusterConnectionManager);

            assertThat(clusterConnectionManager.getClusterList()).hasSize(1);
        } finally {
            testPinpointClient.closeAll();
            if (zookeeper != null) {
                zookeeper.close();
            }
        }
    }

    private void awaitZookeeperConnected(final ZooKeeper zookeeper) {
        awaitility().until(() -> getNodeAndCompareContents0(zookeeper));
    }

    private void awaitZookeeperDisconnected(final ZooKeeper zookeeper) {
        awaitility()
                .untilAsserted(() -> assertThat(getNodeAndCompareContents0(zookeeper)).isFalse());
    }

    private void awaitPinpointClientConnected(final ClusterConnectionManager connectionManager) {
        awaitility()
                .untilAsserted(() -> assertThat(connectionManager.getClusterList()).isNotEmpty());
    }

    private void getNodeAndCompareContents(ZooKeeper zookeeper) throws KeeperException, InterruptedException {
        LOGGER.debug("getNodeAndCompareContents() {}", CLUSTER_NODE_PATH);

        byte[] contents = zookeeper.getData(CLUSTER_NODE_PATH, null, null);

        String[] registeredIpList = new String(contents).split("\r\n");

        List<String> ipList = NetUtils.getLocalV4IpList();

        assertThat(ipList).hasSize(registeredIpList.length);

        for (String ip : registeredIpList) {
            assertThat(ipList).contains(ip);
        }
    }

    private boolean getNodeAndCompareContents0(ZooKeeper zookeeper) {
        try {
            LOGGER.debug("getNodeAndCompareContents() {}", CLUSTER_NODE_PATH);

            byte[] contents = zookeeper.getData(CLUSTER_NODE_PATH, null, null);
            if (contents == null) {
                contents = new byte[0];
            }

            String[] registeredIplist = new String(contents).split("\r\n");

            List<String> ipList = NetUtils.getLocalV4IpList();

            if (registeredIplist.length != ipList.size()) {
                return false;
            }

            for (String ip : registeredIplist) {
                if (!ipList.contains(ip)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return false;
    }

}
