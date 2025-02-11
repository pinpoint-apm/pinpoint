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
import com.navercorp.pinpoint.realtime.collector.sink.ActiveThreadCountPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.ActiveThreadDumpPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.ActiveThreadLightDumpPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.EchoPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.Publisher;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final SinkRepository<ActiveThreadCountPublisher> activeThreadCountSinkRepo;
    private final SinkRepository<ActiveThreadDumpPublisher> activeThreadDumpSinkRepo;
    private final SinkRepository<ActiveThreadLightDumpPublisher> activeThreadLightDumpSinkRepo;
    private final SinkRepository<EchoPublisher> echoSinkRepo;

    public GrpcCommandService(
            GrpcAgentConnectionRepository agentConnectionRepository,
            SinkRepository<ActiveThreadCountPublisher> activeThreadCountSinkRepo,
            SinkRepository<ActiveThreadDumpPublisher> activeThreadDumpSinkRepo,
            SinkRepository<ActiveThreadLightDumpPublisher> activeThreadLightDumpSinkRepo,
            SinkRepository<EchoPublisher> echoSinkRepo
    ) {
        this.agentConnectionRepository = Objects.requireNonNull(agentConnectionRepository, "clusterPointRepository");
        this.activeThreadCountSinkRepo = Objects.requireNonNull(activeThreadCountSinkRepo, "activeThreadCountSinkRepo");
        this.activeThreadDumpSinkRepo = Objects.requireNonNull(activeThreadDumpSinkRepo, "activeThreadDumpSinkRepo");
        this.activeThreadLightDumpSinkRepo = Objects.requireNonNull(activeThreadLightDumpSinkRepo, "activeThreadLightDumpSinkRepo");
        this.echoSinkRepo = Objects.requireNonNull(echoSinkRepo, "echoSinkRepo");
    }

    @Override
    @SuppressWarnings("deprecation")
    public StreamObserver<PCmdMessage> handleCommand(StreamObserver<PCmdRequest> requestObserver) {
        final Context context = Context.current();
        Long transportId = getTransportIdFromContext(context);

        final Header header = ServerContext.getAgentInfo(context);
        ClusterKey clusterKey = getClusterKeyFromContext(header);

        logger.info("{} => local. handleCommand(). transportId:{}.", clusterKey, transportId);

        List<Integer> supportCommandCodeList = header.getSupportCommandCodeList();
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
                            buildAgentConnection(header, serverCallStreamObserver, supportCommandServiceKeyList);

                    if (connRef.compareAndSet(null, conn)) {
                        GrpcCommandService.this.agentConnectionRepository.add(conn);
                    }
                } else if (value.hasFailMessage()) {
                    handleFail(value);
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
        final Context context = Context.current();
        Long transportId = getTransportIdFromContext(context);

        final Header header = ServerContext.getAgentInfo(context);
        ClusterKey clusterKey = getClusterKeyFromContext(header);
        List<Integer> supportCommandCodeList = header.getSupportCommandCodeList();
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
            GrpcAgentConnection conn = buildAgentConnection(header, serverCallStreamObserver, supportCommandCodeList);
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
                    handleFail(value);
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

    private GrpcAgentConnection buildAgentConnection(Header header,
            ServerCallStreamObserver<PCmdRequest> requestObserver,
            List<Integer> supportCommandServiceCodeList
    ) {
        return new GrpcAgentConnection(
                getRemoteAddressFromContext(),
                getClusterKeyFromContext(header),
                requestObserver,
                supportCommandServiceCodeList
        );
    }

    private void handleFail(PCmdMessage value) {
        final PCmdResponse failMessage = value.getFailMessage();
        final long sinkId = failMessage.getResponseId();
        final Exception exception = new RuntimeException(failMessage.getMessage().getValue());
        activeThreadCountSinkRepo.error(sinkId, exception);
        activeThreadDumpSinkRepo.error(sinkId, exception);
        activeThreadLightDumpSinkRepo.error(sinkId, exception);
        echoSinkRepo.error(sinkId, exception);
    }

    private void handleOnError(Throwable t, GrpcAgentConnection conn) {
        if (conn == null) {
            logger.warn("Command error before establishment");
            return;
        }

        final Status status = Status.fromThrowable(t);
        Metadata metadata = Status.trailersFromThrowable(t);

        logger.info("Failed to command stream, {} => local, {} {}",
                conn.getClusterKey(), status, metadata);

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
        final EchoPublisher publisher = this.echoSinkRepo.get(sinkId);
        emitMono(response, responseObserver, publisher);
        this.echoSinkRepo.invalidate(sinkId);
    }

    @Override
    public void commandActiveThreadDump(PCmdActiveThreadDumpRes response, StreamObserver<Empty> responseObserver) {
        long sinkId = response.getCommonResponse().getResponseId();
        final ActiveThreadDumpPublisher publisher = this.activeThreadDumpSinkRepo.get(sinkId);
        emitMono(response, responseObserver, publisher);
        this.activeThreadDumpSinkRepo.invalidate(sinkId);
    }

    @Override
    public void commandActiveThreadLightDump(PCmdActiveThreadLightDumpRes response, StreamObserver<Empty> responseObserver) {
        long sinkId = response.getCommonResponse().getResponseId();
        final ActiveThreadLightDumpPublisher publisher = this.activeThreadLightDumpSinkRepo.get(sinkId);
        emitMono(response, responseObserver, publisher);
        this.activeThreadLightDumpSinkRepo.invalidate(sinkId);
    }

    @Override
    public StreamObserver<PCmdActiveThreadCountRes> commandStreamActiveThreadCount(StreamObserver<Empty> responseObserver) {
        logger.debug("commandStreamActiveThreadCount started");

        ServerCallStreamObserver<Empty> serverResponseObserver = (ServerCallStreamObserver<Empty>) responseObserver;
        return new ActiveThreadCountResponseStreamObserver(serverResponseObserver, this.activeThreadCountSinkRepo);
    }

    private <T> void emitMono(T response, StreamObserver<Empty> responseObserver, Publisher<T> sink) {
        if (sink == null) {
            if (logger.isWarnEnabled()) {
                Header header = ServerContext.getAgentInfo();
                logger.warn("Could not find echo sink: clusterKey = {}", getClusterKeyFromContext(header));
            }
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
            return;
        }
        sink.publish(response);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private InetSocketAddress getRemoteAddressFromContext() {
        TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        return transportMetadata.getRemoteAddress();
    }

    private ClusterKey getClusterKeyFromContext(Header header) {
        return new ClusterKey(header.getApplicationName(), header.getAgentId(), header.getAgentStartTime());
    }

    private Long getTransportIdFromContext(Context context) {
        TransportMetadata transportMetadata = ServerContext.getTransportMetadata(context);
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
            Status status = Status.fromThrowable(t);
            logger.debug("onError:{}", status);
        }

        @Override
        public void onCompleted() {
            logger.debug("onCompleted");
        }

    }
}
