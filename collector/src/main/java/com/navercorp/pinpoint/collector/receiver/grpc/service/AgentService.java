/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.grpc.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.collector.grpc.lifecycle.PingSession;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PPing;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.util.MessageType;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
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

    private final SimpleRequestHandlerAdaptor<GeneratedMessageV3, GeneratedMessageV3> simpleRequestHandlerAdaptor;
    private final ServerRequestFactory serverRequestFactory;

    private final PingEventHandler pingEventHandler;
    private final Executor executor;

    public AgentService(DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                        PingEventHandler pingEventHandler, Executor executor, ServerRequestFactory serverRequestFactory) {
        this.simpleRequestHandlerAdaptor = new SimpleRequestHandlerAdaptor<>(this.getClass().getName(), dispatchHandler);
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");

        this.pingEventHandler = Objects.requireNonNull(pingEventHandler, "pingEventHandler");
        Objects.requireNonNull(executor, "executor");
        this.executor = Context.currentContextExecutor(executor);
    }

    @Override
    public void requestAgentInfo(PAgentInfo agentInfo, StreamObserver<PResult> responseObserver) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
        }

        final ServerRequest<PAgentInfo> request = this.serverRequestFactory.newServerRequest(MessageType.AGENT_INFO, agentInfo);
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    simpleRequestHandlerAdaptor.request(request, responseObserver);
                    // Update service type of PingSession
                    TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
                    pingEventHandler.update(transportMetadata.getTransportId());
                }
            });
        } catch (RejectedExecutionException ree) {
            // Defense code
            logger.warn("Failed to request. Rejected execution, executor={} header:{}", executor, request.getHeader());
        }
    }

    @Override
    public StreamObserver<PPing> pingSession(final StreamObserver<PPing> response) {
        Context context = Context.current();
        Header header = ServerContext.getAgentInfo(context);
        TransportMetadata transport = ServerContext.getTransportMetadata(context);
        PingSession pingSession = this.pingEventHandler.newPingSession(transport.getTransportId(), header);

        final ServerCallStreamObserver<PPing> responseObserver = (ServerCallStreamObserver<PPing>) response;
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
                    thLogger.info("Failed to ping stream, id={}, {} metadata:{}", pingSession.getTransportId(), status, metadata);
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

            private void disconnect(PingSession pingSession) {
                AgentService.this.pingEventHandler.close(pingSession);
            }

        };
    }

}