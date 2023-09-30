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

package com.navercorp.pinpoint.realtime.collector.receiver.grpc;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
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
import com.navercorp.pinpoint.realtime.collector.sink.ErrorSinkRepository;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.MonoSink;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
public class GrpcCommandService extends ProfilerCommandServiceGrpc.ProfilerCommandServiceImplBase {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcAgentConnectionRepository agentConnectionRepository;
    private final ErrorSinkRepository sinkRepository;
    private final SinkRepository<FluxSink<PCmdActiveThreadCountRes>> activeThreadCountSinkRepo;
    private final SinkRepository<MonoSink<PCmdActiveThreadDumpRes>> activeThreadDumpSinkRepo;
    private final SinkRepository<MonoSink<PCmdActiveThreadLightDumpRes>> activeThreadLightDumpSinkRepo;
    private final SinkRepository<MonoSink<PCmdEchoResponse>> echoSinkRepo;

    public GrpcCommandService(
            GrpcAgentConnectionRepository agentConnectionRepository,
            ErrorSinkRepository sinkRepository,
            SinkRepository<FluxSink<PCmdActiveThreadCountRes>> activeThreadCountSinkRepo,
            SinkRepository<MonoSink<PCmdActiveThreadDumpRes>> activeThreadDumpSinkRepo,
            SinkRepository<MonoSink<PCmdActiveThreadLightDumpRes>> activeThreadLightDumpSinkRepo,
            SinkRepository<MonoSink<PCmdEchoResponse>> echoSinkRepo
    ) {
        this.agentConnectionRepository = Objects.requireNonNull(agentConnectionRepository, "clusterPointRepository");
        this.sinkRepository = Objects.requireNonNull(sinkRepository, "sinkRepository");
        this.activeThreadCountSinkRepo = Objects.requireNonNull(activeThreadCountSinkRepo, "activeThreadCountSinkRepo");
        this.activeThreadDumpSinkRepo = Objects.requireNonNull(activeThreadDumpSinkRepo, "activeThreadDumpSinkRepo");
        this.activeThreadLightDumpSinkRepo = Objects.requireNonNull(activeThreadLightDumpSinkRepo, "activeThreadLightDumpSinkRepo");
        this.echoSinkRepo = Objects.requireNonNull(echoSinkRepo, "echoSinkRepo");
    }

