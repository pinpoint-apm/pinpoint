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
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PStatMessage;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.UidFetcher;
import com.navercorp.pinpoint.io.request.UidFetcherStreamService;
import com.navercorp.pinpoint.io.util.MessageType;
import com.navercorp.pinpoint.io.util.MessageTypes;
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
public class StatService extends StatGrpc.StatImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final AtomicLong serverStreamId = new AtomicLong();

    private final UidFetcherStreamService uidFetcherStreamService;

    private final ServerRequestFactory serverRequestFactory;
    private final StreamCloseOnError streamCloseOnError;

    private final SimpleHandler<PAgentStatBatch> statBatchHandler;
    private final SimpleHandler<PAgentStat> statHandler;
    private final SimpleHandler<PAgentUriStat> uriStatHandler;

    public StatService(SimpleHandler<PAgentStatBatch> statBatchHandler,
                       SimpleHandler<PAgentStat> statHandler,
                       SimpleHandler<PAgentUriStat> uriStatHandler,
                       UidFetcherStreamService uidFetcherStreamService,
                       ServerRequestFactory serverRequestFactory,
                       StreamCloseOnError streamCloseOnError) {
        this.statBatchHandler = Objects.requireNonNull(statBatchHandler, "statBatchHandler");
        this.statHandler = Objects.requireNonNull(statHandler, "statHandler");
        this.uriStatHandler = Objects.requireNonNull(uriStatHandler, "uriStatHandler");
        this.uidFetcherStreamService = Objects.requireNonNull(uidFetcherStreamService, "uidFetcherStreamService");
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
        this.streamCloseOnError = Objects.requireNonNull(streamCloseOnError, "streamCloseOnError");
    }

    @Override
    public StreamObserver<PStatMessage> sendAgentStat(StreamObserver<Empty> responseStream) {
        final ServerCallStreamObserver<Empty> responseObserver = (ServerCallStreamObserver<Empty>) responseStream;
        long streamId = serverStreamId.incrementAndGet();
        UidFetcher fetcher = uidFetcherStreamService.newUidFetcher();
        return new ServerCallStream<>(logger, streamId, fetcher, responseObserver, this::onNext, streamCloseOnError, Empty::getDefaultInstance);
    }


    private void onNext(ServerCallStream<PStatMessage, Empty> call, PStatMessage statMessage, ServerCallStream<PStatMessage, Empty> response) {
        if (isTrace) {
            logger.trace("Send PAgentStat={}", MessageFormatUtils.debugLog(statMessage));
        }
        if (statMessage.hasAgentStat()) {
            PAgentStat agentStat = statMessage.getAgentStat();
            this.dispatch(agentStat, MessageTypes.AGENT_STAT, statHandler, call.getUidFetcher(), response);
        } else if (statMessage.hasAgentStatBatch()) {
            PAgentStatBatch agentStatBatch = statMessage.getAgentStatBatch();
            this.dispatch(agentStatBatch, MessageTypes.AGENT_STAT_BATCH, statBatchHandler, call.getUidFetcher(), response);
        } else if (statMessage.hasAgentUriStat()) {
            PAgentUriStat agentUriStat = statMessage.getAgentUriStat();
            this.dispatch(agentUriStat, MessageTypes.AGENT_URI_STAT, uriStatHandler, call.getUidFetcher(), response);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Found empty stat message header:{}", ServerContext.getAgentInfo());
            }
        }
    }

    private <T> void dispatch(T data,
                              MessageType messageType,
                              SimpleHandler<T> handler,
                              UidFetcher fetcher,
                              ServerCallStream<PStatMessage, Empty> responseObserver) {
        final Context context = Context.current();
        final ServerRequest<T> request = this.serverRequestFactory.newServerRequest(context, fetcher, messageType, data);
        try {
            handler.handleSimple(request);
        } catch (Throwable e) {
            logger.warn("Failed to request. header={}", request.getHeader(), e);
            responseObserver.onNextError(e);
        }
    }
}