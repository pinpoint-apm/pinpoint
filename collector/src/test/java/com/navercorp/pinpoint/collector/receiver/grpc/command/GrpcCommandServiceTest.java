/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc.command;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.collector.cluster.ClusterPointRepository;
import com.navercorp.pinpoint.collector.cluster.zookeeper.InMemoryZookeeperClient;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperClusterService;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperProfilerClusterManager;
import com.navercorp.pinpoint.collector.receiver.grpc.RecordedStreamObserver;
import com.navercorp.pinpoint.collector.receiver.grpc.service.command.GrpcCommandService;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.DefaultTransportMetadata;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdServiceHandshake;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import io.grpc.Context;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.curator.utils.ZKPaths;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.hasSize;

/**
 * @author Taejin Koo
 */
public class GrpcCommandServiceTest {

    private ConditionFactory awaitility() {
        ConditionFactory conditionFactory = Awaitility.await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .timeout(1000, TimeUnit.MILLISECONDS);
        return conditionFactory;
    }

    @Test
    public void oldVersionHandshakeTest() throws IOException {
        ZookeeperProfilerClusterManager manager = creteMemoryClusterManager();

        ZookeeperClusterService mockClusterService = Mockito.mock(ZookeeperClusterService.class);
        Mockito.when(mockClusterService.getProfilerClusterManager()).thenReturn(manager);

        try (GrpcCommandService commandService = new GrpcCommandService(mockClusterService)) {
            TransportMetadata transportMetaData = createTransportMetaData(new InetSocketAddress("127.0.0.1", 61613), 10);
            attachContext(transportMetaData);

            attachContext(new Header("test", "agentId", "agentName", "applicationName", ServiceType.UNDEFINED.getCode(), System.currentTimeMillis(), Header.SOCKET_ID_NOT_EXIST, null));

            StreamObserver<PCmdMessage> handleMessageObserver = commandService.handleCommand(new TempServerCallStreamObserver<>());

            handleMessageObserver.onNext(createHandshakeMessage());
            awaitility().await("oldVersionHandshakeTest")
                    .until(manager::getClusterData, hasSize(1));


            assertHandleMessage(commandService, transportMetaData);
        }
    }

    @Test
    public void oldVersionHandshakeFailTest() throws IOException {
        ZookeeperProfilerClusterManager manager = creteMemoryClusterManager();

        ZookeeperClusterService mockClusterService = Mockito.mock(ZookeeperClusterService.class);
        Mockito.when(mockClusterService.getProfilerClusterManager()).thenReturn(manager);

        try (GrpcCommandService commandService = new GrpcCommandService(mockClusterService)) {
            TransportMetadata transportMetaData = createTransportMetaData(new InetSocketAddress("127.0.0.1", 61613), 10);
            attachContext(transportMetaData);
            attachContext(new Header("test", "agentId", "agentName", "applicationName", ServiceType.UNDEFINED.getCode(), System.currentTimeMillis(), Header.SOCKET_ID_NOT_EXIST, getCodeList()));

            final TempServerCallStreamObserver<PCmdRequest> requestObserver = new TempServerCallStreamObserver<>();
            StreamObserver<PCmdMessage> handleMessageObserver = commandService.handleCommand(requestObserver);

            Assert.assertThrows(ConditionTimeoutException.class, () -> {
                        Awaitility.await("oldVersionHandshakeFailTest")
                                .timeout(400, TimeUnit.MILLISECONDS)
                                .until(manager::getClusterData, hasSize(1));
                    }
            );

            Assert.assertNotNull(requestObserver.getLatestException());
        }
    }

    @Test
    public void newVersionHandshakeTest() throws IOException {
        ZookeeperProfilerClusterManager manager = creteMemoryClusterManager();

        ZookeeperClusterService mockClusterService = Mockito.mock(ZookeeperClusterService.class);
        Mockito.when(mockClusterService.getProfilerClusterManager()).thenReturn(manager);

        try (GrpcCommandService commandService = new GrpcCommandService(mockClusterService)) {
            TransportMetadata transportMetaData = createTransportMetaData(new InetSocketAddress("127.0.0.1", 61613), 10);
            attachContext(transportMetaData);
            attachContext(new Header("test", "agentId", null, "applicationName", ServiceType.UNDEFINED.getCode(), System.currentTimeMillis(), Header.SOCKET_ID_NOT_EXIST, getCodeList()));

            StreamObserver<PCmdMessage> handleMessageObserver = commandService.handleCommandV2(new TempServerCallStreamObserver<>());
            awaitility()
                    .until(manager::getClusterData, hasSize(1));

            assertHandleMessage(commandService, transportMetaData);
        }
    }