    @Override
    @SuppressWarnings("deprecation")
    public StreamObserver<PCmdMessage> handleCommand(StreamObserver<PCmdRequest> requestObserver) {
        Long transportId = getTransportIdFromContext();
        ClusterKey clusterKey = getClusterKeyFromContext();

        logger.info("{} => local. handleCommand(). transportId:{}.", clusterKey, transportId);

        List<Integer> supportCommandCodeList = getSupportCommandCodeListFromContext();
        if (supportCommandCodeList != Header.SUPPORT_COMMAND_CODE_LIST_NOT_EXIST) {
            logger.warn(
                    "handleCommand() not support included Header:{}. Connection will be disconnected.",
                    Header.SUPPORT_COMMAND_CODE.name()
            );

            requestObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
            return DisabledStreamObserver.instance();
        }

        AtomicReference<GrpcAgentConnection> connRef = new AtomicReference<>();
        ServerCallStreamObserver<PCmdRequest> serverCallStreamObserver =
                (ServerCallStreamObserver<PCmdRequest>) requestObserver;

        serverCallStreamObserver.setOnCancelHandler(() -> {
            GrpcAgentConnection conn = connRef.get();
            if (conn != null) {
                this.agentConnectionRepository.remove(conn);
            }
        });

        serverCallStreamObserver.setOnCloseHandler(() -> {
            GrpcAgentConnection conn = connRef.get();
            if (conn != null) {
                this.agentConnectionRepository.remove(conn);
            }
        });

        return new StreamObserver<>() {
            @Override
            public void onNext(PCmdMessage value) {
                // old operation for handshake
                if (value.hasHandshakeMessage()) {
                    List<Integer> supportCommandServiceKeyList =
                            value.getHandshakeMessage().getSupportCommandServiceKeyList();
                    GrpcAgentConnection conn =
                            buildAgentConnection(serverCallStreamObserver, supportCommandServiceKeyList);

                    if (connRef.compareAndSet(null, conn)) {
                        GrpcCommandService.this.agentConnectionRepository.add(conn);
                    }
                } else if (value.hasFailMessage()) {
                    PCmdResponse failMessage = value.getFailMessage();
                    long sinkId = failMessage.getResponseId();
                    sinkRepository.error(sinkId, new RuntimeException(failMessage.getMessage().getValue()));
                }
            }

            @Override
            public void onError(Throwable t) {
                handleOnError(t, connRef.get());
                requestObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                handleOnCompleted(connRef.get());
                requestObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<PCmdMessage> handleCommandV2(StreamObserver<PCmdRequest> requestObserver) {
        Long transportId = getTransportIdFromContext();
        ClusterKey clusterKey = getClusterKeyFromContext();
        List<Integer> supportCommandCodeList = getSupportCommandCodeListFromContext();
        logger.info(
                "{} => local. handleCommandV2(). transportId:{}, supportCommandCodeList{}",
                clusterKey,
                transportId,
                supportCommandCodeList
        );

        if (supportCommandCodeList == Header.SUPPORT_COMMAND_CODE_LIST_NOT_EXIST) {
            logger.warn(
                    "handleCommandV2() not allow empty Header:{}. Connection will be disconnected.",
                    Header.SUPPORT_COMMAND_CODE.name()
            );
            requestObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
            return DisabledStreamObserver.instance();
        }

        AtomicReference<GrpcAgentConnection> connRef = new AtomicReference<>();

        ServerCallStreamObserver<PCmdRequest> serverCallStreamObserver =
                (ServerCallStreamObserver<PCmdRequest>) requestObserver;

        serverCallStreamObserver.setOnReadyHandler(() -> {
            GrpcAgentConnection conn = buildAgentConnection(serverCallStreamObserver, supportCommandCodeList);
            if (connRef.compareAndSet(null, conn)) {
                logger.info("{} => local. ready() transportId:{}", clusterKey, transportId);
                GrpcCommandService.this.agentConnectionRepository.add(conn);
            }
        });

        serverCallStreamObserver.setOnCancelHandler(() -> {
            GrpcAgentConnection conn = connRef.get();
            if (conn != null) {
                this.agentConnectionRepository.remove(conn);
            }
        });

        serverCallStreamObserver.setOnCloseHandler(() -> {
            GrpcAgentConnection conn = connRef.get();
            if (conn != null) {
                this.agentConnectionRepository.remove(conn);
            }
        });

        return new StreamObserver<>() {
            @Override
            public void onNext(PCmdMessage value) {
                if (value.hasFailMessage()) {
                    PCmdResponse failMessage = value.getFailMessage();
                    long sinkId = failMessage.getResponseId();
                    sinkRepository.error(sinkId, new RuntimeException(failMessage.getMessage().getValue()));
                }
            }

            @Override
            public void onError(Throwable t) {
                handleOnError(t, connRef.get());
                requestObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                handleOnCompleted(connRef.get());
                requestObserver.onCompleted();
            }

        };
    }

    private GrpcAgentConnection buildAgentConnection(
            ServerCallStreamObserver<PCmdRequest> requestObserver,
            List<Integer> supportCommandServiceCodeList
    ) {
        return new GrpcAgentConnection(
                getRemoteAddressFromContext(),
                getClusterKeyFromContext(),
                requestObserver,
                supportCommandServiceCodeList
        );
    }

    private void handleOnError(Throwable t, GrpcAgentConnection conn) {
        if (conn == null) {
            logger.warn("Command error before establishment");
            return;
        }

        final StatusError statusError = StatusErrors.throwable(t);
        if (statusError.isSimpleError()) {
            logger.info("Failed to command stream, {} => local, cause={}",
                    conn.getClusterKey(), statusError.getMessage());
        } else {
            logger.warn(
                    "Failed to command stream, {} => local, cause={}",
                    conn.getClusterKey(),
                    statusError.getMessage(),
                    statusError.getThrowable()
            );
        }
    }

    private void handleOnCompleted(GrpcAgentConnection conn) {
        if (conn == null) {
            logger.warn("Command complete before establishment");
            return;
        }

        logger.info("{} => local. onCompleted", conn.getClusterKey());
    }

    @Override
    public void commandEcho(PCmdEchoResponse response, StreamObserver<Empty> responseObserver) {
        long sinkId = response.getCommonResponse().getResponseId();
        emitMono(response, responseObserver, this.echoSinkRepo.get(sinkId));
        this.echoSinkRepo.invalidate(sinkId);
    }

    @Override
    public void commandActiveThreadDump(PCmdActiveThreadDumpRes response, StreamObserver<Empty> responseObserver) {
        long sinkId = response.getCommonResponse().getResponseId();
        emitMono(response, responseObserver, this.activeThreadDumpSinkRepo.get(sinkId));
        this.activeThreadDumpSinkRepo.invalidate(sinkId);
    }

    @Override
    public void commandActiveThreadLightDump(
            PCmdActiveThreadLightDumpRes response,
            StreamObserver<Empty> responseObserver
    ) {
        long sinkId = response.getCommonResponse().getResponseId();
        emitMono(response, responseObserver, this.activeThreadLightDumpSinkRepo.get(sinkId));
        this.activeThreadLightDumpSinkRepo.invalidate(sinkId);
    }

    @Override
    public StreamObserver<PCmdActiveThreadCountRes> commandStreamActiveThreadCount(
            StreamObserver<Empty> responseObserver
    ) {
        ServerCallStreamObserver<Empty> serverResponseObserver = (ServerCallStreamObserver<Empty>) responseObserver;
        return new FluxCommandResponseObserver<>(serverResponseObserver, this.activeThreadCountSinkRepo) {
            @Override
            protected long extractSinkId(PCmdActiveThreadCountRes response) {
                return response.getCommonStreamResponse().getResponseId();
            }

            @Override
            protected int extractSequence(PCmdActiveThreadCountRes response) {
                return response.getCommonStreamResponse().getSequenceId();
            }
        };
    }

    private <T> void emitMono(T response, StreamObserver<Empty> responseObserver, MonoSink<T> sink) {
        if (sink == null) {
            logger.warn("Could not find echo sink: clusterKey = {}", getClusterKeyFromContext());
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
            return;
        }
        sink.success(response);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private InetSocketAddress getRemoteAddressFromContext() {
        TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        return transportMetadata.getRemoteAddress();
    }

    private ClusterKey getClusterKeyFromContext() {
        Header header = ServerContext.getAgentInfo();
        return new ClusterKey(header.getApplicationName(), header.getAgentId(), header.getAgentStartTime());
    }

    private List<Integer> getSupportCommandCodeListFromContext() {
        Header header = ServerContext.getAgentInfo();
        return header.getSupportCommandCodeList();
    }

    private Long getTransportIdFromContext() {
        TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        return transportMetadata.getTransportId();
    }

    private static class DisabledStreamObserver<V> implements StreamObserver<V> {

        private static final DisabledStreamObserver<?> DISABLED_INSTANCE = new DisabledStreamObserver<>();

        private final Logger logger = LogManager.getLogger(this.getClass());

        @SuppressWarnings("unchecked")
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
