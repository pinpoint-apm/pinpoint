/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.BadOperationException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class ZookeeperJobWorkerTest {

    private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
    private static final String PINPOINT_COLLECTOR_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/collector";

    private static final String IDENTIFIER = "ZookeeperJobWorkerTest";
    private static final String PATH = "/pinpoint-cluster/collector/" + IDENTIFIER;

    private static final String EMPTY_STRING = "";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(50, 3000);

    @Test
    public void test1() throws Exception {
        MockZookeeperClient zookeeperClient = new MockZookeeperClient();
        zookeeperClient.connect();

        ZookeeperJobWorker zookeeperWorker = new ZookeeperJobWorker(zookeeperClient, IDENTIFIER);
        zookeeperWorker.start();
        // To check for handling when multiple started. (goal: nothing happen)
        zookeeperWorker.start();

        try {
            int random = ThreadLocalRandom.current().nextInt(10, 20);
            for (int i = 0; i < random; i++) {
                PinpointServer mockServer = createMockPinpointServer("app" + i, "agent" + i, System.currentTimeMillis());
                zookeeperWorker.addPinpointServer(mockServer);
            }

            waitZookeeperServerData(random, zookeeperClient);
            Assert.assertEquals(random, zookeeperWorker.getClusterList().size());
        } finally {
            zookeeperWorker.stop();
        }
    }

    @Test
    public void test2() throws Exception {
        MockZookeeperClient zookeeperClient = new MockZookeeperClient();
        zookeeperClient.connect();

        ZookeeperJobWorker zookeeperWorker = new ZookeeperJobWorker(zookeeperClient, IDENTIFIER);
        zookeeperWorker.start();

        try {
            PinpointServer mockServer = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            zookeeperWorker.addPinpointServer(mockServer);
            zookeeperWorker.addPinpointServer(mockServer);
            waitZookeeperServerData(1, zookeeperClient);
            Assert.assertEquals(1, zookeeperWorker.getClusterList().size());

            zookeeperWorker.removePinpointServer(mockServer);
            waitZookeeperServerData(0, zookeeperClient);
            Assert.assertEquals(0, zookeeperWorker.getClusterList().size());
        } finally {
            zookeeperWorker.stop();
        }
    }

    @Test
    public void test3() throws Exception {
        MockZookeeperClient zookeeperClient = new MockZookeeperClient();
        zookeeperClient.connect();

        ZookeeperJobWorker zookeeperWorker = new ZookeeperJobWorker(zookeeperClient, IDENTIFIER);
        zookeeperWorker.start();

        try {
            PinpointServer mockServer = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            zookeeperWorker.addPinpointServer(mockServer);
            waitZookeeperServerData(1, zookeeperClient);
            Assert.assertEquals(1, zookeeperWorker.getClusterList().size());

            zookeeperWorker.clear();
            waitZookeeperServerData(0, zookeeperClient);
            Assert.assertEquals(0, zookeeperWorker.getClusterList().size());

            zookeeperWorker.addPinpointServer(mockServer);
            waitZookeeperServerData(1, zookeeperClient);
            Assert.assertEquals(1, zookeeperWorker.getClusterList().size());
        } finally {
            zookeeperWorker.stop();
        }
    }

    @Test
    public void test4() throws Exception {
        MockZookeeperClient zookeeperClient = new MockZookeeperClient();
        zookeeperClient.connect();

        ZookeeperJobWorker zookeeperWorker = new ZookeeperJobWorker(zookeeperClient, IDENTIFIER);
        zookeeperWorker.start();

        try {
            PinpointServer mockServer1 = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            zookeeperWorker.addPinpointServer(mockServer1);

            PinpointServer mockServer2 = createMockPinpointServer("app", "agent", System.currentTimeMillis() + 1000);
            zookeeperWorker.addPinpointServer(mockServer2);

            waitZookeeperServerData(2, zookeeperClient);
            Assert.assertEquals(2, zookeeperWorker.getClusterList().size());

            zookeeperWorker.removePinpointServer(mockServer1);
            waitZookeeperServerData(1, zookeeperClient);
            Assert.assertEquals(1, zookeeperWorker.getClusterList().size());
        } finally {
            zookeeperWorker.stop();
        }
    }

    private PinpointServer createMockPinpointServer(String applicationName, String agentId, long startTimeStamp) {
        Map<Object, Object> properties = new HashMap<>();
        properties.put(HandshakePropertyType.APPLICATION_NAME.getName(), applicationName);
        properties.put(HandshakePropertyType.AGENT_ID.getName(), agentId);
        properties.put(HandshakePropertyType.START_TIMESTAMP.getName(), startTimeStamp);

        PinpointServer mockServer = mock(PinpointServer.class);
        when(mockServer.getChannelProperties()).thenReturn(properties);

        return mockServer;
    }

    private List<String> getServerData(ZookeeperClient zookeeperClient) throws PinpointZookeeperException, InterruptedException {
        final String clusterString = BytesUtils.toString(zookeeperClient.getData(PATH));
        return decodeServerData(clusterString);
    }

    private List<String> decodeServerData(String serverData) throws PinpointZookeeperException, InterruptedException {
        if (serverData == null) {
            return Collections.emptyList();
        }

        final String[] tokenArray = org.springframework.util.StringUtils.tokenizeToStringArray(serverData, "\r\n");
        return Arrays.asList(tokenArray);
    }

    private void waitZookeeperServerData(final int expectedServerDataCount, final MockZookeeperClient zookeeperClient) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                try {
                    return expectedServerDataCount == getServerData(zookeeperClient).size();
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
                return false;
            }
        });

        Assert.assertTrue(pass);
    }

    class MockZookeeperClient implements ZookeeperClient {

        private final AtomicInteger intAdder = new AtomicInteger(0);

        private final byte[] EMPTY_BYTE = new byte[]{};
        private final Map<String, byte[]> contents = new HashMap<>();
        private volatile boolean connected = false;

        @Override
        public void connect() throws IOException {
            connected = true;
        }

        @Override
        public synchronized void createPath(String value) throws PinpointZookeeperException, InterruptedException {
            ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(value);
            contents.put(pathAndNode.getPath(), EMPTY_BYTE);
        }

        @Override
        public synchronized String createNode(String zNodePath, byte[] data) throws PinpointZookeeperException, InterruptedException {
            byte[] bytes = contents.putIfAbsent(zNodePath, data);
            if (bytes != null) {
                throw new BadOperationException("node already exist");
            }
            return zNodePath;
        }

        @Override
        public String createOrSetNode(String path, byte[] payload) throws PinpointZookeeperException, KeeperException, InterruptedException {
            if (intAdder.incrementAndGet() % 2 == 1) {
                throw new PinpointZookeeperException("exception");
            }

            contents.put(path, payload);
            return path;
        }

        @Override
        public synchronized byte[] getData(String path) throws PinpointZookeeperException, InterruptedException {
            byte[] bytes = contents.get(path);
            return bytes;
        }

        @Override
        public byte[] getData(String path, boolean watch) throws PinpointZookeeperException, InterruptedException {
            return contents.get(path);
        }

        @Override
        public synchronized void delete(String path) throws PinpointZookeeperException, InterruptedException {
            contents.remove(path);
        }

        @Override
        public synchronized boolean isConnected() {
            return connected;
        }

        @Override
        public List<String> getChildNodeList(String path, boolean watch) {
            return new ArrayList<>();
        }

        @Override
        public synchronized void close() {
            connected = false;
        }

    }


}
