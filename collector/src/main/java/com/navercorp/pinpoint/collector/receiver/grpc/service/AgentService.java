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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcServerResponse;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcServerStreamResponse;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class AgentService extends AgentGrpc.AgentImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ServerRequestFactory serverRequestFactory = new ServerRequestFactory();
    private final DispatchHandler dispatchHandler;

    public AgentService(DispatchHandler dispatchHandler) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
    }

//    @Override
//    public void requestAgentInfo(PAgentInfo agentInfo, StreamObserver<PResult> responseObserver) {
//        if (isDebug) {
//            logger.debug("Request PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
//        }
//
//        Message<PAgentInfo> message = newMessage(agentInfo, DefaultTBaseLocator.AGENT_INFO);
//
//        simpleRequestHandlerAdaptor.request(message, responseObserver);
//    }


    @Override
    public StreamObserver<PAgentInfo> sendAgentInfo(StreamObserver<PResult> responseObserver) {
        StreamObserver<PAgentInfo> request = new StreamObserver<PAgentInfo>() {
            @Override
            public void onNext(PAgentInfo pAgentInfo) {
                if (isDebug) {
                    logger.debug("Send PAgentInfo={}", MessageFormatUtils.debugLog(pAgentInfo));
                }
                final Message<PAgentInfo> message = newMessage(pAgentInfo, DefaultTBaseLocator.AGENT_INFO);
                send(responseObserver, message);
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("Error sendAgentInfo stream", t);
            }

            @Override
            public void onCompleted() {
                if (isDebug) {
                    logger.debug("sendAgentInfo stream onCompleted()");
                }
                responseObserver.onCompleted();
            }
        };
        return request;
    }

    private <T> Message<T> newMessage(T requestData, short type) {
        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type);
        final HeaderEntity headerEntity = new HeaderEntity(Collections.emptyMap());
        return new DefaultMessage<T>(header, headerEntity, requestData);
    }


    private void send(StreamObserver<PResult> responseObserver, final Message<? extends GeneratedMessageV3> message) {
        ServerRequest<? extends GeneratedMessageV3> request;
        try {
            request = serverRequestFactory.newServerRequest(message);
            ServerResponse<PResult> streamResponse =  new GrpcServerStreamResponse<>(responseObserver);
            this.dispatchHandler.dispatchRequestMessage(request, streamResponse);
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