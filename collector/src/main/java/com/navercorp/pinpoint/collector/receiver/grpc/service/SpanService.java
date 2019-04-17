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
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;

public class SpanService extends SpanGrpc.SpanImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DispatchHandler dispatchHandler;
    private final ServerRequestFactory serverRequestFactory = new ServerRequestFactory();

    public SpanService(DispatchHandler dispatchHandler) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
    }

    @Override
    public StreamObserver<PSpan> sendSpan(final StreamObserver<Empty> responseObserver) {
        StreamObserver<PSpan> observer = new StreamObserver<PSpan>() {
            @Override
            public void onNext(PSpan pSpan) {
                logger.debug("Send Span {}", pSpan);
                final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, DefaultTBaseLocator.SPAN);
                final HeaderEntity headerEntity = new HeaderEntity(new HashMap<String, String>());
                final Message<PSpan> message = new DefaultMessage<PSpan>(header, headerEntity, pSpan);
                send(responseObserver, message);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.warn("span stream-error ", throwable);
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

    @Override
    public StreamObserver<PSpanChunk> sendSpanChunk(StreamObserver<Empty> responseObserver) {
        StreamObserver<PSpanChunk> observer = new StreamObserver<PSpanChunk>() {
            @Override
            public void onNext(PSpanChunk pSpanChunk) {
                logger.debug("Send SpanChunk {}", pSpanChunk);
                final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, DefaultTBaseLocator.SPANCHUNK);
                final HeaderEntity headerEntity = new HeaderEntity(new HashMap<>());
                Message<PSpanChunk> message = new DefaultMessage<>(header, headerEntity, pSpanChunk);
                send(responseObserver, message);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.warn("spanChunk stream-error ", throwable);
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

    private void send(StreamObserver<Empty> responseObserver, final Message<?> message) {
        ServerRequest<?> request;
        try {
            request = serverRequestFactory.newServerRequest(message);
        } catch (StatusException e) {
            logger.warn("serverRequest create fail Caused by:" + e.getMessage(), e);
            responseObserver.onError(e);
            return;
        }
        this.dispatchHandler.dispatchSendMessage(request);
    }
}
