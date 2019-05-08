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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import io.grpc.NameResolverProvider;
import io.grpc.stub.StreamObserver;

/**
 * @author jaehong.kim
 */
public class SpanGrpcDataSender extends GrpcDataSender {
    private final SpanGrpc.SpanStub spanStub;
    private volatile StreamObserver<PSpan> spanStream;
    private volatile StreamObserver<PSpanChunk> spanChunkStream;

    public SpanGrpcDataSender(String name, String host, int port, MessageConverter<GeneratedMessageV3> messageConverter, HeaderFactory headerFactory, NameResolverProvider nameResolverProvider) {
        super(name, host, port, messageConverter, headerFactory, nameResolverProvider);

        this.spanStub = SpanGrpc.newStub(managedChannel);
        this.spanStream = newSpanStream();
        this.spanChunkStream = newSpanChunkStream();
    }

    private StreamObserver<PSpanChunk> newSpanChunkStream() {
        final ResponseStreamObserver responseObserver = new ResponseStreamObserver();
        final StreamObserver<PSpanChunk> pSpanChunkStreamObserver = spanStub.sendSpanChunk(responseObserver);

        responseObserver.setReconnectAction(new ExponentialBackoffReconnectJob() {
            @Override
            public void run() {
                spanChunkStream = spanStub.sendSpanChunk(responseObserver);
            }
        });
        return pSpanChunkStreamObserver;
    }

    private StreamObserver<PSpan> newSpanStream() {
        final ResponseStreamObserver responseObserver = new ResponseStreamObserver();
        StreamObserver<PSpan> pSpanStreamObserver = spanStub.sendSpan(responseObserver);

        responseObserver.setReconnectAction(new ExponentialBackoffReconnectJob() {
            @Override
            public void run() {
                spanStream = spanStub.sendSpan(responseObserver);
            }
        });

        return pSpanStreamObserver;
    }

    public boolean send0(Object data) {
        final GeneratedMessageV3 spanMessage = messageConverter.toMessage(data);
        if (logger.isDebugEnabled()) {
            logger.debug("message:{}", spanMessage);
        }
        if (spanMessage instanceof PSpanChunk) {
            final PSpanChunk pSpan = (PSpanChunk) spanMessage;
            spanChunkStream.onNext(pSpan);
            return true;
        }
        if (spanMessage instanceof PSpan) {
            final  PSpan pSpan = (PSpan) spanMessage;
            spanStream.onNext(pSpan);
            return true;
        }
        throw new IllegalStateException("unsupported message " + data);
    }
}