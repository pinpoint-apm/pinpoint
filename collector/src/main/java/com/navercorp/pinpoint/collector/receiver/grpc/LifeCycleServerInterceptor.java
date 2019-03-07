/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class LifeCycleServerInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<ServerStateChangeEventHandler> channelStateChangeEventHandlers;

    public LifeCycleServerInterceptor(List<ServerStateChangeEventHandler> channelStateChangeEventHandlers) {
        this.channelStateChangeEventHandlers = channelStateChangeEventHandlers;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        final Context context = Context.current();
        final GrpcRequestHeader requestHeaderContextValue = GrpcRequestHeaderContextValue.get();
        final ServerCall.Listener<ReqT> listener = Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);

//        final ServerCall.Listener<ReqT> listener = serverCallHandler.startCall(serverCall, metadata);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onMessage(ReqT message) {
                final GrpcRequestHeader requestHeaderContextValue = GrpcRequestHeaderContextValue.get();
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                final GrpcRequestHeader requestHeaderContextValue = GrpcRequestHeaderContextValue.get();
                super.onHalfClose();
                eventHandle(SocketStateCode.CLOSED_BY_CLIENT);
            }

            @Override
            public void onCancel() {
                final GrpcRequestHeader requestHeaderContextValue = GrpcRequestHeaderContextValue.get();
                super.onCancel();
                eventHandle(SocketStateCode.CLOSED_BY_CLIENT);
            }

            @Override
            public void onComplete() {
                final GrpcRequestHeader requestHeaderContextValue = GrpcRequestHeaderContextValue.get();
                super.onComplete();
                eventHandle(SocketStateCode.CLOSED_BY_SERVER);
            }

            @Override
            public void onReady() {
                final GrpcRequestHeader requestHeaderContextValue = GrpcRequestHeaderContextValue.get();
                super.onReady();
                eventHandle(SocketStateCode.CONNECTED);
            }
        };
    }

    private void eventHandle(final SocketStateCode socketStateCode) {
        logger.debug("Event handle {}", socketStateCode);
//        for (ServerStateChangeEventHandler handler : this.channelStateChangeEventHandlers) {
//            try {
//                handler.eventPerformed(null, socketStateCode);
//            } catch (Exception e) {
//            }
//        }
    }
}