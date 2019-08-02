/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import com.google.common.util.concurrent.MoreExecutors;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.util.DatagramPacketFactory;
import com.navercorp.pinpoint.collector.util.DefaultObjectPool;
import com.navercorp.pinpoint.collector.util.ObjectPool;
import com.navercorp.pinpoint.collector.util.ObjectPoolFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.profiler.context.DefaultSpanChunk;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessorV1;
import com.navercorp.pinpoint.profiler.context.thrift.DefaultTransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.SpanThriftMessageConverter;
import com.navercorp.pinpoint.profiler.sender.SpanStreamUdpSender;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import org.apache.thrift.TBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;

/**
 * @author emeroad
 */
public class SpanStreamUDPSenderTest {

    private String applicationName = "appName";
    private String agentId = "agentId";
    private long agentStartTime = 0;
    private ServiceType applicationServiceType = ServiceType.STAND_ALONE;

    private static MessageHolderDispatchHandler messageHolder;
    private static UDPReceiver receiver = null;

    private static int port;

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 6000);

    private final TransactionIdEncoder transactionIdEncoder = new DefaultTransactionIdEncoder(agentId, agentStartTime);
    private final SpanProcessor<TSpan, TSpanChunk> spanPostProcessor = new SpanProcessorV1();
    private final MessageConverter<TBase<?, ?>> messageConverter
            = new SpanThriftMessageConverter(applicationName, agentId, agentStartTime, applicationServiceType.getCode(),
            transactionIdEncoder, spanPostProcessor);

    @BeforeClass
    public static void setUp() throws IOException {
        port = SocketUtils.findAvailableUdpPort(21111);

        messageHolder = new MessageHolderDispatchHandler();

        Executor executor = MoreExecutors.directExecutor();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", port);
        PacketHandlerFactory<DatagramPacket> packetHandlerFactory = new SpanStreamUDPPacketHandlerFactory<>(messageHolder, new TestTBaseFilter());

        ObjectPoolFactory<DatagramPacket> packetFactory = new DatagramPacketFactory();
        ObjectPool<DatagramPacket> pool = new DefaultObjectPool<>(packetFactory, 10);
        receiver = new UDPReceiver("test", packetHandlerFactory, executor, 1024, inetSocketAddress, pool);
        receiver.start();
    }

    @AfterClass
    public static void tearDown() {
        if (receiver != null) {
            receiver.shutdown();
        }
    }

    private TraceRoot mockTraceRoot() {
        final TraceRoot traceRoot = mock(TraceRoot.class);
        TraceId traceId = mock(TraceId.class);
        Mockito.when(traceRoot.getTraceId()).thenReturn(traceId);

        Shared shared = mock(Shared.class);
        Mockito.when(traceRoot.getShared()).thenReturn(shared);
        return traceRoot;
    }

    @Test
    public void sendTest1() throws InterruptedException {
        SpanStreamUdpSender sender = null;
        try {
            final TraceRoot traceRoot = mockTraceRoot();

            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10, 200, SpanStreamUdpSender.SEND_BUFFER_SIZE, messageConverter);
            sender.send(createSpanChunk(traceRoot, 10));
            sender.send(createSpanChunk(traceRoot, 3));

            awaitMessageReceived(2, messageHolder, TSpanChunk.class);

            List<ServerRequest> tBaseList = messageHolder.getMessageHolder();
            tBaseList.clear();
        } finally {
            if (sender != null) {
                sender.stop();
            }
        }
    }

    @Test
    public void sendTest2() throws InterruptedException {
        SpanStreamUdpSender sender = null;
        try {
            final TraceRoot traceRoot = mockTraceRoot();
            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10, 200, SpanStreamUdpSender.SEND_BUFFER_SIZE, messageConverter);
            sender.send(createSpan(traceRoot, 10));
            sender.send(createSpan(traceRoot, 3));

            awaitMessageReceived(2, messageHolder, TSpan.class);

            List<ServerRequest> tBaseList = messageHolder.getMessageHolder();
            tBaseList.clear();
        } finally {
            if (sender != null) {
                sender.stop();
            }
        }
    }
    
    @Test
    public void sendTest3() throws InterruptedException {
        SpanStreamUdpSender sender = null;
        try {
            final TraceRoot traceRoot = mockTraceRoot();
            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10, 200, SpanStreamUdpSender.SEND_BUFFER_SIZE, messageConverter);
            sender.send(createSpan(traceRoot, 10));
            sender.send(createSpan(traceRoot, 3));
            sender.send(createSpanChunk(traceRoot, 3));

            awaitMessageReceived(2, messageHolder, TSpan.class);
            awaitMessageReceived(1, messageHolder, TSpanChunk.class);

            List<ServerRequest> tBaseList = messageHolder.getMessageHolder();
            tBaseList.clear();
        } finally {
            if (sender != null) {
                sender.stop();
            }
        }
    }

    private Span createSpan(TraceRoot traceRoot, int spanEventSize) throws InterruptedException {

        List<SpanEvent> spanEventList = createSpanEventList(traceRoot, spanEventSize);

        Span span = new Span(traceRoot);
        span.setSpanEventList(spanEventList);
        return span;
    }

    private SpanChunk createSpanChunk(TraceRoot traceRoot, int spanEventSize) throws InterruptedException {

        List<SpanEvent> originalSpanEventList = createSpanEventList(traceRoot, spanEventSize);
        SpanChunk spanChunk = newSpanChunk(traceRoot, originalSpanEventList);
        return spanChunk;
    }


    private SpanChunk newSpanChunk(TraceRoot traceRoot, List<SpanEvent> spanEventList) {
        SpanChunk spanChunk = new DefaultSpanChunk(traceRoot, spanEventList);
        return spanChunk;
    }
    private int getObjectCount(List<ServerRequest> tbaseList, Class clazz) {
        int count = 0;


        for (ServerRequest t : tbaseList) {
            if (clazz.isInstance(t.getData())) {
                count++;
            }
        }
        
        return count;
    }

    private List<SpanEvent> createSpanEventList(TraceRoot traceRoot, int size) throws InterruptedException {

        int elapsedTime = 0;

        List<SpanEvent> spanEventList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            SpanEvent spanEvent = new SpanEvent();
            spanEvent.setStartTime(traceRoot.getTraceStartTime() + elapsedTime++);
            spanEvent.setElapsedTime(elapsedTime++);

            spanEventList.add(spanEvent);
        }

        return spanEventList;
    }

