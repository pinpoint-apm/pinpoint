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

package com.navercorp.pinpoint.collector.receiver.grpc.flow;

import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.flowcontrol.DefaultServerCallWrapper;
import com.navercorp.pinpoint.grpc.server.flowcontrol.ServerCallWrapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.grpc.Context;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author jaehong.kim
 */
public class RateLimitClientStreamServerInterceptor implements ServerInterceptor {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger rejectLogger;
    private final ThrottledLogger bandwidthLogger;
    private final String name;
    private final Executor executor;

    private final Bandwidth bandwidth;


    public RateLimitClientStreamServerInterceptor(String name, final Executor executor, Bandwidth bandwidth, final long throttledLoggerRatio) {
        this.name = Objects.requireNonNull(name, "name");

        Objects.requireNonNull(executor, "executor");
        // Context wrapper
        this.executor = Context.currentContextExecutor(executor);

        this.bandwidth = Objects.requireNonNull(bandwidth, "bandwidth");

        this.rejectLogger = ThrottledLogger.getLogger(logger, throttledLoggerRatio);
        this.bandwidthLogger = ThrottledLogger.getLogger(logger, throttledLoggerRatio);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final ServerCallWrapper serverCall = newServerCallWrapper(call, headers);
        if (logger.isInfoEnabled()) {
            logger.info("Initialize {} interceptor. {}, headers={}, remoteAddr={} Bandwidth(capacity={}, refillTokens={})",
                    this.name, call.getMethodDescriptor().getFullMethodName(), headers, serverCall.getRemoteAddr(), bandwidth.getCapacity(), bandwidth.getRefillTokens());
        }
        final ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            private final Bucket bucket = Bucket.builder().addLimit(bandwidth).build();

            @Override
            public void onMessage(final ReqT message) {
                if (bucket.tryConsume(1)) {
                    try {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                delegate().onMessage(message);
                            }
                        });
                    } catch (Throwable th) {
                        if (rejectLogger.isInfoEnabled()) {
                            rejectLogger.info("Failed to request. ThreadPool is exhausted. {} {}/{} {} count={}",
                                    name, serverCall.getApplicationName(), serverCall.getAgentId(), serverCall.getRemoteAddr(), rejectLogger.getCounter());
                        }
                    }
                } else {
                    if (bandwidthLogger.isInfoEnabled()) {
                        bandwidthLogger.info("Too many requests. Bandwidth exceeded. {} {}/{} {}",
                                name, serverCall.getApplicationName(), serverCall.getAgentId(), serverCall.getRemoteAddr());
                    }
                }
            }
        };
    }

    private <ReqT, RespT> ServerCallWrapper newServerCallWrapper(ServerCall<ReqT, RespT> call, Metadata headers) {
        final String agentId = headers.get(Header.AGENT_ID_KEY);
        final String applicationName = headers.get(Header.APPLICATION_NAME_KEY);
        return new DefaultServerCallWrapper<>(call, applicationName, agentId);
    }
}