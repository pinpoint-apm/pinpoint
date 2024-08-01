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
public class SpanService extends SpanGrpc.SpanImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler;
    private final ServerRequestFactory serverRequestFactory;

    public SpanService(DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler, ServerRequestFactory serverRequestFactory) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
    }

    @Override
    public StreamObserver<PSpanMessage> sendSpan(final StreamObserver<Empty> responseStream) {
        final ServerCallStreamObserver<Empty> responseObserver = (ServerCallStreamObserver<Empty>) responseStream;

        StreamObserver<PSpanMessage> observer = new StreamObserver<>() {
            @Override
            public void onNext(PSpanMessage spanMessage) {
                if (isDebug) {
                    logger.debug("Send PSpan={}", MessageFormatUtils.debugLog(spanMessage));
                }

                if (spanMessage.hasSpan()) {
                    final Message<PSpan> message = newMessage(spanMessage.getSpan(), DefaultTBaseLocator.SPAN);
                    send(message, responseObserver);
                } else if (spanMessage.hasSpanChunk()) {
                    final Message<PSpanChunk> message = newMessage(spanMessage.getSpanChunk(), DefaultTBaseLocator.SPANCHUNK);
                    send(message, responseObserver);
                } else {
                    if (isDebug) {
                        logger.debug("Found empty span message {}", MessageFormatUtils.debugLog(spanMessage));
                    }
                    onError(Status.INVALID_ARGUMENT.withDescription("Invalid Request").asException());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                com.navercorp.pinpoint.grpc.Header header = ServerContext.getAgentInfo();

                Status status = Status.fromThrowable(throwable);
                Metadata metadata = Status.trailersFromThrowable(throwable);
                if (logger.isInfoEnabled()) {
                    logger.info("onError: Failed to span stream, {} {} {}", header, status, metadata);
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
            onError(responseObserver, e);
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