/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.common.util.CpuUtils;
import com.navercorp.pinpoint.pluginit.utils.SocketUtils;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.examples.manualflowcontrol.HelloReply;
import io.grpc.examples.manualflowcontrol.HelloRequest;
import io.grpc.examples.manualflowcontrol.StreamingGreeterGrpc;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.netty.channel.nio.NioEventLoopGroup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * copy grpc framework
 * - https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/manualflowcontrol/ManualFlowControlServer.java
 */
public class HelloWorldStreamServer implements HelloWorldServer {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private int requestCount;

    private Server server;

    private int bindPort;

    @PostConstruct
    public void start() throws IOException {
        StreamingGreeterGrpc.StreamingGreeterImplBase svc = new StreamingGreeterGrpc.StreamingGreeterImplBase() {
            @Override
            public StreamObserver<HelloRequest> sayHelloStreaming(final StreamObserver<HelloReply> responseObserver) {
                // Set up manual flow control for the request stream. It feels backwards to configure the request
                // stream's flow control using the response stream's observer, but this is the way it is.
                final ServerCallStreamObserver<HelloReply> serverCallStreamObserver =
                        (ServerCallStreamObserver<HelloReply>) responseObserver;
                serverCallStreamObserver.disableAutoInboundFlowControl();

                // Guard against spurious onReady() calls caused by a race between onNext() and onReady(). If the transport
                // toggles isReady() from false to true while onNext() is executing, but before onNext() checks isReady(),
                // request(1) would be called twice - once by onNext() and once by the onReady() scheduled during onNext()'s
                // execution.
                final AtomicBoolean wasReady = new AtomicBoolean(false);

                // Set up a back-pressure-aware consumer for the request stream. The onReadyHandler will be invoked
                // when the consuming side has enough buffer space to receive more messages.
                //
                // Note: the onReadyHandler's invocation is serialized on the same thread pool as the incoming StreamObserver's
                // onNext(), onError(), and onComplete() handlers. Blocking the onReadyHandler will prevent additional messages
                // from being processed by the incoming StreamObserver. The onReadyHandler must return in a timely manor or else
                // message processing throughput will suffer.
                serverCallStreamObserver.setOnReadyHandler(new Runnable() {
                    public void run() {
                        if (serverCallStreamObserver.isReady() && wasReady.compareAndSet(false, true)) {
                            logger.info("READY");
                            // Signal the request sender to send one message. This happens when isReady() turns true, signaling that
                            // the receive buffer has enough free space to receive more messages. Calling request() serves to prime
                            // the message pump.
                            serverCallStreamObserver.request(1);
                        }
                    }
                });

                // Give gRPC a StreamObserver that can observe and process incoming requests.
                return new StreamObserver<HelloRequest>() {
                    @Override
                    public void onNext(HelloRequest request) {
                        requestCount++;

                        // Process the request and send a response or an error.
                        try {
                            // Accept and enqueue the request.
                            String name = request.getName();
                            logger.info("--> " + name);

                            // Simulate server "work"
                            Thread.sleep(100);

                            // Send a response.
                            String message = "Hello " + name;
                            logger.info("<-- " + message);
                            HelloReply reply = HelloReply.newBuilder().setMessage(message).build();
                            responseObserver.onNext(reply);

                            // Check the provided ServerCallStreamObserver to see if it is still ready to accept more messages.
                            if (serverCallStreamObserver.isReady()) {
                                // Signal the sender to send another request. As long as isReady() stays true, the server will keep
                                // cycling through the loop of onNext() -> request()...onNext() -> request()... until either the client
                                // runs out of messages and ends the loop or the server runs out of receive buffer space.
                                //
                                // If the server runs out of buffer space, isReady() will turn false. When the receive buffer has
                                // sufficiently drained, isReady() will turn true, and the serverCallStreamObserver's onReadyHandler
                                // will be called to restart the message pump.
                                serverCallStreamObserver.request(1);
                            } else {
                                // If not, note that back-pressure has begun.
                                wasReady.set(false);
                            }
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                            responseObserver.onError(
                                    Status.UNKNOWN.withDescription("Error handling request").withCause(throwable).asException());
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        // End the response stream if the client presents an error.
                        t.printStackTrace();
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void onCompleted() {
                        // Signal the end of work when the client ends the request stream.
                        logger.info("COMPLETED");
                        responseObserver.onCompleted();
                    }
                };
            }
        };

        bindPort = SocketUtils.findAvailableTcpPort(27675);

        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(bindPort);
        if (serverBuilder instanceof NettyServerBuilder) {
            ExecutorService workerExecutor = Executors.newCachedThreadPool();
            NioEventLoopGroup eventExecutors = new NioEventLoopGroup(CpuUtils.cpuCount() + 5, workerExecutor);
            ((NettyServerBuilder) serverBuilder).workerEventLoopGroup(eventExecutors);
        }
        this.server = serverBuilder
                .addService(svc)
                .build()
                .start();
    }

    @Override
    public int getBindPort() {
        return bindPort;
    }

    @Override
    public String getMethodName() {
        return StreamingGreeterGrpc.getSayHelloStreamingMethod().getFullMethodName();
    }

    public int getRequestCount() {
        return requestCount;
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }


}
