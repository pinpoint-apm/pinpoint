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
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PStatMessage;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
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
public class StatService extends StatGrpc.StatImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final AtomicLong serverStreamId = new AtomicLong();

    private final DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler;
    private final ServerRequestFactory serverRequestFactory;
    private final StreamCloseOnError streamCloseOnError;

    public StatService(DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                       ServerRequestFactory serverRequestFactory,
                       StreamCloseOnError streamCloseOnError) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
        this.streamCloseOnError = Objects.requireNonNull(streamCloseOnError, "streamCloseOnError");
    }

    @Override
    public StreamObserver<PStatMessage> sendAgentStat(StreamObserver<Empty> responseStream) {
        final ServerCallStreamObserver<Empty> responseObserver = (ServerCallStreamObserver<Empty>) responseStream;
        return new ServerCallStream<>(logger, serverStreamId.incrementAndGet(), responseObserver, this::messageDispatch, streamCloseOnError, Empty::getDefaultInstance);
    }

    private void messageDispatch(PStatMessage statMessage, ServerCallStream<PStatMessage, Empty> response) {
        if (isDebug) {
            logger.debug("Send PAgentStat={}", MessageFormatUtils.debugLog(statMessage));
        }
        if (statMessage.hasAgentStat()) {
            PAgentStat agentStat = statMessage.getAgentStat();
            ServerRequest<PAgentStat> request = this.serverRequestFactory.newServerRequest(MessageType.AGENT_STAT, agentStat);
            this.dispatch(request, response);
        } else if (statMessage.hasAgentStatBatch()) {
            PAgentStatBatch agentStatBatch = statMessage.getAgentStatBatch();
            ServerRequest<PAgentStatBatch> request = this.serverRequestFactory.newServerRequest(MessageType.AGENT_STAT_BATCH, agentStatBatch);
            this.dispatch(request, response);
        } else if (statMessage.hasAgentUriStat()) {
            PAgentUriStat agentUriStat = statMessage.getAgentUriStat();
            ServerRequest<PAgentUriStat> request = this.serverRequestFactory.newServerRequest(MessageType.AGENT_URI_STAT, agentUriStat);
            this.dispatch(request, response);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Found empty stat message header:{}", ServerContext.getAgentInfo());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatch(ServerRequest<? extends GeneratedMessageV3> request, ServerCallStream<PStatMessage, Empty> responseObserver) {
        try {
            dispatchHandler.dispatchSendMessage((ServerRequest<GeneratedMessageV3>) request);
        } catch (Throwable e) {
            logger.warn("Failed to request. header={}", request.getHeader(), e);
            responseObserver.onNextError(e);
        }
    }
}