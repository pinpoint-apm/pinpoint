/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.collector.grpc.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.collector.grpc.lifecycle.PingSession;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.io.MessageType;
import com.navercorp.pinpoint.common.server.io.MessageTypes;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.server.TransportMutableContext;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PPing;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.stream.NoopStreamObserver;
import com.navercorp.pinpoint.io.request.ServiceUidSuppliers;
import com.navercorp.pinpoint.io.request.UidFetcher;
import com.navercorp.pinpoint.io.request.UidFetcherService;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author jaehong.kim
 */
public class AgentService extends AgentGrpc.AgentImplBase {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger tLogger = ThrottledLogger.getLogger(logger, 100);

    private final RequestResponseHandler<PAgentInfo, PResult> handler;

    private final PingEventHandler pingEventHandler;

    private final UidFetcherService uidFetcherService;

    private final JobRunner jobRunner;
    private final Executor executor;

    public AgentService(RequestResponseHandler<PAgentInfo, PResult> handler,
                        PingEventHandler pingEventHandler,
                        UidFetcherService uidFetcherService,
                        Executor executor,
                        ServerRequestFactory requestFactory,
                        ServerResponseFactory responseFactory) {
        this.handler = Objects.requireNonNull(handler, "handler");

        this.pingEventHandler = Objects.requireNonNull(pingEventHandler, "pingEventHandler");
        this.uidFetcherService = Objects.requireNonNull(uidFetcherService, "uidFetcherService");
        Objects.requireNonNull(executor, "executor");
        this.jobRunner = new JobRunner(logger, requestFactory, responseFactory);
        this.executor = Context.currentContextExecutor(executor);
    }

    @Override
    public void requestAgentInfo(PAgentInfo agentInfo, StreamObserver<PResult> responseObserver) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
        }

        final MessageType messageType = MessageTypes.AGENT_INFO;
        doExecute(new Runnable() {
            @Override
            public void run() {
                jobRunner.execute(messageType, agentInfo, responseObserver, handler::handleRequest);

                // Update service type in transport context
                TransportMutableContext transportMutableContext = ServerContext.getTransportMutableContext();
                transportMutableContext.setServiceType(agentInfo.getServiceType());
                TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
                pingEventHandler.update(transportMetadata.getTransportId());
            }
        }, messageType);
    }

    void doExecute(Runnable runnable, MessageType messageType) {
        try {
            executor.execute(runnable);
        } catch (RejectedExecutionException ree) {
            final Header header = ServerContext.getAgentInfo();
            // Defense code
            logger.warn("Failed to request. Rejected execution, {} {} executor={}", messageType, header, executor);
        }
    }

    @Override
    public StreamObserver<PPing> pingSession(final StreamObserver<PPing> response) {
        Context context = Context.current();
        Header header = ServerContext.getAgentInfo(context);
        TransportMetadata transport = ServerContext.getTransportMetadata(context);
        TransportMutableContext transportServiceContext = ServerContext.getTransportMutableContext(context);
        final ServerCallStreamObserver<PPing> responseObserver = (ServerCallStreamObserver<PPing>) response;

        UidFetcher uidFetcher = uidFetcherService.newUidFetcher();
        ServiceUid serviceUid;
        try {
            serviceUid = resolveServiceUid(header, uidFetcher);
        } catch (StatusRuntimeException e) {
            if (!responseObserver.isCancelled()) {
                responseObserver.onError(e);
            }
            return NoopStreamObserver.instance();
        }

        PingSession pingSession = this.pingEventHandler.newPingSession(transport.getTransportId(), header, transportServiceContext, serviceUid);
        responseObserver.setOnCancelHandler(() -> disconnect(pingSession));

        return new StreamObserver<>() {
            private final ThrottledLogger thLogger = ThrottledLogger.getLogger(AgentService.this.logger, 100);

            @Override
            public void onNext(PPing ping) {
                AgentService.this.pingEventHandler.ping(pingSession);
                if (logger.isDebugEnabled()) {
                    thLogger.debug("PingSession:{} onNext:PPing", pingSession);
                }
                if (responseObserver.isReady()) {
                    PPing replay = newPing();
                    responseObserver.onNext(replay);
                } else {
                    thLogger.warn("ping message is ignored: stream is not ready: {}", pingSession.getHeader());
                }
            }

            private PPing newPing() {
                return PPing.getDefaultInstance();
            }

            @Override
            public void onError(Throwable t) {
                final Status status = Status.fromThrowable(t);
                final Metadata metadata = Status.trailersFromThrowable(t);
                if (thLogger.isInfoEnabled()) {
                    thLogger.info("Failed to ping stream, transportId={}, header={}, status={}, metadata={}", pingSession.getTransportId(), header, status, metadata);
                }
                // responseObserver.onCompleted();
                disconnect(pingSession);
            }

            @Override
            public void onCompleted() {
                if (logger.isDebugEnabled()) {
                    thLogger.debug("PingSession:{} onCompleted()", pingSession);
                }
                responseObserver.onCompleted();
                disconnect(pingSession);
            }
        };
    }

    private void disconnect(PingSession pingSession) {
        pingEventHandler.close(pingSession);
    }

    private ServiceUid resolveServiceUid(Header header, UidFetcher uidFetcher) {
        final String serviceName = header.getServiceName();
        final ServiceUid serviceUid;
        try {
            serviceUid = ServiceUidSuppliers.newSupplier(serviceName, uidFetcher).get();
        } catch (RuntimeException e) {
            // Reject the stream so the agent backs off and reconnects, retrying the lookup (e.g. transient timeout).
            logger.warn("Failed to resolve serviceUid. serviceName={} header={}", serviceName, header, e);
            throw Status.UNAVAILABLE
                    .withDescription("Failed to find service. serviceName=" + serviceName)
                    .asRuntimeException();
        }
        if (serviceUid == null) {
            tLogger.info("Service not found. header={}", header);
            if (logger.isDebugEnabled()) {
                logger.debug("serviceUid not found. header={}", header);
            }
            throw Status.UNAVAILABLE
                    .withDescription("Service not found. serviceName=" + serviceName)
                    .asRuntimeException();
        }
        return serviceUid;
    }

}