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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.jboss.netty.channel.Channel;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.web.config.WebConfig;
import com.navercorp.pinpoint.web.server.PinpointSocketManager;
import com.navercorp.pinpoint.web.util.PinpointWebTestUtils;

/**
 * @author Taejin Koo
 */
public class ClusterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterTest.class);

    // some tests may fail when executed in local environment
    // when failures happen, you have to copy pinpoint-web.properties of resource-test to resource-local. Tests will succeed.

    private static final String DEFAULT_IP = PinpointWebTestUtils.getRepresentationLocalV4Ip();

    private static String CLUSTER_NODE_PATH;

    private static int acceptorPort;
    private static int zookeeperPort;
    
    private static String zookeeperAddress;
    
    private static TestingServer ts = null;

    static PinpointSocketManager socketManager;

    @BeforeClass
    public static void setUp() throws Exception {
        acceptorPort = PinpointWebTestUtils.findAvailablePort();
        zookeeperPort = PinpointWebTestUtils.findAvailablePort(acceptorPort + 1);

        zookeeperAddress = DEFAULT_IP + ":" + zookeeperPort;
        
        CLUSTER_NODE_PATH = "/pinpoint-cluster/web/" + DEFAULT_IP + ":" + acceptorPort;
        LOGGER.info("CLUSTER_NODE_PATH:{}", CLUSTER_NODE_PATH);
        
        WebConfig config = mock(WebConfig.class);

        when(config.isClusterEnable()).thenReturn(true);
        when(config.getClusterTcpPort()).thenReturn(acceptorPort);
        when(config.getClusterZookeeperAddress()).thenReturn(zookeeperAddress);
        when(config.getClusterZookeeperRetryInterval()).thenReturn(60000);
        when(config.getClusterZookeeperSessionTimeout()).thenReturn(3000);

        socketManager = new PinpointSocketManager(config);
        socketManager.start();

        ts = createZookeeperServer(zookeeperPort);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        closeZookeeperServer(ts);
        socketManager.stop();
    }

    @Before
    public void before() throws IOException {
        ts.stop();
    }

    @Test
    public void clusterTest1() throws Exception {
        ts.restart();
        Thread.sleep(5000);

        ZooKeeper zookeeper = new ZooKeeper(zookeeperAddress, 5000, null);
        getNodeAndCompareContents(zookeeper);

        if (zookeeper != null) {
            zookeeper.close();
        }
    }

    @Test
    public void clusterTest2() throws Exception {
        ts.restart();
        Thread.sleep(5000);

        ZooKeeper zookeeper = new ZooKeeper(zookeeperAddress, 5000, null);
        getNodeAndCompareContents(zookeeper);

        ts.stop();

        Thread.sleep(5000);
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
        ts.restart();

        PinpointSocketFactory factory = null;
        PinpointSocket socket = null;

        ZooKeeper zookeeper = null;
        try {
            Thread.sleep(5000);

            zookeeper = new ZooKeeper(zookeeperAddress, 5000, null);
            getNodeAndCompareContents(zookeeper);

            Assert.assertEquals(0, socketManager.getCollectorChannelContext().size());

            factory = new PinpointSocketFactory();
            factory.setMessageListener(new SimpleListener());

            socket = factory.connect(DEFAULT_IP, acceptorPort);

            Thread.sleep(1000);

            Assert.assertEquals(1, socketManager.getCollectorChannelContext().size());

        } finally {
            closePinpointSocket(factory, socket);

            if (zookeeper != null) {
                zookeeper.close();
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
        LOGGER.info("getNodeAndCompareContents() {}", CLUSTER_NODE_PATH);

        byte[] conetents = zookeeper.getData(CLUSTER_NODE_PATH, null, null);

        String[] registeredIplist = new String(conetents).split("\r\n");

        List<String> ipList = NetUtils.getLocalV4IpList();

        Assert.assertEquals(registeredIplist.length, ipList.size());

        for (String ip : registeredIplist) {
            Assert.assertTrue(ipList.contains(ip));
        }
    }

    private void closePinpointSocket(PinpointSocketFactory factory, PinpointSocket socket) {
        if (socket != null) {
            socket.close();
        }

        if (factory != null) {
            factory.release();
        }
    }

    class SimpleListener implements MessageListener {
        @Override
        public void handleSend(SendPacket sendPacket, Channel channel) {

        }

        @Override
        public void handleRequest(RequestPacket requestPacket, Channel channel) {
            // TODO Auto-generated method stub

        }
    }

}
