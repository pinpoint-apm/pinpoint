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
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HeaderPropagationInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HeaderReader<Header> headerReader;
    private final Context.Key<Header> contextKey;

    public HeaderPropagationInterceptor(HeaderReader<Header> headerReader) {
        this(headerReader, ServerContext.getAgentInfoKey());
    }

    public HeaderPropagationInterceptor(HeaderReader<Header> headerReader, Context.Key<Header> contextKey) {
        this.headerReader = Assert.requireNonNull(headerReader, "headerReader");
        this.contextKey = Assert.requireNonNull(contextKey, "contextKey");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Header headerObject;
        try {
            headerObject = headerReader.extract(headers);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Header extract fail cause={}, method={} headers={}, attr={}",
                        e.getMessage(), call.getMethodDescriptor().getFullMethodName(), headers, call.getAttributes(), e);
            }
            call.close(Status.INVALID_ARGUMENT.withDescription(e.getMessage()), new Metadata());
            return new ServerCall.Listener<ReqT>() {
            };
        }

        final Context currentContext = Context.current();
        final Context newContext = currentContext.withValue(contextKey, headerObject);
        if (logger.isDebugEnabled()) {
            logger.debug("headerPropagation method={}, headers={}, attr={}", call.getMethodDescriptor().getFullMethodName(), headers, call.getAttributes());
        }

        ServerCall.Listener<ReqT> contextPropagateInterceptor = Contexts.interceptCall(newContext, call, headers, next);
        return contextPropagateInterceptor;
    }

}