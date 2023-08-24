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

package com.navercorp.pinpoint.log.collector.grpc.context;

import com.navercorp.pinpoint.grpc.HeaderReader;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LogHeaderPropagationInterceptor implements ServerInterceptor {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HeaderReader<LogAgentHeader> headerReader;
    private final Context.Key<LogAgentHeader> contextKey;

    public LogHeaderPropagationInterceptor(HeaderReader<LogAgentHeader> headerReader) {
        this(headerReader, LogAgentHeader.LOG_AGENT_HEADER_KEY);
    }

    public LogHeaderPropagationInterceptor(HeaderReader<LogAgentHeader> headerReader, Context.Key<LogAgentHeader> contextKey) {
        this.headerReader = Objects.requireNonNull(headerReader, "headerReader");
        this.contextKey = Objects.requireNonNull(contextKey, "contextKey");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        LogAgentHeader headerObject;
        try {
            headerObject = headerReader.extract(headers);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Header extract fail cause={}, method={} headers={}, attr={}",
                        e.getMessage(), call.getMethodDescriptor().getFullMethodName(), headers, call.getAttributes(), e);
            }
            call.close(Status.INVALID_ARGUMENT.withDescription(e.getMessage()), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }

        final Context currentContext = Context.current();
        final Context newContext = currentContext.withValue(contextKey, headerObject);
        if (logger.isDebugEnabled()) {
            logger.debug("headerPropagation method={}, headers={}, attr={}", call.getMethodDescriptor().getFullMethodName(), headers, call.getAttributes());
        }

        return Contexts.interceptCall(newContext, call, headers, next);
    }

}