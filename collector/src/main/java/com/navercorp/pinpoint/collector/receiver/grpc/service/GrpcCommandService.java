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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.cluster.AgentInfo;
import com.navercorp.pinpoint.collector.cluster.GrpcAgentConnection;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperProfilerClusterManager;
import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServer;
import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServerRepository;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.rpc.client.RequestManager;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class GrpcCommandService extends ProfilerCommandServiceGrpc.ProfilerCommandServiceImplBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointGrpcServerRepository grpcServerRepository = new PinpointGrpcServerRepository();

    private final ZookeeperProfilerClusterManager zookeeperProfilerClusterManager;
    private final Timer timer;

    public GrpcCommandService(ZookeeperProfilerClusterManager zookeeperProfilerClusterManager, Timer timer) {
        this.zookeeperProfilerClusterManager = Assert.requireNonNull(zookeeperProfilerClusterManager, "zookeeperProfilerClusterManager must not be null");
        this.timer = Assert.requireNonNull(timer, "timer must not be null");
    }

    @Override
    public StreamObserver<PCmdMessage> handleCommand(StreamObserver<PCmdRequest> requestObserver) {
        final long transportId = getTransportId();
        final AgentInfo agentInfo = getAgentInfo();

        logger.debug("handleCommand() agentInfo:{}, transportId:{}", agentInfo, transportId);

        RequestManager requestManager = new RequestManager(timer, 3000);
        final PinpointGrpcServer pinpointGrpcServer = new PinpointGrpcServer(agentInfo, requestManager, requestObserver);

        boolean registered = grpcServerRepository.registerIfAbsent(transportId, pinpointGrpcServer);
        if (!registered) {
            requestObserver.onError(new StatusException(Status.ALREADY_EXISTS));
            return DisabledStreamObserver.DISABLED_INSTANCE;
        }

        final ServerCallStreamObserver<PCmdRequest> serverCallStreamObserver = (ServerCallStreamObserver<PCmdRequest>) requestObserver;
        serverCallStreamObserver.setOnReadyHandler(new Runnable() {
            public void run() {
                if (serverCallStreamObserver.isReady()) {
                    logger.info("ready() agentInfo:{}, transportId:{}", agentInfo, transportId);
                    pinpointGrpcServer.connected();
                    serverCallStreamObserver.request(1);
                }
            }
        });
        serverCallStreamObserver.setOnCancelHandler(new Runnable() {
            @Override
            public void run() {
                if (serverCallStreamObserver.isCancelled()) {
                    pinpointGrpcServer.disconnected();
                }
            }
        });

        StreamObserver<PCmdMessage> streamObserver = new StreamObserver<PCmdMessage>() {
            @Override
            public void onNext(PCmdMessage value) {
                try {
                    if (value.hasHandshakeMessage()) {
                        List<Integer> supportCommandServiceKeyList = value.getHandshakeMessage().getSupportCommandServiceKeyList();
                        boolean handshakeSucceed = pinpointGrpcServer.handleHandshake(supportCommandServiceKeyList);
                        if (handshakeSucceed) {
                            GrpcAgentConnection grpcAgentConnection = new GrpcAgentConnection(pinpointGrpcServer, supportCommandServiceKeyList);
                            zookeeperProfilerClusterManager.register(grpcAgentConnection);
                        }
                    } else if (value.hasFailMessage()) {
                        PCmdResponse failMessage = value.getFailMessage();
                        pinpointGrpcServer.handleFail(failMessage);
                    }
                } finally {
                    serverCallStreamObserver.request(1);
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.info("onError:{}", t);
                pinpointGrpcServer.disconnected();
            }

            @Override
            public void onCompleted() {
                logger.info("onCompleted");
                pinpointGrpcServer.close();
            }

        };
        return streamObserver;
    }

    @Override
    public void commandEcho(PCmdEchoResponse echoResponse, StreamObserver<Empty> responseObserver) {
        final long transportId = getTransportId();
        PinpointGrpcServer pinpointGrpcServer = grpcServerRepository.get(transportId);
        if (pinpointGrpcServer != null) {
            pinpointGrpcServer.handleMessage(echoResponse.getCommonResponse().getResponseId(), echoResponse);
        } else {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }

    private AgentInfo getAgentInfo() {
        AgentHeaderFactory.Header header = ServerContext.getAgentInfo();
        return new AgentInfo(header.getApplicationName(), header.getAgentId(), header.getAgentStartTime());
    }

    private long getTransportId() {
        TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        return transportMetadata.getTransportId();
    }

    private static class DisabledStreamObserver implements StreamObserver<PCmdMessage> {

        private static final DisabledStreamObserver DISABLED_INSTANCE = new DisabledStreamObserver();

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public void onNext(PCmdMessage value) {
            logger.debug("onNext:{}", value);
        }

        @Override
        public void onError(Throwable t) {
            logger.debug("onError:{}", t);
        }

        @Override
        public void onCompleted() {
            logger.debug("onCompleted:");
        }

    }

}
