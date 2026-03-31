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
import io.github.bucket4j.local.LocalBucketBuilder;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jaehong.kim
 */
public class RateLimitClientStreamServerInterceptor implements ServerInterceptor {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger bandwidthLogger;
    private final String name;

    private final Bandwidth bandwidth;
    private final LocalBucketBuilder bucketBuilder;


    public RateLimitClientStreamServerInterceptor(String name, Bandwidth bandwidth, final long throttledLoggerRatio) {
        this.name = Objects.requireNonNull(name, "name");

        this.bandwidth = Objects.requireNonNull(bandwidth, "bandwidth");
        this.bucketBuilder = Bucket.builder().addLimit(bandwidth);

        this.bandwidthLogger = ThrottledLogger.getLogger(logger, throttledLoggerRatio);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final MethodDescriptor<ReqT, RespT> methodDescriptor = call.getMethodDescriptor();

        final MethodDescriptor.MethodType methodType = methodDescriptor.getType();
        if (!isClientStream(methodType)) {
            return next.startCall(call, headers);
        }

        final ServerCallWrapper serverCall = newServerCallWrapper(call, headers);
        if (logger.isInfoEnabled()) {
            logger.info("Initialize {} interceptor. {}, headers={}, remoteAddr={} Bandwidth(capacity={}, refillTokens={})",
                    this.name, methodDescriptor.getFullMethodName(), headers, serverCall.getRemoteAddr(), bandwidth.getCapacity(), bandwidth.getRefillTokens());
        }
        final ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            private final Bucket bucket = bucketBuilder.build();
            private final AtomicLong dropCount = new AtomicLong();

            @Override
            public void onMessage(final ReqT message) {
                if (bucket.tryConsume(1)) {
                    delegate().onMessage(message);
                } else {
                    long count = dropCount.incrementAndGet();
                    if (bandwidthLogger.isInfoEnabled()) {
                        bandwidthLogger.info("Too many requests. Bandwidth exceeded. {} {}/{} {} drop={}",
                                name, serverCall.getApplicationName(), serverCall.getAgentId(), serverCall.getRemoteAddr(), count);
                    }
                }
            }
        };
    }

    private boolean isClientStream(MethodDescriptor.MethodType methodType) {
        return methodType == MethodDescriptor.MethodType.CLIENT_STREAMING;
    }

    private <ReqT, RespT> ServerCallWrapper newServerCallWrapper(ServerCall<ReqT, RespT> call, Metadata headers) {
        final String agentId = headers.get(Header.AGENT_ID_KEY);
        final String applicationName = headers.get(Header.APPLICATION_NAME_KEY);
        return new DefaultServerCallWrapper<>(call, applicationName, agentId);
    }
}