//    private void waitExpectedRequestCount(final AtomicInteger requestCount, final int expectedRequestCount) {
//        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
//            @Override
//            public boolean checkCompleted() {
//                return requestCount.get() == expectedRequestCount;
//            };
//        });
//
//        Assert.assertTrue(pass);
//    }

    private void waitMessageReceived(final int receivedCount, final int awaitReceiveCount) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return receivedCount == awaitReceiveCount;
            }
        });

        Assert.assertTrue(pass);
    }

    private void awaitMessageReceived(final int receivedCount, final MessageHolderDispatchHandler dispatchHandler, final Class clazz) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                List<ServerRequest> messageHolder = dispatchHandler.getMessageHolder();
                int objectCount = getObjectCount(messageHolder, clazz);
                return receivedCount == objectCount;
            }
        });

        Assert.assertTrue(pass);
    }

    static class TestTBaseFilter<T> implements TBaseFilter<T> {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public boolean filter(DatagramSocket localSocket, TBase<?, ?> tBase, T remoteHostAddress) {
            logger.debug("filter");
            return false;
        }

    }

    static class MessageHolderDispatchHandler implements DispatchHandler {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private List<ServerRequest> messageHolder = new ArrayList<>();


        @Override
        public void dispatchSendMessage(ServerRequest serverRequest) {
            logger.debug("dispatchSendMessage");
        }

        @Override
        public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
            messageHolder.add(serverRequest);
            TResult tResult = new TResult(true);
            serverResponse.write(tResult);
        }

        public List<ServerRequest> getMessageHolder() {
            return messageHolder;
        }

    }

}