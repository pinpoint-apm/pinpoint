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
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.ClientOption;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.ServerFactory;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.grpc.server.TransportMetadataFactory;
import com.navercorp.pinpoint.grpc.server.TransportMetadataServerInterceptor;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.NameResolverProvider;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerTransportFilter;
import io.grpc.Status;
import io.grpc.internal.PinpointDnsNameResolverProvider;
import io.grpc.stub.StreamObserver;
import org.junit.AfterClass;
import org.junit.Assert;
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
import java.util.concurrent.atomic.AtomicInteger;


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

        HeaderFactory headerFactory = new AgentHeaderFactory("agentId", "appName", System.currentTimeMillis());

        CountRecordClientInterceptor countRecordClientInterceptor = new CountRecordClientInterceptor();

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder(this.getClass().getSimpleName());
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setNameResolverProvider(nameResolverProvider);
        channelFactoryBuilder.addClientInterceptor(countRecordClientInterceptor);
        channelFactoryBuilder.setClientOption(new ClientOption.Builder().build());
        ChannelFactory channelFactory = channelFactoryBuilder.build();

        ManagedChannel managedChannel = channelFactory.build("127.0.0.1", PORT);
        managedChannel.getState(false);

        SpanGrpc.SpanStub spanStub = SpanGrpc.newStub(managedChannel);

        final QueueingStreamObserver<Empty> responseObserver = new QueueingStreamObserver<Empty>();

        logger.debug("sendSpan");
        StreamObserver<PSpanMessage> sendSpan = spanStub.sendSpan(responseObserver);

        PSpan pSpan = newSpan();
        PSpanMessage message = PSpanMessage.newBuilder().setSpan(pSpan).build();

        logger.debug("client-onNext");
        sendSpan.onNext(message);
        logger.debug("wait for response");
        Empty value = responseObserver.getValue();
        logger.debug("response:{}", value);

        logger.debug("client-onCompleted");
        sendSpan.onCompleted();

        Assert.assertTrue(countRecordClientInterceptor.getExecutedInterceptCallCount() == 1);

        logger.debug("state:{}", managedChannel.getState(true));
        spanService.awaitOnCompleted();
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

        serverFactory = new ServerFactory(ChannelFactoryTest.class.getSimpleName() + "-server", "127.0.0.1", PORT, executorService, new ServerOption.Builder().build());
        spanService = new SpanService();

        serverFactory.addService(spanService);

        addFilter(serverFactory);
        Server server = serverFactory.build();
        return server;
    }

    private static void addFilter(ServerFactory serverFactory) {
        TransportMetadataFactory transportMetadataFactory = new TransportMetadataFactory(ChannelFactoryTest.class.getSimpleName());
        final ServerTransportFilter metadataTransportFilter = new MetadataServerTransportFilter(transportMetadataFactory);
        serverFactory.addTransportFilter(metadataTransportFilter);

        ServerInterceptor transportMetadataServerInterceptor = new TransportMetadataServerInterceptor();
        serverFactory.addInterceptor(transportMetadataServerInterceptor);

    }

    static class SpanService extends SpanGrpc.SpanImplBase {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final CountDownLatch onCompletedLatch;

        public SpanService() {
            this.onCompletedLatch = new CountDownLatch(1);
        }

        @Override
        public StreamObserver<PSpanMessage> sendSpan(final StreamObserver<Empty> responseObserver) {
            return new StreamObserver<PSpanMessage>() {
                @Override
                public void onNext(PSpanMessage value) {
                    Header header = ServerContext.getAgentInfo();

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
                    onCompletedLatch.countDown();
                }
            };
        }

        public boolean awaitOnCompleted() {
            try {
                return onCompletedLatch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    static class CountRecordClientInterceptor implements ClientInterceptor {

        private final AtomicInteger executedInterceptCallCount = new AtomicInteger();

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
            executedInterceptCallCount.incrementAndGet();
            return next.newCall(method, callOptions);
        }

        public int getExecutedInterceptCallCount() {
            return executedInterceptCallCount.get();
        }
    }
}