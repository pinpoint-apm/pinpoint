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

import com.navercorp.pinpoint.collector.cluster.ClusterPointRepository;
import com.navercorp.pinpoint.collector.cluster.ClusterPointStateChangedEventHandler;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import org.apache.curator.utils.ZKPaths;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class ZookeeperJobWorkerTest {

    private static final String IDENTIFIER = "ZookeeperJobWorkerTest";
    private static final String PATH =
            ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.COLLECTOR_LEAF_PATH, IDENTIFIER);

    private final ChannelPropertiesFactory channelPropertiesFactory = new ChannelPropertiesFactory();

    private ConditionFactory awaitility() {
        return Awaitility.await()
                .pollDelay(50, TimeUnit.MILLISECONDS)
                .timeout(3000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void test1() throws Exception {
        InMemoryZookeeperClient zookeeperClient = new InMemoryZookeeperClient(true);
        zookeeperClient.connect();

        ZookeeperProfilerClusterManager manager = new ZookeeperProfilerClusterManager(zookeeperClient, PATH, new ClusterPointRepository<>());
        manager.start();

        ClusterPointStateChangedEventHandler clusterPointStateChangedEventHandler = new ClusterPointStateChangedEventHandler(channelPropertiesFactory, manager);

        try {
            int random = ThreadLocalRandom.current().nextInt(10, 20);
            for (int i = 0; i < random; i++) {
                PinpointServer mockServer = createMockPinpointServer("app" + i, "agent" + i, System.currentTimeMillis());
                clusterPointStateChangedEventHandler.stateUpdated(mockServer, SocketStateCode.RUN_DUPLEX);
            }

            waitZookeeperServerData(random, zookeeperClient);
            assertThat(manager.getClusterData()).hasSize(random);
        } finally {
            manager.stop();
        }
    }

    @Test
    public void test2() throws Exception {
        InMemoryZookeeperClient zookeeperClient = new InMemoryZookeeperClient(true);
        zookeeperClient.connect();

        ZookeeperProfilerClusterManager manager = new ZookeeperProfilerClusterManager(zookeeperClient, PATH, new ClusterPointRepository<>());
        manager.start();

        ClusterPointStateChangedEventHandler clusterPointStateChangedEventHandler = new ClusterPointStateChangedEventHandler(channelPropertiesFactory, manager);

        try {
            PinpointServer mockServer = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            clusterPointStateChangedEventHandler.stateUpdated(mockServer, SocketStateCode.RUN_DUPLEX);
            waitZookeeperServerData(1, zookeeperClient);
            assertThat(manager.getClusterData()).hasSize(1);

            clusterPointStateChangedEventHandler.stateUpdated(mockServer, SocketStateCode.CLOSED_BY_CLIENT);
            waitZookeeperServerData(0, zookeeperClient);
            assertThat(manager.getClusterData()).isEmpty();
        } finally {
            manager.stop();
        }
    }

    @Test
    public void test3() throws Exception {
        InMemoryZookeeperClient zookeeperClient = new InMemoryZookeeperClient(true);
        zookeeperClient.connect();

        ZookeeperProfilerClusterManager manager = new ZookeeperProfilerClusterManager(zookeeperClient, PATH, new ClusterPointRepository<>());
        manager.start();

        ClusterPointStateChangedEventHandler clusterPointStateChangedEventHandler = new ClusterPointStateChangedEventHandler(channelPropertiesFactory, manager);

        try {
            PinpointServer mockServer = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            clusterPointStateChangedEventHandler.stateUpdated(mockServer, SocketStateCode.RUN_DUPLEX);
            waitZookeeperServerData(1, zookeeperClient);
            assertThat(manager.getClusterData()).hasSize(1);

            zookeeperClient.createPath(PATH);
            CreateNodeMessage createNodeMessage = new CreateNodeMessage(PATH, new byte[0]);
            try {
                zookeeperClient.createOrSetNode(createNodeMessage);
            } catch (Exception ignored) {
            }

            try {
                zookeeperClient.createOrSetNode(createNodeMessage);
            } catch (Exception ignored) {
            }

            waitZookeeperServerData(0, zookeeperClient);
            assertThat(manager.getClusterData()).isEmpty();

            manager.refresh();
            waitZookeeperServerData(1, zookeeperClient);
            assertThat(manager.getClusterData()).hasSize(1);
        } finally {
            manager.stop();
        }
    }

    @Test
    public void test4() throws Exception {
        InMemoryZookeeperClient zookeeperClient = new InMemoryZookeeperClient(true);
        zookeeperClient.connect();

        ZookeeperProfilerClusterManager manager = new ZookeeperProfilerClusterManager(zookeeperClient, PATH, new ClusterPointRepository<>());
        manager.start();

        ClusterPointStateChangedEventHandler clusterPointStateChangedEventHandler = new ClusterPointStateChangedEventHandler(channelPropertiesFactory, manager);

        try {
            PinpointServer mockServer1 = createMockPinpointServer("app", "agent", System.currentTimeMillis());
            clusterPointStateChangedEventHandler.stateUpdated(mockServer1, SocketStateCode.RUN_DUPLEX);

            PinpointServer mockServer2 = createMockPinpointServer("app", "agent", System.currentTimeMillis() + 1000);
            clusterPointStateChangedEventHandler.stateUpdated(mockServer2, SocketStateCode.RUN_DUPLEX);

            waitZookeeperServerData(2, zookeeperClient);
            assertThat(manager.getClusterData()).hasSize(2);

            clusterPointStateChangedEventHandler.stateUpdated(mockServer1, SocketStateCode.CLOSED_BY_SERVER);
            waitZookeeperServerData(1, zookeeperClient);
            assertThat(manager.getClusterData()).hasSize(1);
        } finally {
            manager.stop();
        }
    }

    private PinpointServer createMockPinpointServer(String applicationName, String agentId, long startTimeStamp) {
        Map<Object, Object> properties = new HashMap<>();
        properties.put(HandshakePropertyType.APPLICATION_NAME.getName(), applicationName);
        properties.put(HandshakePropertyType.AGENT_ID.getName(), agentId);
        properties.put(HandshakePropertyType.START_TIMESTAMP.getName(), startTimeStamp);
        properties.put(HandshakePropertyType.VERSION.getName(), Version.VERSION);

        PinpointServer mockServer = mock(PinpointServer.class);
        when(mockServer.getChannelProperties()).thenReturn(properties);
        when(mockServer.getCurrentStateCode()).thenReturn(SocketStateCode.RUN_DUPLEX);

        return mockServer;
    }

    private List<String> getServerData(ZookeeperClient zookeeperClient) throws PinpointZookeeperException, InterruptedException {
        final String clusterString = BytesUtils.toString(zookeeperClient.getData(PATH));
        return decodeServerData(clusterString);
    }

    private List<String> decodeServerData(String serverData) {
        if (serverData == null) {
            return Collections.emptyList();
        }

        final String[] tokenArray = org.springframework.util.StringUtils.tokenizeToStringArray(serverData, "\r\n");
        return List.of(tokenArray);
    }

    private void waitZookeeperServerData(final int expectedServerDataCount, final InMemoryZookeeperClient zookeeperClient) {
        awaitility()
                .untilAsserted(() -> assertThat(getServerData(zookeeperClient)).hasSize(expectedServerDataCount));
    }
}
