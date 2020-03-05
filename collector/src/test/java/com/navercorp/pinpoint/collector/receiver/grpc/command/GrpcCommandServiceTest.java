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
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.DefaultTransportMetadata;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdServiceHandshake;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import io.grpc.Context;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public class GrpcCommandServiceTest {

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 1000);

    @Test
    public void simpleTest() throws IOException {
        InMemoryZookeeperClient zookeeperClient = new InMemoryZookeeperClient();
        zookeeperClient.connect();


        ZookeeperProfilerClusterManager manager = new ZookeeperProfilerClusterManager(zookeeperClient, this.getClass().getSimpleName(), new ClusterPointRepository());
        manager.start();

        ZookeeperClusterService mockClusterService = Mockito.mock(ZookeeperClusterService.class);
        Mockito.when(mockClusterService.getProfilerClusterManager()).thenReturn(manager);

        GrpcCommandService commandService = new GrpcCommandService(mockClusterService);

        try {
            TransportMetadata transportMetaData = createTransportMetaData(new InetSocketAddress("127.0.0.1", 61613), 10);
            attachContext(transportMetaData);
            attachContext(new Header("agent", "applicationName", System.currentTimeMillis(), Header.SOCKET_ID_NOT_EXIST));

            StreamObserver<PCmdMessage> handleMessageObserver = commandService.handleCommand(new TempServerCallStreamObserver<PCmdRequest>());

            handleMessageObserver.onNext(createHandshakeMessage());

            awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return manager.getClusterData().size() == 1;
                }
            });

            RecordedStreamObserver<Empty> recordedStreamObserver = new RecordedStreamObserver<>();
            PCmdEchoResponse defaultInstance = PCmdEchoResponse.getDefaultInstance();
            commandService.commandEcho(defaultInstance, recordedStreamObserver);
            Assert.assertNull(recordedStreamObserver.getLatestThrowable());

            attachContext(createTransportMetaData(transportMetaData.getRemoteAddress(), transportMetaData.getTransportId() + 1));
            commandService.commandEcho(defaultInstance, recordedStreamObserver);
            Assert.assertNotNull(recordedStreamObserver.getLatestThrowable());

            StreamObserver<PCmdActiveThreadCountRes> pCmdActiveThreadCountResStreamObserver = commandService.commandStreamActiveThreadCount(new TempServerCallStreamObserver<Empty>());
            Assert.assertNotNull(pCmdActiveThreadCountResStreamObserver);

            attachContext(transportMetaData);
            TempServerCallStreamObserver<Empty> streamConnectionManagerObserver = new TempServerCallStreamObserver<>();

            pCmdActiveThreadCountResStreamObserver = commandService.commandStreamActiveThreadCount(streamConnectionManagerObserver);
            Assert.assertNull(streamConnectionManagerObserver.getLatestException());

            pCmdActiveThreadCountResStreamObserver.onNext(PCmdActiveThreadCountRes.getDefaultInstance());
            Assert.assertNotNull(streamConnectionManagerObserver.getLatestException());
        } finally {
            commandService.close();
        }
    }

    private TransportMetadata createTransportMetaData(InetSocketAddress remoteAddress, long transportId) {
        return new DefaultTransportMetadata(this.getClass().getSimpleName(), remoteAddress, transportId, System.currentTimeMillis());
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
