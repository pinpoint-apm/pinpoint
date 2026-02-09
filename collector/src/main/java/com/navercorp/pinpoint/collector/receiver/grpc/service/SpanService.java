/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.common.server.io.MessageTypes;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.io.request.UidFetcher;
import com.navercorp.pinpoint.io.request.UidFetcherStreamService;
import io.grpc.Context;
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

    private final SimpleHandler<PSpan> spanHandler;
    private final SimpleHandler<PSpanChunk> spanCheckHandler;

    private final ServerRequestFactory serverRequestFactory;
    private final StreamCloseOnError streamCloseOnError;
    private final UidFetcherStreamService uidFetcherStreamService;

    public SpanService(SimpleHandler<PSpan> spanHandler,
                       SimpleHandler<PSpanChunk> spanCheckHandler,
                       UidFetcherStreamService uidFetcherStreamService,
                       ServerRequestFactory serverRequestFactory,
                       StreamCloseOnError streamCloseOnError) {
        this.spanHandler = Objects.requireNonNull(spanHandler, "spanHandler");
        this.spanCheckHandler = Objects.requireNonNull(spanCheckHandler, "spanCheckHandler");

        this.uidFetcherStreamService = Objects.requireNonNull(uidFetcherStreamService, "uidFetcherStreamService");
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
        this.streamCloseOnError = Objects.requireNonNull(streamCloseOnError, "streamCloseOnError");
    }

    @Override
    public StreamObserver<PSpanMessage> sendSpan(final StreamObserver<Empty> responseStream) {
        final ServerCallStreamObserver<Empty> responseObserver = (ServerCallStreamObserver<Empty>) responseStream;
        long streamId = serverStreamId.incrementAndGet();

        UidFetcher fetcher = uidFetcherStreamService.newUidFetcher();
        return new ServerCallStream<>(logger, streamId, fetcher, responseObserver, this::messageDispatch, streamCloseOnError, Empty::getDefaultInstance);
    }


    private void messageDispatch(ServerCallStream<PSpanMessage, Empty> call, PSpanMessage spanMessage, ServerCallStream<PSpanMessage, Empty> responseObserver) {
        if (isDebug) {
            logger.debug("Send PSpan={}", MessageFormatUtils.debugLog(spanMessage));
        }

        if (spanMessage.hasSpan()) {
            PSpan span = spanMessage.getSpan();

            Context current = Context.current();
            UidFetcher fetcher = call.getUidFetcher();
            ServerRequest<PSpan> request = serverRequestFactory.newServerRequest(current, fetcher, MessageTypes.SPAN, span);
            this.dispatch(this.spanHandler, request, responseObserver);
        } else if (spanMessage.hasSpanChunk()) {
            PSpanChunk spanChunk = spanMessage.getSpanChunk();

            Context current = Context.current();
            UidFetcher fetcher = call.getUidFetcher();
            ServerRequest<PSpanChunk> request = serverRequestFactory.newServerRequest(current, fetcher, MessageTypes.SPANCHUNK, spanChunk);
            this.dispatch(this.spanCheckHandler, request, responseObserver);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Found empty span message, header:{}", ServerContext.getAgentInfo());
            }
        }
    }

    private <T> void dispatch(SimpleHandler<T> handler,
                          ServerRequest<T> request,
                          ServerCallStream<PSpanMessage, Empty> responseObserver) {
        try {
            handler.handleSimple(request);
        } catch (Throwable e) {
            logger.warn("Failed to request. header={}", request.getHeader(), e);
            responseObserver.onNextError(e);
        }
    }



}