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

package com.navercorp.pinpoint.grpc.client;

import com.navercorp.pinpoint.common.util.Assert;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class UnaryCallDeadlineInterceptor implements ClientInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long timeoutMillis;

    public UnaryCallDeadlineInterceptor(long timeoutMillis) {
        Assert.isTrue(timeoutMillis > 0, "must be `timeoutMillis > 0`");
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        if (MethodDescriptor.MethodType.UNARY == method.getType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("interceptCall {}", method.getFullMethodName());
            }
            return next.newCall(method, callOptions.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS));
        } else {
            return next.newCall(method, callOptions);
        }
    }

}