    private ZookeeperProfilerClusterManager creteMemoryClusterManager() throws IOException {
        InMemoryZookeeperClient zookeeperClient = new InMemoryZookeeperClient();
        zookeeperClient.connect();

        String path
                = ZKPaths.makePath(ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH, ZookeeperConstants.COLLECTOR_LEAF_PATH, this.getClass().getSimpleName());

        ZookeeperProfilerClusterManager manager = new ZookeeperProfilerClusterManager(zookeeperClient, path, new ClusterPointRepository());
        manager.start();
        return manager;
    }

    private void assertHandleMessage(GrpcCommandService commandService, TransportMetadata transportMetaData) {
        RecordedStreamObserver<Empty> recordedStreamObserver = new RecordedStreamObserver<>();
        PCmdEchoResponse defaultInstance = PCmdEchoResponse.getDefaultInstance();
        commandService.commandEcho(defaultInstance, recordedStreamObserver);
        Assert.assertNull(recordedStreamObserver.getLatestThrowable());

        attachContext(createTransportMetaData(transportMetaData.getRemoteAddress(), transportMetaData.getTransportId() + 1));
        commandService.commandEcho(defaultInstance, recordedStreamObserver);
        Assert.assertNotNull(recordedStreamObserver.getLatestThrowable());

        StreamObserver<PCmdActiveThreadCountRes> pCmdActiveThreadCountResStreamObserver = commandService.commandStreamActiveThreadCount(new TempServerCallStreamObserver<>());
        Assert.assertNotNull(pCmdActiveThreadCountResStreamObserver);

        attachContext(transportMetaData);
        TempServerCallStreamObserver<Empty> streamConnectionManagerObserver = new TempServerCallStreamObserver<>();

        pCmdActiveThreadCountResStreamObserver = commandService.commandStreamActiveThreadCount(streamConnectionManagerObserver);
        Assert.assertNull(streamConnectionManagerObserver.getLatestException());

        pCmdActiveThreadCountResStreamObserver.onNext(PCmdActiveThreadCountRes.getDefaultInstance());
        Assert.assertNotNull(streamConnectionManagerObserver.getLatestException());
    }

    private TransportMetadata createTransportMetaData(InetSocketAddress remoteAddress, long transportId) {
        InetSocketAddress localAddress = InetSocketAddress.createUnresolved("127.0.0.1", 1111);
        return new DefaultTransportMetadata(this.getClass().getSimpleName(), remoteAddress, localAddress, transportId, System.currentTimeMillis(), -1L);
    }

    private void attachContext(TransportMetadata transportMetadata) {
        final Context currentContext = Context.current();
        Context newContext = currentContext.withValue(ServerContext.getTransportMetadataKey(), transportMetadata);
        newContext.attach();
    }

    private void attachContext(Header header) {
        final Context currentContext = Context.current();
        Context newContext = currentContext.withValue(ServerContext.getAgentInfoKey(), header);
        newContext.attach();
    }

    private PCmdMessage createHandshakeMessage() {
        PCmdServiceHandshake.Builder handshakeBuilder = PCmdServiceHandshake.newBuilder();
        for (TCommandType commandType : TCommandType.values()) {
            handshakeBuilder.addSupportCommandServiceKey(commandType.getCode());
        }

        PCmdMessage.Builder builder = PCmdMessage.newBuilder();
        builder.setHandshakeMessage(handshakeBuilder.build());
        return builder.build();
    }

    private List<Integer> getCodeList() {
        List<Integer> codes = new ArrayList<>();
        for (TCommandType commandType : TCommandType.values()) {
            codes.add((int) commandType.getCode());
        }
        return codes;
    }

    private static class TempServerCallStreamObserver<T> extends ServerCallStreamObserver<T> {

        private Throwable latestException;

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void setOnCancelHandler(Runnable onCancelHandler) {
        }

        @Override
        public void setCompression(String compression) {
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setOnReadyHandler(Runnable onReadyHandler) {
            onReadyHandler.run();
        }

        @Override
        public void disableAutoInboundFlowControl() {
        }

        @Override
        public void request(int count) {
        }

        @Override
        public void setMessageCompression(boolean enable) {
        }

        @Override
        public void onNext(T value) {
        }

        @Override
        public void onError(Throwable t) {
            latestException = t;
        }

        @Override
        public void onCompleted() {
        }

        public Throwable getLatestException() {
            return latestException;
        }

    }

}
