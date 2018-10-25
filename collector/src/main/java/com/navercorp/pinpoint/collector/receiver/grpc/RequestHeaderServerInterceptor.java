/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

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

public class RequestHeaderServerInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GrpcRequestHeaderReader requestHeaderMetadataReader = new GrpcRequestHeaderReader();

    public RequestHeaderServerInterceptor() {
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        final Attributes attributes = serverCall.getAttributes();
        try {
            final GrpcRequestHeader requestHeader = this.requestHeaderMetadataReader.read(attributes, metadata);
            if (logger.isDebugEnabled()) {
                logger.debug("Header {}", requestHeader);
            }

            if (!requestHeader.getTransportStatus().isOk()) {
                // TODO Error Handling + log
                return new ServerCall.Listener<ReqT>() {
                    @Override
                    public void onReady() {
                        logger.warn("Invalid transport status {}", requestHeader.getTransportStatus());
                        serverCall.close(Status.INTERNAL.withDescription(requestHeader.getTransportStatus().getCause()), new Metadata());
                    }
                };
            }

            // Setup request header
            final GrpcRequestHeaderContextValue.ContextBuilder contextBuilder = new GrpcRequestHeaderContextValue.ContextBuilder();
            contextBuilder.setRequestHeader(requestHeader);
            final Context context = contextBuilder.build();
            final ServerCall.Listener<ReqT> listener = Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
            return listener;
        } catch (Exception e) {
            return new ServerCall.Listener<ReqT>() {
                @Override
                public void onReady() {
                    logger.warn("Internal server error ", e);
                    serverCall.close(Status.INTERNAL.withCause(e), new Metadata());
                }
            };
        }
    }
}