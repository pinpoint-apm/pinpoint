/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.udp;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.collector.TestAwaitTaskUtils;
import com.navercorp.pinpoint.collector.TestAwaitUtils;
import com.navercorp.pinpoint.collector.receiver.AbstractDispatchHandler;
import com.navercorp.pinpoint.collector.receiver.DataReceiver;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactoryV1;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.sender.SpanStreamUdpSender;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
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
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author emeroad
 */
public class SpanStreamUDPSenderTest {

    private static MessageHolderDispatchHandler messageHolder;
    private static DataReceiver receiver = null;

    private static int port;

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 6000);

    @BeforeClass
    public static void setUp() throws IOException {
        port = SocketUtils.findAvailableUdpPort(21111);

        try {
            messageHolder = new MessageHolderDispatchHandler();
            receiver = new TestUDPReceiver("test", new SpanStreamUDPPacketHandlerFactory<>(messageHolder, new TestTBaseFilter()), "127.0.0.1",
                    port, 1024, 1, 10);
            receiver.start();
        } catch (Exception ignored) {
        }
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

            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10, 200, SpanStreamUdpSender.SEND_BUFFER_SIZE);
            sender.send(createSpanChunk(traceRoot, 10));
            sender.send(createSpanChunk(traceRoot, 3));

            awaitMessageReceived(2, messageHolder, TSpanChunk.class);

            List<TBase> tBaseList = messageHolder.getMessageHolder();
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
            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10, 200, SpanStreamUdpSender.SEND_BUFFER_SIZE);
            sender.send(createSpan(traceRoot, 10));
            sender.send(createSpan(traceRoot, 3));

            awaitMessageReceived(2, messageHolder, TSpan.class);

            List<TBase> tBaseList = messageHolder.getMessageHolder();
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
            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10, 200, SpanStreamUdpSender.SEND_BUFFER_SIZE);
            sender.send(createSpan(traceRoot, 10));
            sender.send(createSpan(traceRoot, 3));
            sender.send(createSpanChunk(traceRoot, 3));

            awaitMessageReceived(2, messageHolder, TSpan.class);
            awaitMessageReceived(1, messageHolder, TSpanChunk.class);

            List<TBase> tBaseList = messageHolder.getMessageHolder();
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
        span.setSpanEventList((List)spanEventList);
        return span;
    }

    private SpanChunk createSpanChunk(TraceRoot traceRoot, int spanEventSize) throws InterruptedException {
        final String agentId = "agentId";
        final long agentStartTime = System.currentTimeMillis();
        final TransactionIdEncoder transactionIdEncoder = new DefaultTransactionIdEncoder(agentId, agentStartTime);
        SpanChunkFactory spanChunkFactory = new SpanChunkFactoryV1("applicationName", agentId, agentStartTime, ServiceType.STAND_ALONE, transactionIdEncoder);

        List<SpanEvent> originalSpanEventList = createSpanEventList(traceRoot, spanEventSize);
        SpanChunk spanChunk = spanChunkFactory.create(traceRoot, originalSpanEventList);
        return spanChunk;
    }
    
    private int getObjectCount(List<TBase> tbaseList, Class clazz) {
        int count = 0;
        
        for (TBase t : tbaseList) {
            if (clazz.isInstance(t)) {
                count++;
            }
        }
        
        return count;
    }

    private List<SpanEvent> createSpanEventList(TraceRoot traceRoot, int size) throws InterruptedException {

        List<SpanEvent> spanEventList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            SpanEvent spanEvent = new SpanEvent(traceRoot);
            spanEvent.markStartTime();
            Thread.sleep(1);
            spanEvent.markAfterTime();

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
                List<TBase> messageHolder = dispatchHandler.getMessageHolder();
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

    static class MessageHolderDispatchHandler extends AbstractDispatchHandler {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private List<TBase> messageHolder = new ArrayList<>();

        @Override
        public void dispatchSendMessage(TBase<?, ?> tBase) {
            logger.debug("dispatchSendMessage");
        }

        @Override
        public TBase dispatchRequestMessage(TBase<?, ?> tBase) {
            messageHolder.add(tBase);
            return new TResult(true);
        }

        public List<TBase> getMessageHolder() {
            return messageHolder;
        }

    }

}