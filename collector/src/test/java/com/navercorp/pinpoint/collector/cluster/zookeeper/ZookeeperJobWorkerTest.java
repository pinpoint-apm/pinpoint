package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @Author Taejin Koo
 */
public class ZookeeperJobWorkerTest {

    private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
    private static final String PINPOINT_COLLECTOR_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/collector";

    private static final String IDENTIFIER = "ZookeeperJobWorkerTest";
    private static final String PATH = "/pinpoint-cluster/collector/" + IDENTIFIER;

    private static final String EMPTY_STRING = "";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void test1() throws Exception {
        MockZookeeperClient zookeeperClient = new MockZookeeperClient();
        ZookeeperJobWorker zookeeperWorker = new ZookeeperJobWorker(zookeeperClient, IDENTIFIER);
        zookeeperWorker.start();

        try {
            int random = ThreadLocalRandom.current().nextInt(1, 10);
            for (int i = 0; i < random; i++) {
                PinpointServer mockServer = createMockPinpointServer("app" + i, "agent" + i, System.currentTimeMillis());
                zookeeperWorker.addPinpointServer(mockServer);
            }

            Thread.sleep(100);
            Assert.assertEquals(random, getServerData(zookeeperClient).size());
        } finally {
            zookeeperWorker.stop();
        }
    }

    @Test
    public void test2() throws Exception {
        MockZookeeperClient zookeeperClient = new MockZookeeperClient();
        ZookeeperJobWorker zookeeperWorker = new ZookeeperJobWorker(zookeeperClient, IDENTIFIER);
        zookeeperWorker.start();

        try {
            PinpointServer mockServer = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            zookeeperWorker.addPinpointServer(mockServer);
            zookeeperWorker.addPinpointServer(mockServer);

            Thread.sleep(100);
            Assert.assertEquals(1, getServerData(zookeeperClient).size());

            zookeeperWorker.removePinpointServer(mockServer);

            Thread.sleep(100);
            Assert.assertEquals(0, getServerData(zookeeperClient).size());
        } finally {
            zookeeperWorker.stop();
        }
    }

    @Test
    public void test3() throws Exception {
        MockZookeeperClient zookeeperClient = new MockZookeeperClient();
        ZookeeperJobWorker zookeeperWorker = new ZookeeperJobWorker(zookeeperClient, IDENTIFIER);
        zookeeperWorker.start();

        try {
            PinpointServer mockServer = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            zookeeperWorker.addPinpointServer(mockServer);

            Thread.sleep(100);
            Assert.assertEquals(1, getServerData(zookeeperClient).size());

            zookeeperWorker.clear();

            Thread.sleep(100);
            Assert.assertEquals(0, getServerData(zookeeperClient).size());

            zookeeperWorker.addPinpointServer(mockServer);
            Thread.sleep(100);
            Assert.assertEquals(1, getServerData(zookeeperClient).size());
        } finally {
            zookeeperWorker.stop();
        }
    }

    @Test
    public void test4() throws Exception {
        MockZookeeperClient zookeeperClient = new MockZookeeperClient();
        ZookeeperJobWorker zookeeperWorker = new ZookeeperJobWorker(zookeeperClient, IDENTIFIER);
        zookeeperWorker.start();

        try {
            PinpointServer mockServer1 = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            zookeeperWorker.addPinpointServer(mockServer1);

            PinpointServer mockServer2 = createMockPinpointServer("app", "agent", System.currentTimeMillis() + 1000);
            zookeeperWorker.addPinpointServer(mockServer2);

            Thread.sleep(100);
            zookeeperWorker.removePinpointServer(mockServer1);

            Thread.sleep(100);
            Assert.assertEquals(1, getServerData(zookeeperClient).size());

        } finally {
            zookeeperWorker.stop();
        }
    }

    private PinpointServer createMockPinpointServer(String applicationName, String agentId, long startTimeStamp) {
        Map<Object, Object> properties = new HashMap<>();
        properties.put(AgentHandshakePropertyType.APPLICATION_NAME.getName(), applicationName);
        properties.put(AgentHandshakePropertyType.AGENT_ID.getName(), agentId);
        properties.put(AgentHandshakePropertyType.START_TIMESTAMP.getName(), startTimeStamp);

        PinpointServer mockServer = mock(PinpointServer.class);
        when(mockServer.getChannelProperties()).thenReturn(properties);

        return mockServer;
    }

    private List<String> getServerData(ZookeeperClient zookeeperClient) throws PinpointZookeeperException, InterruptedException {
        List<String> servers = new ArrayList<>();

        String[] allData = new String(zookeeperClient.getData(PATH)).split("\r\n");
        for (String data : allData) {
            if (!EMPTY_STRING.equals(data.trim())) {
                servers.add(data);
            }
        }

        return servers;
    }

    class MockZookeeperClient implements ZookeeperClient {

        private final byte[] EMPTY_BYTE = new byte[]{};
        private final Map<String, byte[]> contents = new HashMap<>();
        private volatile boolean connected = false;

        @Override
        public synchronized void reconnectWhenSessionExpired() {
            connected = true;
        }

        @Override
        public synchronized void createPath(String path) throws PinpointZookeeperException, InterruptedException {
            contents.put(path, EMPTY_BYTE);
        }

        @Override
        public synchronized void createPath(String path, boolean createEndNode) throws PinpointZookeeperException, InterruptedException {
            contents.put(path, EMPTY_BYTE);
        }

        @Override
        public synchronized String createNode(String znodePath, byte[] data) throws PinpointZookeeperException, InterruptedException {
            contents.put(znodePath, data);
            return "";
        }

        @Override
        public synchronized byte[] getData(String path) throws PinpointZookeeperException, InterruptedException {
            byte[] bytes = contents.get(path);
            return bytes;
        }

        @Override
        public synchronized void setData(String path, byte[] data) throws PinpointZookeeperException, InterruptedException {
            if (!contents.containsKey(path)) {
                throw new PinpointZookeeperException("can't find path.");
            }
            contents.put(path, data);
        }

        @Override
        public synchronized void delete(String path) throws PinpointZookeeperException, InterruptedException {
            contents.remove(path);
        }

        @Override
        public synchronized boolean exists(String path) throws PinpointZookeeperException, InterruptedException {
            return contents.containsKey(path);
        }

        @Override
        public synchronized boolean isConnected() {
            return connected;
        }

        @Override
        public synchronized List<String> getChildrenNode(String path, boolean watch) throws PinpointZookeeperException, InterruptedException {
            return new ArrayList<>();
        }

        @Override
        public synchronized void close() {
            connected = false;
        }

    }


}
