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

package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HeaderPropagationInterceptor<H> implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HeaderFactory<H> headerFactory;
    private final Context.Key<H> contextKey;

    public HeaderPropagationInterceptor(HeaderFactory<H> headerFactory, Context.Key<H> contextKey) {
        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory must not be null");
        this.contextKey = Assert.requireNonNull(contextKey, "contextKey must not be null");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final H headerObject = headerFactory.extract(headers);

        final Context currentContext = Context.current();
        final Context newContext = currentContext.withValue(contextKey, headerObject);
        if (logger.isDebugEnabled()) {
            logger.debug("interceptCall(call = [{}], headers = [{}], next = [{}])", call, headers, next);
        }
        ServerCall.Listener<ReqT>  contextPropagateInterceptor = Contexts.interceptCall(newContext, call, headers, next);
        return contextPropagateInterceptor;
    }
}
