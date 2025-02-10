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
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PPing;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jaehong.kim
 */
public class AgentService extends AgentGrpc.AgentImplBase {
    private static final AtomicLong idAllocator = new AtomicLong();

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SimpleRequestHandlerAdaptor<GeneratedMessageV3, GeneratedMessageV3> simpleRequestHandlerAdaptor;
    private final PingEventHandler pingEventHandler;
    private final Executor executor;

    public AgentService(DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                        PingEventHandler pingEventHandler, Executor executor, ServerRequestFactory serverRequestFactory) {
        this.simpleRequestHandlerAdaptor = new SimpleRequestHandlerAdaptor<>(this.getClass().getName(), dispatchHandler, serverRequestFactory);
        this.pingEventHandler = Objects.requireNonNull(pingEventHandler, "pingEventHandler");
        Objects.requireNonNull(executor, "executor");
        this.executor = Context.currentContextExecutor(executor);
    }

    @Override
    public void requestAgentInfo(PAgentInfo agentInfo, StreamObserver<PResult> responseObserver) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
        }

        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final Message<PAgentInfo> message = newMessage(agentInfo, MessageType.AGENT_INFO);
                    simpleRequestHandlerAdaptor.request(message, responseObserver);
                    // Update service type of PingSession
                    AgentService.this.pingEventHandler.update((short) agentInfo.getServiceType());
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
        return new StreamObserver<>() {
            private final AtomicBoolean first = new AtomicBoolean(false);
            private final ThrottledLogger thLogger = ThrottledLogger.getLogger(AgentService.this.logger, 100);

            private final long id = nextSessionId();
            @Override
            public void onNext(PPing ping) {
                if (first.compareAndSet(false, true)) {
                    // Only first
                    if (logger.isDebugEnabled()) {
                        thLogger.debug("PingSession:{} start:{}", id, MessageFormatUtils.debugLog(ping));
                    }
                    AgentService.this.pingEventHandler.connect();
                } else {
                    AgentService.this.pingEventHandler.ping();
                }
                if (logger.isDebugEnabled()) {
                    thLogger.debug("PingSession:{} onNext:{}", id, MessageFormatUtils.debugLog(ping));
                }
                if (responseObserver.isReady()) {
                    PPing replay = newPing();
                    responseObserver.onNext(replay);
                } else {
                    thLogger.warn("ping message is ignored: stream is not ready: {}", ServerContext.getAgentInfo());
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
                    thLogger.debug("PingSession:{} onCompleted()", id);
                }
                responseObserver.onCompleted();
                disconnect();
            }

            private void disconnect() {
                AgentService.this.pingEventHandler.close();
            }

        };
    }

    private long nextSessionId() {
        return idAllocator.getAndIncrement();
    }

    private <T> Message<T> newMessage(T requestData, MessageType type) {
        Header header = ServerContext.getAgentInfo();
        return new DefaultMessage<>(header, type, requestData);
    }
}