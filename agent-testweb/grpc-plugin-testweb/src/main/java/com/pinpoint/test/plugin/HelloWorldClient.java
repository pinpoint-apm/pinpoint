/*
 * Copyright 2018 NAVER Corp.
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

package com.pinpoint.test.plugin;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.MetadataUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
public class HelloWorldClient {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /** Construct client connecting to HelloWorld server at {@code host:port}. */
    public HelloWorldClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .intercept(MetadataUtils.newCaptureMetadataInterceptor(new AtomicReference<>(), new AtomicReference<>()))
                .build());
    }

    /** Construct client for accessing HelloWorld server using the existing channel. */
    HelloWorldClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String greet(String name) {
        return greet(name, 1);
    }

        /** Say hello to server. */
    public String greet(String name, int count) {
        Assert.isTrue(count > 0, "count must be greater than 0");

        logger.info("Will try to greet {}. count:{}", name, count);

        HelloReply lastReply = null;
        for (int i = 0; i < count; i++) {
            try {
                HelloRequest request = HelloRequest.newBuilder().setName(name + " " + i + "th").build();
                lastReply = blockingStub.sayHello(request);
            } catch (Exception e) {
                logger.warn("failed to send request(index:{}). message:{}", i, e.getMessage(), e);
            }
        }

        logger.info("Greeting: {}" + lastReply.getMessage());
        return lastReply.getMessage();
    }

}
