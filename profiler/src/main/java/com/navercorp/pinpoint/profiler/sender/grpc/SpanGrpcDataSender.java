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


import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryOption;

import com.google.protobuf.Empty;

import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.util.concurrent.RejectedExecutionException;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class SpanGrpcDataSender extends GrpcDataSender {
    private final SpanGrpc.SpanStub spanStub;
    private final ReconnectExecutor reconnectExecutor;

    private volatile StreamObserver<PSpanMessage> spanStream;
    private final Reconnector spanStreamReconnector;

    public SpanGrpcDataSender(String host, int port,
                              int executorQueueSize,
                              MessageConverter<GeneratedMessageV3> messageConverter,
                              ReconnectExecutor reconnectExecutor,
                              ChannelFactoryOption channelFactoryOption) {
        super(host, port, executorQueueSize, messageConverter, channelFactoryOption);

        this.spanStub = SpanGrpc.newStub(managedChannel).withInterceptors(new CheckReadyClientInterceptor());
        this.reconnectExecutor = Assert.requireNonNull(reconnectExecutor, "reconnectExecutor must not be null");
        {
            final Runnable spanStreamReconnectJob = new Runnable() {
                @Override
                public void run() {
                    spanStream = newSpanStream();
                }
            };
            this.spanStreamReconnector = reconnectExecutor.newReconnector(spanStreamReconnectJob);
            this.spanStream = newSpanStream();
        }
    }

    private StreamObserver<PSpanMessage> newSpanStream() {
        StreamId spanId = StreamId.newStreamId("SpanStream");
        ResponseStreamObserver<PSpanMessage, Empty> responseStreamObserver = new ResponseStreamObserver<PSpanMessage, Empty>(spanId, spanStreamReconnector);
        return spanStub.sendSpan(responseStreamObserver);
    }

    @Override
    public boolean send(final Object data) {
        final Runnable command = new Runnable() {
            @Override
            public void run() {
                try {
                    send0(data);
                } catch (Exception ex) {
                    logger.debug("send fail:{}", data, ex);
                }
            }
        };
        try {
            executor.execute(command);
        } catch (RejectedExecutionException reject) {
            logger.debug("reject:{}", command);
            return false;
        }
        return true;
    }

    private boolean send0(Object data) {
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
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        logger.info("Stop {}, channel={}", name, managedChannel);
        final ReconnectExecutor reconnectExecutor = this.reconnectExecutor;
        if (reconnectExecutor != null) {
            reconnectExecutor.close();
        }
        logger.info("{} close()", this.spanStream);
        StreamUtils.close(this.spanStream);
        release();
    }

    @Override
    public String toString() {
        return "SpanGrpcDataSender{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                "} " + super.toString();
    }

    private class CheckReadyClientInterceptor implements ClientInterceptor {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                @Override
                public void sendMessage(ReqT message) {
                    if (isReady()) {
                        super.sendMessage(message);
                    } else {
                        logger.info("Drop span message, stream not ready.");
                    }
                }

                @Override
                public void cancel(@Nullable String message, @Nullable Throwable cause) {
                    logger.info("Cancel message. message={}, cause={}", message, cause.getMessage(), cause);
                    super.cancel(message, cause);
                }
            };
        }
    }
}