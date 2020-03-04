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

import io.grpc.Attributes;
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
public class TransportMetadataServerInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TransportMetadataServerInterceptor() {
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> serverCall, Metadata headers, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        final Attributes attributes = serverCall.getAttributes();

        final TransportMetadata transportMetadata = attributes.get(MetadataServerTransportFilter.TRANSPORT_METADATA_KEY);
        if (transportMetadata == null) {
            if (logger.isInfoEnabled()) {
                logger.info("Close call. cause=transportMetadata is null, headers={}, attributes={}", headers, serverCall.getAttributes());
            }
            serverCall.close(Status.INTERNAL.withDescription("transportMetadata is null"), new Metadata());
            return new ServerCall.Listener<ReqT>() {
            };
        }

        final Context currentContext = Context.current();
        final Context newContext = currentContext.withValue(ServerContext.getTransportMetadataKey(), transportMetadata);
        if (logger.isDebugEnabled()) {
            logger.debug("bind metadata method={}, headers={}, attr={}", serverCall.getMethodDescriptor().getFullMethodName(), headers, serverCall.getAttributes());
        }
        ServerCall.Listener<ReqT> listener = Contexts.interceptCall(newContext, serverCall, headers, serverCallHandler);
        return listener;
    }
}