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
import com.navercorp.pinpoint.grpc.client.config.ClientOption;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.client.UnaryCallDeadlineInterceptor;
import com.navercorp.pinpoint.grpc.client.interceptor.DiscardClientInterceptor;
import com.navercorp.pinpoint.grpc.client.interceptor.DiscardEventListener;
import com.navercorp.pinpoint.grpc.client.interceptor.LoggingDiscardEventListener;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import com.navercorp.pinpoint.profiler.sender.grpc.Reconnector;
import com.navercorp.pinpoint.profiler.sender.grpc.ResponseStreamObserver;
import com.navercorp.pinpoint.profiler.sender.grpc.SpanGrpcDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamEventListener;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamId;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SpanClientMock {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ReconnectExecutor reconnectExecutor = new ReconnectExecutor(scheduledExecutorService);



    private final ManagedChannel channel;
    private final SpanGrpc.SpanStub spanStub;
    private final ChannelFactory channelFactory;
    private final AtomicLong total = new AtomicLong(0);
    private volatile StreamObserver<PSpanMessage> spanStream;
    private final Reconnector spanStreamReconnector;

    public SpanClientMock(final String host, final int port) throws Exception {
        channelFactory = newChannelFactory();
        this.channel = channelFactory.build("SpanClientMock", host, port);

        final AtomicBoolean onReadyFlag = new AtomicBoolean();
        this.spanStub = SpanGrpc.newStub(channel);
//        this.spanStub = SpanGrpc.newStub(channel).withInterceptors(new ClientInterceptor() {
//            @Override
//            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
//                return new ForwardClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
//                    AtomicLong counter = new AtomicLong(0);
//                    @Override
//                    public void sendMessage(ReqT message) {
//                        try {
//                            super.sendMessage(message);
//                        } catch(Exception e) {
//                            System.out.println("Exception " + e.getMessage());
//                        }
//
//                        if (Boolean.FALSE == isReady()) {
//                            if(onReadyFlag.compareAndSet(Boolean.TRUE, Boolean.FALSE)) {
//                                System.out.println("");
//                            }
//                            System.out.print("D");
////                            logger.info("Drop span message, stream not ready. " + counter.incrementAndGet());
//                        } else {
//                            if(onReadyFlag.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
//                                System.out.println("");
//                            }
//                            System.out.print("S");
//                        }
//                    }
//
//                    @Override
//                    public void cancel(String message, Throwable cause) {
//                        logger.info("Cancel message. message={}, cause={}", message, cause.getMessage(), cause);
//                        super.cancel(message, cause);
//                    }
//
//
//
//                };
//            }
//        });
        final Runnable spanStreamReconnectJob = new Runnable() {
            @Override
            public void run() {
                spanStream = newSpanStream();
            }
        };
        this.spanStreamReconnector = reconnectExecutor.newReconnector(spanStreamReconnectJob);
        spanStreamReconnectJob.run();



        logger.info("CallOptions={}, channel={}", spanStub.getCallOptions(), spanStub.getChannel());
    }

    private StreamObserver<PSpanMessage> newSpanStream() {
        System.out.println("### ");
        System.out.println("NEW SpanStream");
        System.out.println("###");
        StreamId spanId = StreamId.newStreamId("SpanStream");
        StreamEventListener<PSpanMessage> listener = new StreamEventListener<PSpanMessage>() {

            @Override
            public void start(ClientCallStreamObserver<PSpanMessage> requestStream) {
                spanStreamReconnector.reset();
            }

            @Override
            public void onError(Throwable t) {
                spanStreamReconnector.reconnect();
            }

            @Override
            public void onCompleted() {
                spanStreamReconnector.reconnect();
            }
        };
        ResponseStreamObserver<PSpanMessage, Empty> responseStreamObserver = new ResponseStreamObserver<PSpanMessage, Empty>(listener);
        return spanStub.sendSpan(responseStreamObserver);
    }


    private ChannelFactory newChannelFactory() {
        HeaderFactory headerFactory = new AgentHeaderFactory("mockAgentId", "mockApplicationName", System.currentTimeMillis());

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("SpanClientMock");
        final ClientInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(1000);
        channelFactoryBuilder.addClientInterceptor(unaryCallDeadlineInterceptor);

        final ClientInterceptor discardClientInterceptor = newDiscardClientInterceptor();
        channelFactoryBuilder.addClientInterceptor(discardClientInterceptor);

        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setClientOption(new ClientOption());

        return channelFactoryBuilder.build();
    }

    private ClientInterceptor newDiscardClientInterceptor() {
        final int spanDiscardLogRateLimit = 1000;
        final long spanDiscardMaxPendingThreshold = 1000;
        final long spanDiscardCountForReconnect = 5 * 60 * 100;
        final long spanNotReadyTimeoutMillis = 5 * 60 * 1000;
        final DiscardEventListener<?> discardEventListener = new LoggingDiscardEventListener(SpanGrpcDataSender.class.getName(), spanDiscardLogRateLimit);
        return new DiscardClientInterceptor(discardEventListener, spanDiscardMaxPendingThreshold, 1000, 1000);
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
        final int size = 1000;
        final byte[] bytes = new byte[size];
        for(int i = 0; i < size; i++) {
            bytes[i] = (byte) i;
        }

        PAnnotationValue value = PAnnotationValue.newBuilder().setBinaryValue(ByteString.copyFrom(bytes)).build();
        PAnnotation annotation = PAnnotation.newBuilder().setValue(value).build();
        PSpanEvent spanEvent = PSpanEvent.newBuilder().addAnnotation(annotation).build();

        AtomicLong counter = new AtomicLong();
        service.execute(new Runnable() {
            @Override
            public void run() {
//                StreamObserver<Empty> responseObserver = getResponseObserver();
//                StreamObserver<PSpanMessage> requestObserver = spanStub.sendSpan(responseObserver);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                }

                for (int i = 0; i < count; i++) {
                    final PSpan span = PSpan.newBuilder().setSpanId(i).addSpanEvent(spanEvent).build();
                    final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpan(span).build();
                    spanStream.onNext(spanMessage);
//                    requestObserver.onNext(spanMessage);
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                    }
//                    System.out.print("S");
                }
                spanStream.onCompleted();
//                requestObserver.onCompleted();
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

//                StreamObserver<Empty> responseObserver = getResponseObserver();
//                StreamObserver<PSpanMessage> requestObserver = spanStub.sendSpan(responseObserver);
                for (int i = 0; i < count; i++) {
                    final PSpanChunk spanChunk = PSpanChunk.newBuilder().build();
                    final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpanChunk(spanChunk).build();
                    spanStream.onNext(spanMessage);
//                    requestObserver.onNext(spanMessage);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                spanStream.onCompleted();
//                requestObserver.onCompleted();
            }
        });

    }

//    private StreamObserver<Empty> getResponseObserver() {
//        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
//            @Override
//            public void onNext(Empty pResult) {
//                logger.info("Response {}", pResult);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                logger.info("Error ", throwable);
//            }
//
//            @Override
//            public void onCompleted() {
//                logger.info("Completed");
//            }
//        };
//        return responseObserver;
//    }
}
