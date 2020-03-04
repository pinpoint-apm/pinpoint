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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.ClientOption;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SpanClientMock {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagedChannel channel;
    private final SpanGrpc.SpanStub spanStub;
    private final ChannelFactory channelFactory;
    private final AtomicLong total = new AtomicLong(0);

    public SpanClientMock(final String host, final int port) throws Exception {
        channelFactory = newChannelFactory();
        this.channel = channelFactory.build("SpanClientMock", host, port);
        this.spanStub = SpanGrpc.newStub(channel).withInterceptors(new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
                return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                    AtomicLong counter = new AtomicLong(0);
                    @Override
                    public void sendMessage(ReqT message) {
                        if (isReady()) {
                            super.sendMessage(message);
                        } else {
                            logger.info("Drop span message, stream not ready.");
                            System.out.println(counter.incrementAndGet());
                        }
                    }

                    @Override
                    public void cancel(String message, Throwable cause) {
                        logger.info("Cancel message. message={}, cause={}", message, cause.getMessage(), cause);
                        super.cancel(message, cause);
                    }
                };
            }
        });
        logger.info("CallOptions={}, channel={}", spanStub.getCallOptions(), spanStub.getChannel());
    }

    private ChannelFactory newChannelFactory() {
        HeaderFactory headerFactory = new AgentHeaderFactory("mockAgentId", "mockApplicationName", System.currentTimeMillis());

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("SpanClientMock");
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setClientOption(new ClientOption.Builder().build());

        return channelFactoryBuilder.build();
    }

    public void stop() throws InterruptedException {
        stop(5);
    }

    public void stop(long await) throws InterruptedException {
        channel.shutdown().awaitTermination(await, TimeUnit.SECONDS);
    }

    public void span() {
        span(1);
    }

    ExecutorService service = Executors.newFixedThreadPool(1);

    public void span(int count) {
        final int size = 10;
        final byte[] bytes = new byte[size];
        for(int i = 0; i < size; i++) {
            bytes[i] = (byte) i;
        }

        PAnnotationValue value = PAnnotationValue.newBuilder().setBinaryValue(ByteString.copyFrom(bytes)).build();
        PAnnotation annotation = PAnnotation.newBuilder().setValue(value).build();
        PSpanEvent spanEvent = PSpanEvent.newBuilder().addAnnotation(annotation).build();

        service.execute(new Runnable() {
            @Override
            public void run() {
                StreamObserver<Empty> responseObserver = getResponseObserver();
                StreamObserver<PSpanMessage> requestObserver = spanStub.sendSpan(responseObserver);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                }

                for (int i = 0; i < count; i++) {
                    final PSpan span = PSpan.newBuilder().addSpanEvent(spanEvent).build();
                    final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpan(span).build();
                    requestObserver.onNext(spanMessage);
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                    }

                }
                requestObserver.onCompleted();
            }
        });
    }

    public void spanChunk() {
        spanChunk(1);
    }

    public void spanChunk(final int count) {
        service.execute(new Runnable() {
            @Override
            public void run() {

                StreamObserver<Empty> responseObserver = getResponseObserver();

                StreamObserver<PSpanMessage> requestObserver = spanStub.sendSpan(responseObserver);
                for (int i = 0; i < count; i++) {
                    final PSpanChunk spanChunk = PSpanChunk.newBuilder().build();
                    final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpanChunk(spanChunk).build();
                    requestObserver.onNext(spanMessage);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                requestObserver.onCompleted();
            }
        });

    }

    private StreamObserver<Empty> getResponseObserver() {
        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty pResult) {
                logger.info("Response {}", pResult);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.info("Error ", throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("Completed");
            }
        };
        return responseObserver;
    }
}
