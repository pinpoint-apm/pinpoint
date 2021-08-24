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

package com.navercorp.pinpoint.collector.receiver.grpc.service.command;

import com.navercorp.pinpoint.collector.cluster.AgentInfo;
import com.navercorp.pinpoint.collector.cluster.ClusterService;
import com.navercorp.pinpoint.collector.cluster.ProfilerClusterManager;
import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServer;
import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServerRepository;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.StatusError;
import com.navercorp.pinpoint.grpc.StatusErrors;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.rpc.client.RequestManager;
import com.navercorp.pinpoint.rpc.util.TimerFactory;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class GrpcCommandService extends ProfilerCommandServiceGrpc.ProfilerCommandServiceImplBase implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointGrpcServerRepository grpcServerRepository = new PinpointGrpcServerRepository();

    private final ProfilerClusterManager profilerClusterManager;
    private final Timer timer;

    private final EchoService echoService = new EchoService();
    private final ActiveThreadDumpService activeThreadDumpService = new ActiveThreadDumpService();
    private final ActiveThreadLightDumpService activeThreadLightDumpService = new ActiveThreadLightDumpService();
    private final ActiveThreadCountService activeThreadCountService = new ActiveThreadCountService();

    public GrpcCommandService(ClusterService clusterService) {
        Objects.requireNonNull(clusterService, "clusterService");
        this.profilerClusterManager = Objects.requireNonNull(clusterService.getProfilerClusterManager(), "profilerClusterManager");
        this.timer = TimerFactory.createHashedWheelTimer("GrpcCommandService-Timer", 100, TimeUnit.MILLISECONDS, 512);
    }

    @Override
    public StreamObserver<PCmdMessage> handleCommand(StreamObserver<PCmdRequest> requestObserver) {
        final Long transportId = getTransportId();
        final AgentInfo agentInfo = getAgentInfo();

        logger.info("{} => local. handleCommand(). transportId:{}.", agentInfo, transportId);

        final List<Integer> supportCommandCodeList = getSupportCommandCodeList();
        if (supportCommandCodeList != Header.SUPPORT_COMMAND_CODE_LIST_NOT_EXIST) {
            logger.warn("handleCommand() not support included Header:{}. Connection will be disconnected.", Header.SUPPORT_COMMAND_CODE.name());

            requestObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
            return DisabledStreamObserver.instance();
        }

        final PinpointGrpcServer pinpointGrpcServer = registerNewPinpointGrpcServer(requestObserver, agentInfo, transportId);
        if (pinpointGrpcServer == null) {
            return handleServerRegistrationFailed(requestObserver, agentInfo, transportId);
        }

        final ServerCallStreamObserver<PCmdRequest> serverCallStreamObserver = (ServerCallStreamObserver<PCmdRequest>) requestObserver;
        serverCallStreamObserver.setOnReadyHandler(new Runnable() {
            public void run() {
                if (serverCallStreamObserver.isReady()) {
                    logger.info("{} => local. ready() transportId:{}", agentInfo.getAgentKey(), transportId);
                    pinpointGrpcServer.connected();
                }

                pinpointGrpcServer.setOnCloseHandler(new Runnable() {
                    @Override
                    public void run() {
                        unregisterPinpointGrpcServer(transportId);
                    }
                });
            }
        });

        final StreamObserver<PCmdMessage> responseObserver = new StreamObserver<PCmdMessage>() {
            @Override
            public void onNext(PCmdMessage value) {
                // old operation for handshake
                if (value.hasHandshakeMessage()) {
                    List<Integer> supportCommandServiceKeyList = value.getHandshakeMessage().getSupportCommandServiceKeyList();
                    registerAgentCommandList(pinpointGrpcServer, supportCommandServiceKeyList);
                } else if (value.hasFailMessage()) {
                    PCmdResponse failMessage = value.getFailMessage();
                    pinpointGrpcServer.handleFail(failMessage);
                }
            }

            @Override
            public void onError(Throwable t) {
                handleOnError(t, pinpointGrpcServer, agentInfo);
            }

            @Override
            public void onCompleted() {
                handleOnCompleted(pinpointGrpcServer, agentInfo);
            }
        };
        return responseObserver;
    }

    @Override
    public StreamObserver<PCmdMessage> handleCommandV2(StreamObserver<PCmdRequest> requestObserver) {
        final Long transportId = getTransportId();
        final AgentInfo agentInfo = getAgentInfo();

        final List<Integer> supportCommandCodeList = getSupportCommandCodeList();
        logger.info("{} => local. handleCommandV2(). transportId:{}, supportCommandCodeList{}", agentInfo, transportId, supportCommandCodeList);

        if (supportCommandCodeList == Header.SUPPORT_COMMAND_CODE_LIST_NOT_EXIST) {
            logger.warn("handleCommandV2() not allow empty Header:{}. Connection will be disconnected.", Header.SUPPORT_COMMAND_CODE.name());
            requestObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
            return DisabledStreamObserver.instance();
        }
        final PinpointGrpcServer pinpointGrpcServer = registerNewPinpointGrpcServer(requestObserver, agentInfo, transportId);
        if (pinpointGrpcServer == null) {
            return handleServerRegistrationFailed(requestObserver, agentInfo, transportId);
        }

        final ServerCallStreamObserver<PCmdRequest> serverCallStreamObserver = (ServerCallStreamObserver<PCmdRequest>) requestObserver;
        serverCallStreamObserver.setOnReadyHandler(new Runnable() {
            public void run() {
                if (serverCallStreamObserver.isReady()) {
                    logger.info("{} => local. ready() transportId:{}", agentInfo.getAgentKey(), transportId);
                    pinpointGrpcServer.connected();
                    registerAgentCommandList(pinpointGrpcServer, supportCommandCodeList);
                }

                pinpointGrpcServer.setOnCloseHandler(new Runnable() {
                    @Override
                    public void run() {
                        unregisterPinpointGrpcServer(transportId);
                    }
                });
            }
        });

        final StreamObserver<PCmdMessage> responseObserver = new StreamObserver<PCmdMessage>() {
            @Override
            public void onNext(PCmdMessage value) {
                if (value.hasFailMessage()) {
                    PCmdResponse failMessage = value.getFailMessage();
                    pinpointGrpcServer.handleFail(failMessage);
                }
            }

            @Override
            public void onError(Throwable t) {
                handleOnError(t, pinpointGrpcServer, agentInfo);
            }

            @Override
            public void onCompleted() {
                handleOnCompleted(pinpointGrpcServer, agentInfo);
            }

        };
        return responseObserver;
    }

    private PinpointGrpcServer registerNewPinpointGrpcServer(StreamObserver<PCmdRequest> requestObserver, AgentInfo agentInfo, Long transportId) {
        PinpointGrpcServer pinpointGrpcServer = createPinpointGrpcServer(requestObserver, agentInfo);
        final boolean registered = grpcServerRepository.registerIfAbsent(transportId, pinpointGrpcServer);
        if (registered) {
            return pinpointGrpcServer;
        } else {
            return null;
        }
    }

    private void unregisterPinpointGrpcServer(Long transportId) {
        grpcServerRepository.unregister(transportId);
    }

    private PinpointGrpcServer createPinpointGrpcServer(StreamObserver<PCmdRequest> requestObserver, AgentInfo agentInfo) {
        final RequestManager requestManager = new RequestManager(timer, 3000);
        return new PinpointGrpcServer(getRemoteAddress(), agentInfo, requestManager, profilerClusterManager, requestObserver);
    }

    private StreamObserver<PCmdMessage> handleServerRegistrationFailed(StreamObserver<PCmdRequest> requestObserver, AgentInfo agentInfo, Long transportId) {
        logger.warn("Duplicate PCmdRequestStream found. Terminate stream. {} transportId:{}", agentInfo, transportId);
        requestObserver.onError(new StatusException(Status.ALREADY_EXISTS));
        return DisabledStreamObserver.instance();
    }

    private boolean registerAgentCommandList(PinpointGrpcServer pinpointGrpcServer, List<Integer> supportCommandServiceCodeList) {
        logger.info("{} => local. execute supportCommandServiceCodeList:{}", getAgentInfo().getAgentKey(), supportCommandServiceCodeList);
        boolean handshakeSucceed = pinpointGrpcServer.handleHandshake(supportCommandServiceCodeList);
        return handshakeSucceed;
    }

    private void handleOnError(Throwable t, PinpointGrpcServer pinpointGrpcServer, AgentInfo agentInfo) {
        Objects.requireNonNull(pinpointGrpcServer, "pinpointGrpcServer");
        Objects.requireNonNull(agentInfo, "agentInfo");

        final StatusError statusError = StatusErrors.throwable(t);
        if (statusError.isSimpleError()) {
            logger.info("Failed to command stream, {} => local, cause={}", agentInfo.getAgentKey(), statusError.getMessage());
        } else {
            logger.warn("Failed to command stream, {} => local, cause={}", agentInfo.getAgentKey(), statusError.getMessage(), statusError.getThrowable());
        }
        pinpointGrpcServer.disconnected();
    }

    private void handleOnCompleted(PinpointGrpcServer pinpointGrpcServer, AgentInfo agentInfo) {
        Objects.requireNonNull(pinpointGrpcServer, "pinpointGrpcServer");
        Objects.requireNonNull(agentInfo, "agentInfo");

        logger.info("{} => local. onCompleted", getAgentInfo().getAgentKey());

        pinpointGrpcServer.disconnected();
    }

    @Override
    public void commandEcho(PCmdEchoResponse echoResponse, StreamObserver<Empty> responseObserver) {
        final Long transportId = getTransportId();
        PinpointGrpcServer pinpointGrpcServer = grpcServerRepository.get(transportId);
        if (pinpointGrpcServer != null) {
            echoService.handle(pinpointGrpcServer, echoResponse, responseObserver);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } else {
            logger.info("{} => local. Can't find PinpointGrpcServer(transportId={})", getAgentInfo().getAgentKey(), transportId);
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }

    @Override
    public void commandActiveThreadDump(PCmdActiveThreadDumpRes activeThreadDumpRes, StreamObserver<Empty> responseObserver) {
        final Long transportId = getTransportId();
        PinpointGrpcServer pinpointGrpcServer = grpcServerRepository.get(transportId);
        if (pinpointGrpcServer != null) {
            activeThreadDumpService.handle(pinpointGrpcServer, activeThreadDumpRes, responseObserver);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } else {
            logger.info("{} => local. Can't find PinpointGrpcServer(transportId={})", getAgentInfo().getAgentKey(), transportId);
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }

    @Override
    public void commandActiveThreadLightDump(PCmdActiveThreadLightDumpRes activeThreadLightDumpResponse, StreamObserver<Empty> responseObserver) {
        final Long transportId = getTransportId();
        PinpointGrpcServer pinpointGrpcServer = grpcServerRepository.get(transportId);
        if (pinpointGrpcServer != null) {
            activeThreadLightDumpService.handle(pinpointGrpcServer, activeThreadLightDumpResponse, responseObserver);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } else {
            logger.info("{} => local. Can't find PinpointGrpcServer(transportId={})", getAgentInfo().getAgentKey(), transportId);
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }

    @Override
    public StreamObserver<PCmdActiveThreadCountRes> commandStreamActiveThreadCount(StreamObserver<Empty> streamConnectionManagerObserver) {
        final Long transportId = getTransportId();
        PinpointGrpcServer pinpointGrpcServer = grpcServerRepository.get(transportId);
        if (pinpointGrpcServer == null) {
            logger.info("{} => local. Can't find PinpointGrpcServer(transportId={})", getAgentInfo().getAgentKey(), transportId);
            streamConnectionManagerObserver.onError(new StatusException(Status.NOT_FOUND));
            return DisabledStreamObserver.instance();
        }

        try {
            return activeThreadCountService.handle(pinpointGrpcServer, streamConnectionManagerObserver);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to handle activeThreadCountService. agentKey={}, transportId={}", getAgentInfo().getAgentKey(), transportId, e);
            streamConnectionManagerObserver.onError(Status.INTERNAL.withDescription("Internal Server Error").asException());
            return DisabledStreamObserver.instance();
        }
    }

    private InetSocketAddress getRemoteAddress() {
        TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        return transportMetadata.getRemoteAddress();
    }

    private AgentInfo getAgentInfo() {
        Header header = ServerContext.getAgentInfo();
        return new AgentInfo(header.getApplicationName(), header.getAgentId(), header.getAgentStartTime());
    }

    private List<Integer> getSupportCommandCodeList() {
        Header header = ServerContext.getAgentInfo();
        return header.getSupportCommandCodeList();
    }

    private Long getTransportId() {
        TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        return transportMetadata.getTransportId();
    }

    @Override
    public void close() throws IOException {
        logger.info("close() started");
        if (timer != null) {
            timer.stop();
        }
    }

    private static class DisabledStreamObserver<V> implements StreamObserver<V> {

        private static final DisabledStreamObserver<?> DISABLED_INSTANCE = new DisabledStreamObserver();

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        public static <V> V instance() {
            return (V) DISABLED_INSTANCE;
        }

        @Override
        public void onNext(V t) {
            logger.debug("onNext:{}", t);
        }

        @Override
        public void onError(Throwable t) {
            logger.debug("onError", t);
        }

        @Override
        public void onCompleted() {
            logger.debug("onCompleted");
        }

    }

}
