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

package com.navercorp.pinpoint.grpc;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.ServerFactory;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import io.grpc.ManagedChannel;
import io.grpc.NameResolverProvider;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.internal.PinpointDnsNameResolverProvider;
import io.grpc.stub.StreamObserver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ChannelFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(ChannelFactoryTest.class);

    public static final int PORT = 30211;

    private static ServerFactory serverFactory;
    private static Server server;
    private static SpanService spanService;
    private static ExecutorService executorService;

    private static ExecutorService dnsExecutorService;
    private static NameResolverProvider nameResolverProvider;

    @BeforeClass
    public static void setUp() throws Exception {
        dnsExecutorService = newCachedExecutorService("dnsExecutor");
        nameResolverProvider = new PinpointDnsNameResolverProvider("dnsExecutor", dnsExecutorService);

        executorService = Executors.newCachedThreadPool(PinpointThreadFactory.createThreadFactory("test-executor"));
        server = serverStart(executorService);
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (server != null ) {
            server.shutdownNow();
            server.awaitTermination();
            serverFactory.close();
        }
        ExecutorUtils.shutdownExecutorService("test-executor", executorService);

        ExecutorUtils.shutdownExecutorService("dnsExecutor", dnsExecutorService);
    }

    @Test
    public void build() throws InterruptedException {

        AgentHeaderFactory.Header header = new AgentHeaderFactory.Header("agentId", "appName", System.currentTimeMillis());
        HeaderFactory<AgentHeaderFactory.Header> headerFactory = new AgentHeaderFactory(header);
        ChannelFactory channelFactory = new ChannelFactory(this.getClass().getSimpleName(), headerFactory, nameResolverProvider);
        ManagedChannel managedChannel = channelFactory.build("test-channel", "127.0.0.1", PORT);
        managedChannel.getState(false);

        SpanGrpc.SpanStub spanStub = SpanGrpc.newStub(managedChannel);
//        traceStub.withExecutor()

        final CountdownStreamObserver responseObserver = new CountdownStreamObserver();

        logger.debug("sendSpan");
        StreamObserver<PSpan> sendSpan = spanStub.sendSpan(responseObserver);

        PSpan pSpan = newSpan();
        logger.debug("client-onNext");
        sendSpan.onNext(pSpan);
        logger.debug("wait for response");
        responseObserver.awaitLatch();
        logger.debug("client-onCompleted");
        sendSpan.onCompleted();

        logger.debug("state:{}", managedChannel.getState(true));
        spanService.awaitLatch();
        logger.debug("managedChannel shutdown");
        managedChannel.shutdown();
        managedChannel.awaitTermination(1000, TimeUnit.MILLISECONDS);

        channelFactory.close();

    }

    private static ExecutorService newCachedExecutorService(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(name, true);
        return Executors.newCachedThreadPool(threadFactory);
    }

    private PSpan newSpan() {
        PSpan.Builder builder = PSpan.newBuilder();
        builder.setApiId(10);
        return builder.build();
    }


    private static Server serverStart(ExecutorService executorService) throws IOException {
        logger.debug("server start");

        serverFactory = new ServerFactory(ChannelFactoryTest.class.getSimpleName() + "-server", "127.0.0.1", PORT, executorService);
        spanService = new SpanService(1);
        serverFactory.addService(spanService);
        Server server = serverFactory.build();
        return server;
    }

    static class SpanService extends SpanGrpc.SpanImplBase {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final CountDownLatch latch;

        public SpanService(int count) {
            this.latch = new CountDownLatch(count);
        }

        @Override
        public StreamObserver<PSpan> sendSpan(final StreamObserver<Empty> responseObserver) {
            return new StreamObserver<PSpan>() {
                @Override
                public void onNext(PSpan value) {
                    AgentHeaderFactory.Header header = ServerContext.getAgentInfo();
                    logger.debug("server-onNext:{} header:{}" , value, header);
                    logger.debug("server-threadName:{}", Thread.currentThread().getName());

                    logger.debug("server-onNext: send Empty" );
                    Empty.Builder builder = Empty.newBuilder();
                    responseObserver.onNext(builder.build());
                }

                @Override
                public void onError(Throwable t) {
                    logger.debug("server-onError:{} status:{}", t.getMessage(), Status.fromThrowable(t), t);
                }

                @Override
                public void onCompleted() {
                    logger.debug("server-onCompleted");
                    responseObserver.onCompleted();
                    latch.countDown();
                }
            };
        }

        public boolean awaitLatch() {
            try {
                return latch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}