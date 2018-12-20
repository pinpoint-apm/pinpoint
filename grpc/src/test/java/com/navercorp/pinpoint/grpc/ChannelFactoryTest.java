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
import com.navercorp.pinpoint.gpc.trace.PSpan;
import com.navercorp.pinpoint.gpc.trace.TraceGrpc;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.server.AgentInfoContext;
import com.navercorp.pinpoint.grpc.server.ServerFactory;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ChannelFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(ChannelFactoryTest.class);

    public static final int PORT = 30211;

    private static Server server;
    private static TraceService traceService;

    @BeforeClass
    public static void setUp() throws Exception {
        server = serverStart();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (server != null ) {
            server.shutdownNow();
            server.awaitTermination();
        }
    }

    @Test
    public void build() throws InterruptedException, IOException {
        AgentHeaderFactory.Header header = new AgentHeaderFactory.Header("agentId", "appName", System.currentTimeMillis());
        HeaderFactory<AgentHeaderFactory.Header> headerFactory = new AgentHeaderFactory(header);
        ChannelFactory channelFactory = new ChannelFactory(this.getClass().getName(), "127.0.0.1", PORT, headerFactory);
        ManagedChannel managedChannel = channelFactory.build();
        managedChannel.getState(false);

        TraceGrpc.TraceStub traceStub = TraceGrpc.newStub(managedChannel);

        StreamObserver<PSpan> sendSpan = traceStub.sendSpan(new CountdownStreamObserver());

        PSpan pSpan = newSpan();
        sendSpan.onNext(pSpan);

        logger.debug("state:{}", managedChannel.getState(true));
        Assert.assertTrue(traceService.awaitLatch());

        managedChannel.shutdown();
    }

    private PSpan newSpan() {
        PSpan.Builder builder = PSpan.newBuilder();
        builder.setApiId(10);
        return builder.build();
    }


    private static Server serverStart() throws IOException {
        logger.debug("server start");

        ServerFactory serverFactory = new ServerFactory(PORT);
        traceService = new TraceService(1);
        serverFactory.addService(traceService);
        Server server = serverFactory.build();
        server.start();
        return server;
    }

    static class TraceService extends TraceGrpc.TraceImplBase {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final CountDownLatch latch;

        public TraceService(int count) {
            this.latch = new CountDownLatch(count);
        }

        @Override
        public StreamObserver<PSpan> sendSpan(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<PSpan>() {
                @Override
                public void onNext(PSpan value) {
                    final Context context = Context.current();
                    AgentHeaderFactory.Header header = AgentInfoContext.agentInfoKey.get(context);
                    logger.debug("onNext:{} header:{}" , value, header);
                    latch.countDown();
                }

                @Override
                public void onError(Throwable t) {
                    logger.debug("onError:{}", t.getMessage(), t);
                }

                @Override
                public void onCompleted() {
                    logger.debug("onCompleted");
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