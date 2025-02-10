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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.util.MessageType;
import io.grpc.Context;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jaehong.kim
 */
public class SpanService extends SpanGrpc.SpanImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final AtomicLong serverStreamId = new AtomicLong();


    private final DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler;
    private final ServerRequestFactory serverRequestFactory;
    private final StreamCloseOnError streamCloseOnError;

    public SpanService(DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                       ServerRequestFactory serverRequestFactory,
                       StreamCloseOnError streamCloseOnError) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
        this.streamCloseOnError = Objects.requireNonNull(streamCloseOnError, "streamCloseOnError");
    }

    @Override
    public StreamObserver<PSpanMessage> sendSpan(final StreamObserver<Empty> responseStream) {
        final ServerCallStreamObserver<Empty> responseObserver = (ServerCallStreamObserver<Empty>) responseStream;
        return new ServerCallStream<>(logger, serverStreamId.incrementAndGet(), responseObserver, this::messageDispatch, streamCloseOnError, Empty::getDefaultInstance);
    }

    private void messageDispatch(PSpanMessage spanMessage, ServerCallStream<PSpanMessage, Empty> responseObserver) {
        if (isDebug) {
            logger.debug("Send PSpan={}", MessageFormatUtils.debugLog(spanMessage));
        }

        final Context context = Context.current();
        final Header header = ServerContext.getAgentInfo(context);
        try {
            if (spanMessage.hasSpan()) {
                final ServerRequest<GeneratedMessageV3> request = wrapRequest(context, header, MessageType.SPAN, spanMessage.getSpan());
                dispatch(request, responseObserver);
            } else if (spanMessage.hasSpanChunk()) {
                final ServerRequest<GeneratedMessageV3> request = wrapRequest(context, header, MessageType.SPANCHUNK, spanMessage.getSpanChunk());
                dispatch(request, responseObserver);
            } else {
                logger.info("Found empty span message {}", MessageFormatUtils.debugLog(spanMessage));
            }
        } catch (Throwable e) {
            logger.warn("Failed to request. header={}", header, e);
            responseObserver.onNextError(e);
        }
    }

    private <T> ServerRequest<GeneratedMessageV3> wrapRequest(Context context, Header header, MessageType messageType, T data) throws StatusException {
        final Message<T> message = new DefaultMessage<>(header, messageType, data);
        return (ServerRequest<GeneratedMessageV3>) serverRequestFactory.newServerRequest(context, message);
    }

    private void dispatch(final ServerRequest<GeneratedMessageV3> request, ServerCallStream<PSpanMessage, Empty> responseObserver) {
        dispatchHandler.dispatchSendMessage(request);
    }
}