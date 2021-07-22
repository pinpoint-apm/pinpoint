/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc.server.flowcontrol;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.Header;
import io.grpc.Context;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author jaehong.kim
 */
public class StreamExecutorServerInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String name;
    private final Executor executor;
    private final int initNumMessages;
    private final StreamExecutorRejectedExecutionRequestScheduler scheduler;

    public StreamExecutorServerInterceptor(String name, final Executor executor, final int initNumMessages, final ScheduledExecutor scheduledExecutor,
                                           RejectedExecutionListenerFactory listenerFactory) {
        this.name = Objects.requireNonNull(name, "name");

        Objects.requireNonNull(executor, "executor");
        // Context wrapper
        this.executor = Context.currentContextExecutor(executor);
        Assert.isTrue(initNumMessages > 0, "initNumMessages must be positive");
        this.initNumMessages = initNumMessages;

        Objects.requireNonNull(scheduledExecutor, "scheduledExecutor");
        Objects.requireNonNull(listenerFactory, "listenerFactory");

        this.scheduler = new StreamExecutorRejectedExecutionRequestScheduler(scheduledExecutor, listenerFactory);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final ServerCallWrapper serverCall = newServerCallWrapper(call, headers);

        final StreamExecutorRejectedExecutionRequestScheduler.Listener scheduleListener = this.scheduler.schedule(serverCall);
        if (logger.isInfoEnabled()) {
            logger.info("Initialize schedule listener. {} {}, headers={}, initNumMessages={}, scheduler={}, listener={}",
                    this.name, call.getMethodDescriptor().getFullMethodName(), headers, initNumMessages, scheduler, scheduleListener);
        }

        final ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
        // Init MessageDeframer.pendingDeliveries
        call.request(initNumMessages);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onMessage(final ReqT message) {
                try {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            scheduleListener.onMessage();
                            delegate().onMessage(message);
                        }
                    });
//                    scheduleListener.onMessage();
                } catch (RejectedExecutionException ree) {
                    // Defense code, need log ?
                    scheduleListener.onRejectedExecution();
                    logger.warn("Failed to request. Rejected execution, count={}", scheduleListener.getRejectedExecutionCount());
                }
            }

            @Override
            public void onCancel() {
                scheduleListener.onCancel();
                delegate().onCancel();
            }

            @Override
            public void onComplete() {
                scheduleListener.onCancel();
                delegate().onComplete();
            }
        };
    }

    private <ReqT, RespT> ServerCallWrapper newServerCallWrapper(ServerCall<ReqT, RespT> call, Metadata headers) {
        final String agentId = headers.get(Header.AGENT_ID_KEY);
        final String applicationName = headers.get(Header.APPLICATION_NAME_KEY);
        return new DefaultServerCallWrapper<>(call, applicationName, agentId);
    }
}