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

package com.navercorp.pinpoint.grpc.client.interceptor;

import com.navercorp.pinpoint.common.util.Assert;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class DiscardClientInterceptor implements ClientInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DiscardEventListener listener;
    private final long maxPendingThreshold;
    private final long discardCountForReconnect;
    private final long notReadyTimeoutMillis;

    public DiscardClientInterceptor(DiscardEventListener listener, long maxPendingThreshold, long discardCountForReconnect, long notReadyTimeoutMillis) {
        this.listener = Assert.requireNonNull(listener, "listener");
        this.maxPendingThreshold = maxPendingThreshold;
        this.discardCountForReconnect = discardCountForReconnect;
        this.notReadyTimeoutMillis = notReadyTimeoutMillis;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        if (MethodDescriptor.MethodType.CLIENT_STREAMING == method.getType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("interceptCall {}", method.getFullMethodName());
            }
            final ClientCall<ReqT, RespT> newCall = next.newCall(method, callOptions);
            return new DiscardClientCall<ReqT, RespT>(newCall, this.listener, maxPendingThreshold, discardCountForReconnect, notReadyTimeoutMillis);
        } else {
            return next.newCall(method, callOptions);
        }
    }

}
