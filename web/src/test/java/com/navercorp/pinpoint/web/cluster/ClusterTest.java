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

import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.SimpleMessageListener;
import com.navercorp.pinpoint.web.cluster.connection.ClusterConnectionManager;
import com.navercorp.pinpoint.web.cluster.zookeeper.ZookeeperClusterDataManager;
import com.navercorp.pinpoint.web.config.WebConfig;
import com.navercorp.pinpoint.web.TestAwaitTaskUtils;
import com.navercorp.pinpoint.web.TestAwaitUtils;
import com.navercorp.pinpoint.web.util.PinpointWebTestUtils;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ClusterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterTest.class);

    private static final Charset UTF_8_CHARSET = StandardCharsets.UTF_8;

    // some tests may fail when executed in local environment
    // when failures happen, you have to copy pinpoint-web.properties of resource-test to resource-local. Tests will succeed.

    private static TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 10000);

    private static final String DEFAULT_IP = PinpointWebTestUtils.getRepresentationLocalV4Ip();
    static ClusterConnectionManager clusterConnectionManager;
    static ZookeeperClusterDataManager clusterDataManager;
    private static String CLUSTER_NODE_PATH;
    private static int acceptorPort;
    private static int zookeeperPort;
    private static String acceptorAddress;
    private static String zookeeperAddress;
    private static TestingServer ts = null;

    @BeforeClass
    public static void setUp() throws Exception {
        acceptorPort = SocketUtils.findAvailableTcpPort(28000);
        acceptorAddress = DEFAULT_IP + ":" + acceptorPort;

        zookeeperPort = SocketUtils.findAvailableTcpPort(acceptorPort + 1);
        zookeeperAddress = DEFAULT_IP + ":" + zookeeperPort;

        ts = createZookeeperServer(zookeeperPort);

        CLUSTER_NODE_PATH = "/pinpoint-cluster/web/" + acceptorAddress;
        LOGGER.debug("CLUSTER_NODE_PATH:{}", CLUSTER_NODE_PATH);

        WebConfig config = mock(WebConfig.class);

        when(config.isClusterEnable()).thenReturn(true);
        when(config.getClusterTcpPort()).thenReturn(acceptorPort);
        when(config.getClusterZookeeperAddress()).thenReturn(zookeeperAddress);
        when(config.getClusterZookeeperRetryInterval()).thenReturn(60000);
        when(config.getClusterZookeeperSessionTimeout()).thenReturn(3000);

        clusterConnectionManager = new ClusterConnectionManager(config);
        clusterConnectionManager.start();

        clusterDataManager = new ZookeeperClusterDataManager(config);
        clusterDataManager.start();

        List<String> localV4IpList = NetUtils.getLocalV4IpList();
        clusterDataManager.registerWebCluster(acceptorAddress, convertIpListToBytes(localV4IpList, "\r\n"));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        closeZookeeperServer(ts);

        try {
            clusterDataManager.stop();
        } catch (Exception ignore) {
        }

        try {
            clusterConnectionManager.stop();
        } catch (Exception ignore) {
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

    @After
    public void after() throws Exception {
        ts.restart();
    }

    @Test
    public void clusterTest1() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(zookeeperAddress, 5000, null);
        awaitZookeeperConnected(zookeeper);

        if (zookeeper != null) {
            zookeeper.close();
        }
    }

    @Test
    public void clusterTest2() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(zookeeperAddress, 5000, null);
        awaitZookeeperConnected(zookeeper);

        ts.stop();

        awaitZookeeperDisconnected(zookeeper);
        try {
            zookeeper.getData(CLUSTER_NODE_PATH, null, null);
            Assert.fail();
        } catch (KeeperException e) {
            Assert.assertEquals(KeeperException.Code.CONNECTIONLOSS, e.code());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ts.restart();
        getNodeAndCompareContents(zookeeper);

        if (zookeeper != null) {
            zookeeper.close();
        }
    }

    @Test
    public void clusterTest3() throws Exception {
        PinpointClientFactory clientFactory = null;
        PinpointClient client = null;

        ZooKeeper zookeeper = null;
        try {
            zookeeper = new ZooKeeper(zookeeperAddress, 5000, null);
            awaitZookeeperConnected(zookeeper);

            Assert.assertEquals(0, clusterConnectionManager.getClusterList().size());

            clientFactory = new DefaultPinpointClientFactory();
            clientFactory.setMessageListener(SimpleMessageListener.INSTANCE);

            client = clientFactory.connect(DEFAULT_IP, acceptorPort);
            awaitPinpointClientConnected(clusterConnectionManager);

            Assert.assertEquals(1, clusterConnectionManager.getClusterList().size());

        } finally {
            closePinpointSocket(clientFactory, client);

            if (zookeeper != null) {
                zookeeper.close();
            }
        }
    }

    private void awaitZookeeperConnected(final ZooKeeper zookeeper) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return getNodeAndCompareContents0(zookeeper);
            }
        });
        Assert.assertTrue(pass);
    }

    private void awaitZookeeperDisconnected(final ZooKeeper zookeeper) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !getNodeAndCompareContents0(zookeeper);
            }
        });
        Assert.assertTrue(pass);
    }

    private void awaitPinpointClientConnected(final ClusterConnectionManager connectionManager) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !connectionManager.getClusterList().isEmpty();
            }
        });
        Assert.assertTrue(pass);
    }

    private void getNodeAndCompareContents(ZooKeeper zookeeper) throws KeeperException, InterruptedException {
        LOGGER.debug("getNodeAndCompareContents() {}", CLUSTER_NODE_PATH);

        byte[] contents = zookeeper.getData(CLUSTER_NODE_PATH, null, null);

        String[] registeredIplist = new String(contents).split("\r\n");

        List<String> ipList = NetUtils.getLocalV4IpList();

        Assert.assertEquals(registeredIplist.length, ipList.size());

        for (String ip : registeredIplist) {
            Assert.assertTrue(ipList.contains(ip));
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

    private void closePinpointSocket(PinpointClientFactory clientFactory, PinpointClient client) {
        if (client != null) {
            client.close();
        }

        if (clientFactory != null) {
            clientFactory.release();
        }
    }

}
