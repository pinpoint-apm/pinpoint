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


import com.navercorp.pinpoint.grpc.client.ChannelFactoryOption;

import com.google.protobuf.Empty;

import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.StreamObserver;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class SpanGrpcDataSender extends GrpcDataSender {
    private final SpanGrpc.SpanStub spanStub;

    private volatile StreamObserver<PSpan> spanStream;
    private final ReconnectJob spanStreamReconnectAction;

    private volatile StreamObserver<PSpanChunk> spanChunkStream;
    private final ReconnectJob spanChunkReconnectAction;

    public SpanGrpcDataSender(String host, int port, int executorQueueSize, MessageConverter<GeneratedMessageV3> messageConverter, ChannelFactoryOption channelFactoryOption) {
        super(host, port, executorQueueSize, messageConverter, channelFactoryOption);

        this.spanStub = SpanGrpc.newStub(managedChannel);

        spanStreamReconnectAction = new ExponentialBackoffReconnectJob() {
            @Override
            public void run() {
                spanStream = newSpanStream();
            }
        };
        this.spanStream = newSpanStream();

        spanChunkReconnectAction = new ExponentialBackoffReconnectJob() {
            @Override
            public void run() {
                spanChunkStream = newSpanChunkStream();
            }
        };
        this.spanChunkStream = newSpanChunkStream();
    }

    private StreamObserver<PSpan> newSpanStream() {
        ResponseStreamObserver<PSpan, Empty> responseStreamObserver = new ResponseStreamObserver<PSpan, Empty>(name, reconnector, spanStreamReconnectAction);
        return spanStub.sendSpan(responseStreamObserver);
    }

    private StreamObserver<PSpanChunk> newSpanChunkStream() {
        ResponseStreamObserver<PSpanChunk, Empty> responseStreamObserver = new ResponseStreamObserver<PSpanChunk, Empty>(name, reconnector, spanChunkReconnectAction);
        return spanStub.sendSpanChunk(responseStreamObserver);
    }

    public boolean send0(Object data) {
        final GeneratedMessageV3 spanMessage = messageConverter.toMessage(data);
        if (logger.isDebugEnabled()) {
            logger.debug("message:{}", debugLog(spanMessage));
        }
        if (spanMessage instanceof PSpanChunk) {
            final PSpanChunk pSpan = (PSpanChunk) spanMessage;
            spanChunkStream.onNext(pSpan);
            return true;
        }
        if (spanMessage instanceof PSpan) {
            final PSpan pSpan = (PSpan) spanMessage;
            spanStream.onNext(pSpan);
            return true;
        }
        throw new IllegalStateException("unsupported message " + data);
    }

    @Override
    public void stop() {
        logger.info("spanStream.close()");
        StreamUtils.close(this.spanStream);
        logger.info("spanChunkStream.close()");
        StreamUtils.close(this.spanChunkStream);

        super.stop();
    }


}