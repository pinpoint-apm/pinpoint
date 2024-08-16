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
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.AgentKey;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.server.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.grpc.server.lifecycle.PingEventHandlerFactory;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PPing;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jaehong.kim
 */
public class AgentService extends AgentGrpc.AgentImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final AtomicLong idAllocator = new AtomicLong();

    private final SimpleRequestHandlerAdaptor<GeneratedMessageV3, GeneratedMessageV3> simpleRequestHandlerAdaptor;
    private final PingEventHandlerFactory pingEventHandlerProvider;
    private final Executor executor;

    public AgentService(DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                        PingEventHandlerFactory pingEventHandlerProvider, Executor executor, ServerRequestFactory serverRequestFactory) {
        this.simpleRequestHandlerAdaptor = new SimpleRequestHandlerAdaptor<>(this.getClass().getName(), dispatchHandler, serverRequestFactory);
        this.pingEventHandlerProvider = Objects.requireNonNull(pingEventHandlerProvider, "pingEventHandlerProvider");
        Objects.requireNonNull(executor, "executor");
        this.executor = Context.currentContextExecutor(executor);
    }

    @Override
    public void requestAgentInfo(PAgentInfo agentInfo, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
        }


        final com.navercorp.pinpoint.grpc.Header header = ServerContext.getAgentInfo();
        final boolean legacyAgent = !header.isServiceTypeSupported();
        if (legacyAgent) {
            final TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
            AgentKey key = new AgentKey(header.getAgentId(), header.getAgentStartTime());
            transportMetadata.registerServiceType(key, (short)agentInfo.getServiceType());
        }

        PingEventHandler pingEventHandler = pingEventHandlerProvider.createPingEventHandler();
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final Message<PAgentInfo> message = newMessage(agentInfo, DefaultTBaseLocator.AGENT_INFO);
                    simpleRequestHandlerAdaptor.request(message, responseObserver);
                    if (legacyAgent) {
                        // Update service type of PingSession
                        pingEventHandler.update();
                    }
                }
            });
        } catch (RejectedExecutionException ree) {
            // Defense code
            logger.warn("Failed to request. Rejected execution, executor={}", executor);
        }
    }


    @Override
    public StreamObserver<PPing> pingSession(final StreamObserver<PPing> response) {
        final ServerCallStreamObserver<PPing> responseObserver = (ServerCallStreamObserver<PPing>) response;

        final PingEventHandler pingEventHandler = pingEventHandlerProvider.createPingEventHandler();

        return new StreamObserver<>() {
            private final ThrottledLogger thLogger = ThrottledLogger.getLogger(AgentService.this.logger, 100);

            private final long id = nextSessionId();

            @Override
            public void onNext(PPing ping) {
                if (isDebug) {
                    thLogger.debug("PingSession:{} onNext:Ping", id);
                }
                pingEventHandler.ping();

                if (responseObserver.isReady()) {
                    PPing replay = newPing();
                    responseObserver.onNext(replay);
                } else {
                    thLogger.warn("Ping message is ignored: stream is not ready: {}", ServerContext.getAgentInfo());
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
                    thLogger.info("Failed to ping stream, id={}, {} metadata:{}", id, status, metadata);
                }
                // responseObserver.onCompleted();
                disconnect();
            }

            @Override
            public void onCompleted() {
                if (logger.isDebugEnabled()) {
                    logger.debug("PingSession:{} onCompleted()", id);
                }
                responseObserver.onCompleted();
                disconnect();
            }

            private void disconnect() {
                pingEventHandler.close();
            }

        };
    }

    private long nextSessionId() {
        return idAllocator.getAndIncrement();
    }

    private <T> Message<T> newMessage(T requestData, short type) {
        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type);
        final HeaderEntity headerEntity = new HeaderEntity(Collections.emptyMap());
        return new DefaultMessage<>(header, headerEntity, requestData);
    }
}