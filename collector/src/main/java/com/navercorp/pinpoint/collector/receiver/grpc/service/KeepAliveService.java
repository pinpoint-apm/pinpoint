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

import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.grpc.trace.KeepAliveGrpc;
import com.navercorp.pinpoint.grpc.trace.PPing;
import io.grpc.Status;
import io.grpc.internal.KeepAliveManager;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KeepAliveService extends KeepAliveGrpc.KeepAliveImplBase {
    private static final long DEFAULT_KEEPALIVE_TIME_NANOS = TimeUnit.SECONDS.toNanos(10L);
    private static final long DEFAULT_KEEPALIVE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(30L);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentEventAsyncTaskService agentEventAsyncTask;
    private final AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask;

    private KeepAliveManager keepAliveManager;
    private EventExecutor eventExecutor = new DefaultEventExecutor();

    // TODO keepalive time, timeout
    public KeepAliveService(AgentEventAsyncTaskService grpcAgentEventAsyncTask, AgentLifeCycleAsyncTaskService grpcAgentLifeCycleAsyncTask) {
        this.agentEventAsyncTask = grpcAgentEventAsyncTask;
        this.agentLifeCycleAsyncTask = grpcAgentLifeCycleAsyncTask;
    }

    @Override
    public StreamObserver<PPing> serverKeepAlive(StreamObserver<PPing> responseObserver) {
        KeepAlivePinger keepAlivePinger = new KeepAlivePinger(responseObserver);
        keepAliveManager = new KeepAliveManager(keepAlivePinger, eventExecutor, DEFAULT_KEEPALIVE_TIME_NANOS, DEFAULT_KEEPALIVE_TIMEOUT_NANOS, true);

        ServerCallStreamObserver<PPing> serverCallStreamObserver = (ServerCallStreamObserver<PPing>) responseObserver;
        serverCallStreamObserver.setOnReadyHandler(new Runnable() {
            @Override
            public void run() {
                if (serverCallStreamObserver.isReady()) {
                    keepAliveManager.onTransportStarted();
                }
            }
        });

        return new StreamObserver<PPing>() {
            @Override
            public void onNext(PPing pPing) {
                keepAliveManager.onDataReceived();
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                logger.warn("Failed to server keep-alive status={}", status, throwable);
                keepAliveManager.onTransportTermination();
            }

            @Override
            public void onCompleted() {
                logger.debug("Completed server keep-alive");
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<PPing> clientKeepAlive(StreamObserver<PPing> responseObserver) {
        return new StreamObserver<PPing>() {
            @Override
            public void onNext(PPing pPing) {
                logger.debug("Received client keep-alive " + pPing);
                // TODO
//                updateState(pPing);
                responseObserver.onNext(PPing.newBuilder().build());
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                logger.warn("Failed to client keep-alive status={}", status);
                //throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                logger.debug("Completed client keep-alive");
                responseObserver.onCompleted();
            }
        };
    }

    private void updateState(final PPing ping) {
        final int eventCounter = ping.getId();
        long pingTimestamp = System.currentTimeMillis();
        Map<Object, Object> channelProperties = new HashMap<>();
        try {
            if (!(eventCounter < 0)) {
                agentLifeCycleAsyncTask.handleLifeCycleEvent(channelProperties, pingTimestamp, AgentLifeCycleState.RUNNING, eventCounter);
            }
            agentEventAsyncTask.handleEvent(channelProperties, pingTimestamp, AgentEventType.AGENT_PING);
        } catch (Exception e) {
            logger.warn("Error handling client ping event", e);
        }
    }

    private static class KeepAlivePinger implements KeepAliveManager.KeepAlivePinger {
        private final StreamObserver<PPing> responseObserver;

        public KeepAlivePinger(StreamObserver<PPing> responseObserver) {
            this.responseObserver = responseObserver;
        }

        @Override
        public void ping() {
            responseObserver.onNext(PPing.newBuilder().build());
        }

        @Override
        public void onPingTimeout() {
            Status status = Status.UNAVAILABLE.withDescription("Keepalive failed, The connection is likely gone");
            responseObserver.onError(status.asRuntimeException());
        }
    }
}