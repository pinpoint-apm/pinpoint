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

import com.navercorp.pinpoint.pluginit.utils.SocketUtils;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * copy grpc framework
 * - https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/helloworld/HelloWorldServer.java
 *
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class HelloWorldSimpleServer implements HelloWorldServer {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Server server;

    private int bindPort;

    @PostConstruct
    public void start() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort(27675);

        /* The port on which the server should run */
        server = ServerBuilder.forPort(bindPort)
                .addService(new GreeterImpl())
                .build()
                .start();

        logger.info("Server started, listening on " + bindPort);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                HelloWorldSimpleServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    public int getBindPort() {
        return bindPort;
    }

    @Override
    public String getMethodName() {
        return GreeterGrpc.getSayHelloMethod().getFullMethodName();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage(req.getName().toUpperCase()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

}
