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
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.util.MessageType;
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

        if (spanMessage.hasSpan()) {
            PSpan span = spanMessage.getSpan();
            ServerRequest<PSpan> request = serverRequestFactory.newServerRequest(MessageType.SPAN, span);
            this.dispatch(request, responseObserver);
        } else if (spanMessage.hasSpanChunk()) {
            PSpanChunk spanChunk = spanMessage.getSpanChunk();
            ServerRequest<PSpanChunk> request = serverRequestFactory.newServerRequest(MessageType.SPANCHUNK, spanChunk);
            this.dispatch(request, responseObserver);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Found empty span message, header:{}", ServerContext.getAgentInfo());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatch(ServerRequest<? extends GeneratedMessageV3> request, ServerCallStream<PSpanMessage, Empty> responseObserver) {
        try {
            dispatchHandler.dispatchSendMessage((ServerRequest<GeneratedMessageV3>) request);
        } catch (Throwable e) {
            logger.warn("Failed to request. header={}", request.getHeader(), e);
            responseObserver.onNextError(e);
        }
    }

}