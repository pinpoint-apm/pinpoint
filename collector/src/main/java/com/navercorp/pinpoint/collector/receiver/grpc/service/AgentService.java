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

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.StatusError;
import com.navercorp.pinpoint.grpc.StatusErrors;
import com.navercorp.pinpoint.grpc.server.lifecycle.PingEventHandler;
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
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final SimpleRequestHandlerAdaptor<PResult> simpleRequestHandlerAdaptor;
    private final PingEventHandler pingEventHandler;
    private final Executor executor;

    public AgentService(DispatchHandler dispatchHandler, PingEventHandler pingEventHandler, Executor executor, ServerRequestFactory serverRequestFactory) {
        this.simpleRequestHandlerAdaptor = new SimpleRequestHandlerAdaptor<PResult>(this.getClass().getName(), dispatchHandler, serverRequestFactory);
        this.pingEventHandler = Objects.requireNonNull(pingEventHandler, "pingEventHandler");
        Objects.requireNonNull(executor, "executor");
        this.executor = Context.currentContextExecutor(executor);
    }

    @Override
    public void requestAgentInfo(PAgentInfo agentInfo, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
        }

        Message<PAgentInfo> message = newMessage(agentInfo, DefaultTBaseLocator.AGENT_INFO);
        doExecutor(message, responseObserver);
    }

    void doExecutor(final Message message, final StreamObserver<PResult> responseObserver) {
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    simpleRequestHandlerAdaptor.request(message, responseObserver);
                }
            });
        } catch (RejectedExecutionException ree) {
            // Defense code
            logger.warn("Failed to request. Rejected execution, executor={}", executor);
        }
    }

    @Override
    public StreamObserver<PPing> pingSession(final StreamObserver<PPing> responseObserver) {
        final StreamObserver<PPing> request = new StreamObserver<PPing>() {
            private final AtomicBoolean first = new AtomicBoolean(false);
            private final long id = nextSessionId();
            @Override
            public void onNext(PPing ping) {
                if (first.compareAndSet(false, true)) {
                    if (isDebug) {
                        logger.debug("PingSession:{} start:{}", id, MessageFormatUtils.debugLog(ping));
                    }
                    AgentService.this.pingEventHandler.connect();
                }
                if (isDebug) {
                    logger.debug("PingSession:{} onNext:{}", id, MessageFormatUtils.debugLog(ping));
                }
                PPing replay = newPing();
                responseObserver.onNext(replay);
                AgentService.this.pingEventHandler.ping();
            }

            private PPing newPing() {
                PPing.Builder builder = PPing.newBuilder();
                return builder.build();
            }

            @Override
            public void onError(Throwable t) {
                final StatusError statusError = StatusErrors.throwable(t);
                if (statusError.isSimpleError()) {
                    logger.info("Failed to ping stream, id={}, cause={}", id, statusError.getMessage());
                } else {
                    logger.warn("Failed to ping stream, id={}, cause={}", id, statusError.getMessage(), statusError.getThrowable());
                }
                disconnect();
            }

            @Override
            public void onCompleted() {
                if (isDebug) {
                    logger.debug("PingSession:{} onCompleted()", id);
                }
                responseObserver.onCompleted();
                disconnect();
            }

            private void disconnect() {
                AgentService.this.pingEventHandler.close();
            }

        };
        return request;
    }

    private long nextSessionId() {
        return idAllocator.getAndIncrement();
    }

    private <T> Message<T> newMessage(T requestData, short type) {
        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type);
        final HeaderEntity headerEntity = new HeaderEntity(Collections.emptyMap());
        return new DefaultMessage<T>(header, headerEntity, requestData);
    }
}