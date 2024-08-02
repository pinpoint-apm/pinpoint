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
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class StatService extends StatGrpc.StatImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

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
        StreamObserver<PStatMessage> observer = new StreamObserver<>() {
            @Override
            public void onNext(PStatMessage statMessage) {
                if (isDebug) {
                    logger.debug("Send PAgentStat={}", MessageFormatUtils.debugLog(statMessage));
                }

                if (statMessage.hasAgentStat()) {
                    final Message<PAgentStat> message = newMessage(statMessage.getAgentStat(), DefaultTBaseLocator.AGENT_STAT);
                    send(message, responseObserver);
                } else if (statMessage.hasAgentStatBatch()) {
                    final Message<PAgentStatBatch> message = newMessage(statMessage.getAgentStatBatch(), DefaultTBaseLocator.AGENT_STAT_BATCH);
                    send(message, responseObserver);
                } else if (statMessage.hasAgentUriStat()) {
                    final Message<PAgentUriStat> message = newMessage(statMessage.getAgentUriStat(), DefaultTBaseLocator.AGENT_URI_STAT);
                    send(message, responseObserver);
                } else {
                    if (isDebug) {
                        logger.debug("Found empty stat message {}", MessageFormatUtils.debugLog(statMessage));
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                Metadata metadata = Status.trailersFromThrowable(throwable);
                if (logger.isInfoEnabled()) {
                    logger.info("onError: Failed to stat stream, {} {}", status, metadata);
                }
                responseCompleted(responseObserver);
            }

            @Override
            public void onCompleted() {
                com.navercorp.pinpoint.grpc.Header header = ServerContext.getAgentInfo();
                logger.info("onCompleted {}", header);
                responseCompleted(responseObserver);
            }

            private void responseCompleted(ServerCallStreamObserver<Empty> responseObserver) {
                if (responseObserver.isCancelled()) {
                    logger.info("responseCompleted: ResponseObserver is cancelled");
                    return;
                }
                Empty empty = Empty.getDefaultInstance();
                responseObserver.onNext(empty);
                responseObserver.onCompleted();
            }
        };

        return observer;
    }

    private <T> Message<T> newMessage(T requestData, short serviceType) {
        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, serviceType);
        final HeaderEntity headerEntity = new HeaderEntity(new HashMap<>());
        return new DefaultMessage<>(header, headerEntity, requestData);
    }

    private void send(final Message<? extends GeneratedMessageV3> message, ServerCallStreamObserver<Empty> responseObserver) {
        try {
            ServerRequest<GeneratedMessageV3> request = (ServerRequest<GeneratedMessageV3>) serverRequestFactory.newServerRequest(message);
            this.dispatchHandler.dispatchSendMessage(request);
        } catch (Throwable e) {
            logger.warn("Failed to request. message={}", message, e);
            if (this.streamCloseOnError.onError(e)) {
                onError(responseObserver, e);
            }
        }
    }

    private void onError(ServerCallStreamObserver<Empty> responseObserver, Throwable e) {
        if (responseObserver.isCancelled()) {
            logger.info("onError: ResponseObserver is cancelled");
            return;
        }
        if (e instanceof StatusException || e instanceof StatusRuntimeException) {
            responseObserver.onError(e);
        } else {
            // Avoid detailed exception
            StatusException statusException = Status.INTERNAL.withDescription("Bad Request").asException();
            responseObserver.onError(statusException);
        }
    }

}