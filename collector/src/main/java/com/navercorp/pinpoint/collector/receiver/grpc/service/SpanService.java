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

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.StatusError;
import com.navercorp.pinpoint.grpc.StatusErrors;
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

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class SpanService extends SpanGrpc.SpanImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final DispatchHandler dispatchHandler;
    private final ServerRequestFactory serverRequestFactory;

    public SpanService(DispatchHandler dispatchHandler, ServerRequestFactory serverRequestFactory) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
    }

    @Override
    public StreamObserver<PSpanMessage> sendSpan(final StreamObserver<Empty> responseObserver) {
        StreamObserver<PSpanMessage> observer = new StreamObserver<PSpanMessage>() {
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
                }
            }

            @Override
            public void onError(Throwable throwable) {
                final StatusError statusError = StatusErrors.throwable(throwable);
                if (statusError.isSimpleError()) {
                    logger.info("Failed to span stream, cause={}", statusError.getMessage());
                } else {
                    logger.warn("Failed to span stream, cause={}", statusError.getMessage(), statusError.getThrowable());
                }
            }

            @Override
            public void onCompleted() {
                Empty empty = Empty.newBuilder().build();
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

    private void send(final Message<? extends GeneratedMessageV3> message, StreamObserver<Empty> responseObserver) {
        try {
            ServerRequest<? extends GeneratedMessageV3> request = serverRequestFactory.newServerRequest(message);
            this.dispatchHandler.dispatchSendMessage(request);
        } catch (Exception e) {
            logger.warn("Failed to request. message={}", message, e);
            if (e instanceof StatusException || e instanceof StatusRuntimeException) {
                responseObserver.onError(e);
            } else {
                // Avoid detailed exception
                responseObserver.onError(Status.INTERNAL.withDescription("Bad Request").asException());
            }
        }
    }

}