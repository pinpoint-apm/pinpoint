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
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcRequestHeader;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcRequestHeaderContextValue;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.TraceGrpc;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.DefaultServerRequest;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class TraceService extends TraceGrpc.TraceImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private DispatchHandler dispatchHandler;


    public TraceService(DispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
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
                send(message);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Empty.newBuilder().build());
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
                final HeaderEntity headerEntity = new HeaderEntity(new HashMap<String, String>());
                Message<PSpanChunk> message = new DefaultMessage<PSpanChunk>(header, headerEntity, pSpanChunk);
                send(message);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Failed to received PSpan");
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
            }
        };

        return observer;
    }


    private void send(final Message<?> message) {
        final GrpcRequestHeader requestHeaderContextValue = GrpcRequestHeaderContextValue.get();
        if (requestHeaderContextValue == null) {
            // TODO Handle error
            logger.warn("Not found request header");
            return;
        }
        ServerRequest request = new DefaultServerRequest(message, requestHeaderContextValue.getRemoteAddress(), requestHeaderContextValue.getRemotePort());
        if (dispatchHandler != null) {
            logger.debug("Dispatch handler={}, message={}", this.dispatchHandler, message);
            // dispatchHandler.dispatchSendMessage(request);

        }
    }
}
