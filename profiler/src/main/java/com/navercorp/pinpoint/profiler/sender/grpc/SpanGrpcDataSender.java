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
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
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
    private final ExecutorAdaptor reconnectExecutor;

    private volatile StreamObserver<PSpanMessage> spanStream;
    private final Reconnector spanStreamReconnector;

    public SpanGrpcDataSender(String host, int port, int executorQueueSize, MessageConverter<GeneratedMessageV3> messageConverter, ChannelFactoryOption channelFactoryOption) {
        super(host, port, executorQueueSize, messageConverter, channelFactoryOption);

        this.spanStub = SpanGrpc.newStub(managedChannel);
        this.reconnectExecutor = newReconnectExecutor();
        {
            final Runnable spanStreamReconnectJob = new Runnable() {
                @Override
                public void run() {
                    spanStream = newSpanStream();
                }
            };
            this.spanStreamReconnector = new ReconnectAdaptor(reconnectExecutor, spanStreamReconnectJob);
            this.spanStream = newSpanStream();
        }
    }

    private ExecutorAdaptor newReconnectExecutor() {
        return new ExecutorAdaptor(GrpcDataSender.reconnectScheduler);
    }

    private StreamObserver<PSpanMessage> newSpanStream() {
        StreamId spanId = StreamId.newStreamId("span");
        ResponseStreamObserver<PSpanMessage, Empty> responseStreamObserver = new ResponseStreamObserver<PSpanMessage, Empty>(spanId, spanStreamReconnector);
        return spanStub.sendSpan(responseStreamObserver);
    }

    public boolean send0(Object data) {
        final GeneratedMessageV3 message = messageConverter.toMessage(data);
        if (logger.isDebugEnabled()) {
            logger.debug("Send message={}", debugLog(message));
        }
        if (message instanceof PSpanChunk) {
            final PSpanChunk spanChunk = (PSpanChunk) message;
            final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpanChunk(spanChunk).build();
            spanStream.onNext(spanMessage);
            return true;
        }
        if (message instanceof PSpan) {
            final PSpan pSpan = (PSpan) message;
            final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpan(pSpan).build();
            spanStream.onNext(spanMessage);
            return true;
        }
        throw new IllegalStateException("unsupported message " + data);
    }

    @Override
    public void stop() {
        logger.info("spanStream.close()");
        StreamUtils.close(this.spanStream);
        super.stop();
    }
}