package com.navercorp.pinpoint.grpc.server;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class StreamCountInterceptorTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    Server start;
    ManagedChannel channel;

    StreamCountInterceptor interceptor = new StreamCountInterceptor();

    @BeforeEach
    public void setUp() throws Exception {
        SpanGrpc.SpanImplBase spanImpl =
                new SpanGrpc.SpanImplBase() {
                    @Override
                    public StreamObserver<PSpanMessage> sendSpan(StreamObserver<Empty> observer) {
                        ServerCallStreamObserver<Empty> responseObserver = (ServerCallStreamObserver) observer;
                        return new StreamObserver<PSpanMessage>() {
                            private boolean responseMessage;

                            @Override
                            public void onNext(PSpanMessage value) {
                                logger.info("Server-onNext {}", value);
                                responseObserver.onNext(Empty.getDefaultInstance());
                                responseMessage = true;
                            }

                            @Override
                            public void onError(Throwable t) {
                                Status status = Status.fromThrowable(t);
                                logger.info("Server-onError {}", status);
                                logResponse(responseObserver, "Server-onError");

                                if (!responseMessage) {
                                    responseObserver.onNext(Empty.getDefaultInstance());
                                }
                                responseObserver.onCompleted();
                                logResponse(responseObserver, "Server-onError");
                            }

                            @Override
                            public void onCompleted() {
                                logger.info("Server-onCompleted");
                                logResponse(responseObserver, "Server-onCompleted");

                                if (!responseMessage) {
                                    responseObserver.onNext(Empty.getDefaultInstance());
                                }
                                responseObserver.onCompleted();
                            }

                            public void logResponse(ServerCallStreamObserver<?> responseObserver, String event) {
                                logger.info("{} (responseObserver isReady:{}, cancel:{})", event, responseObserver.isReady(), responseObserver.isCancelled());
                            }
                        };
                    }
                };
        String serverName = InProcessServerBuilder.generateName();
        this.start = InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(spanImpl, interceptor))
                .build().start();

        this.channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    }

    @AfterEach
    void tearDown() {
        this.channel.shutdown();
        try {
            this.channel.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.start.shutdown();
        try {
            this.start.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void currentStream() {
        SpanGrpc.SpanStub spanStub = SpanGrpc.newStub(channel);

        PSpanMessage pSpanMessage = PSpanMessage.newBuilder().build();

        ClientCallStreamObserver<PSpanMessage> requestStream1 = sendSpan(spanStub);
        requestStream1.onNext(pSpanMessage);

        Assertions.assertEquals(1, interceptor.getCurrentStream());

        ClientCallStreamObserver<PSpanMessage> requestStream2 = sendSpan(spanStub);
        requestStream2.onNext(pSpanMessage);

        Assertions.assertEquals(2, interceptor.getCurrentStream());

        requestStream2.onCompleted();

        Assertions.assertEquals(1, interceptor.getCurrentStream());

        requestStream1.onError(Status.UNAVAILABLE.withDescription("testcase").asRuntimeException());

        Assertions.assertEquals(0, interceptor.getCurrentStream());

    }

    @SuppressWarnings("unchecked")
    private ClientCallStreamObserver<PSpanMessage> sendSpan(SpanGrpc.SpanStub spanStub) {
        return (ClientCallStreamObserver<PSpanMessage>) spanStub.sendSpan(new ClientResponseObserver<PSpanMessage, Empty>() {
            private ClientCallStreamObserver<PSpanMessage> requestStream;
            @Override
            public void beforeStart(ClientCallStreamObserver requestStream) {
                this.requestStream = requestStream;
            }

            @Override
            public void onNext(Empty value) {
                logger.info("Client-onNext {}", value);
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                logger.info("Client-onError {} requestStream.isReady:{}", status, requestStream.isReady());
            }

            @Override
            public void onCompleted() {
                logger.info("Client-onCompleted requestStream.isReady:{}", requestStream.isReady());
            }
        });
    }
}