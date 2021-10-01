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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * copy grpc framework
 * - https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/helloworld/HelloWorldClient.java
 *
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldSimpleClient implements HelloWorldClient {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * Construct client connecting to HelloWorld server at {@code host:port}.
     */
    @SuppressWarnings("deprecated")
    public HelloWorldSimpleClient(String host, int port) {
        this(newChannel(host, port));
    }

    private static ManagedChannel newChannel(String host, int port) {
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(host, port);
        BuilderUtils.usePlainText(builder);

        if (builder instanceof NettyChannelBuilder) {
            ExecutorService workerExecutor = Executors.newCachedThreadPool();
            NioEventLoopGroup eventExecutors = new NioEventLoopGroup(CpuUtils.cpuCount() + 5, workerExecutor);
            ((NettyChannelBuilder) builder).eventLoopGroup(eventExecutors);
        }

        builder.intercept(MetadataUtils.newCaptureMetadataInterceptor(new AtomicReference<Metadata>(), new AtomicReference<Metadata>()));
        return builder.build();
    }


    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    HelloWorldSimpleClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    @Override
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response = blockingStub.sayHello(request);

        logger.info("Greeting: {}" + response.getMessage());
        return response.getMessage();
    }

}
