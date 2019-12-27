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
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PStatMessage;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
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
public class StatService extends StatGrpc.StatImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final DispatchHandler dispatchHandler;
    private final ServerRequestFactory serverRequestFactory;

    public StatService(DispatchHandler dispatchHandler, ServerRequestFactory serverRequestFactory) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
    }

    @Override
    public StreamObserver<PStatMessage> sendAgentStat(StreamObserver<Empty> responseObserver) {
        StreamObserver<PStatMessage> observer = new StreamObserver<PStatMessage>() {
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
                } else {
                    if (isDebug) {
                        logger.debug("Found empty stat message {}", MessageFormatUtils.debugLog(statMessage));
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                final StatusError statusError = StatusErrors.throwable(throwable);
                if (statusError.isSimpleError()) {
                    logger.info("Failed to stat stream, cause={}", statusError.getMessage());
                } else {
                    logger.warn("Failed to stat stream, cause={}", statusError.getMessage(), statusError.getThrowable());
                }
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Empty.newBuilder().build());
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
            ServerRequest<?> request = serverRequestFactory.newServerRequest(message);
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