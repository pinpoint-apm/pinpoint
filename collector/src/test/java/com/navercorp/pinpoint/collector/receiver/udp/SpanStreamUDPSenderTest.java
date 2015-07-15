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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.navercorp.pinpoint.collector.receiver.AbstractDispatchHandler;
import com.navercorp.pinpoint.collector.receiver.DataReceiver;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.sender.SpanStreamUdpSender;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

/**
 * @author emeroad
 */
public class SpanStreamUDPSenderTest {

    private static MessageHolderDispatchHandler messageHolder;
    private static DataReceiver receiver = null;

    private static int port;

    @BeforeClass
    public static void setUp() throws IOException {
        port = getAvaiableUDPPort(21111);

        try {
            messageHolder = new MessageHolderDispatchHandler();
            receiver = new TestUDPReceiver("test", new SpanStreamUDPPacketHandlerFactory<DatagramPacket>(messageHolder, new TestTBaseFilter()), "127.0.0.1",
                    port, 1024, 1, 10);
            receiver.start();
        } catch (Exception e) {
        }
    }

    @AfterClass
    public static void tearDown() {
        if (receiver != null) {
            receiver.shutdown();
        }
    }

    private static int getAvaiableUDPPort(int defaultPort) throws IOException {
        int bindPort = defaultPort;

        DatagramSocket dagagramSocket = null;
        while (0xFFFF >= bindPort && dagagramSocket == null) {
            try {
                dagagramSocket = new DatagramSocket(bindPort);
            } catch (IOException ex) {
                bindPort++;
            }
        }

        if (dagagramSocket != null) {
            dagagramSocket.close();
            return bindPort;
        }

        throw new IOException("can't find available port.");
    }

    @Test
    public void sendTest1() throws InterruptedException {
        SpanStreamUdpSender sender = null;
        try {
            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10);
            sender.send(createSpanChunk(10));
            sender.send(createSpanChunk(3));

            Thread.sleep(6000);

            List<TBase> tBaseList = messageHolder.getMessageHolder();
            int spanChunkCount = getObjectCount(tBaseList, TSpanChunk.class);
            Assert.assertEquals(2, spanChunkCount);
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
            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10);
            sender.send(createSpan(10));
            sender.send(createSpan(3));

            Thread.sleep(6000);

            List<TBase> tBaseList = messageHolder.getMessageHolder();
            int spanCount = getObjectCount(tBaseList, TSpan.class);
            Assert.assertEquals(2, spanCount);
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
            sender = new SpanStreamUdpSender("127.0.0.1", port, "threadName", 10);
            sender.send(createSpan(10));
            sender.send(createSpan(3));
            sender.send(createSpanChunk(3));

            Thread.sleep(6000);

            List<TBase> tBaseList = messageHolder.getMessageHolder();
            int spanCount = getObjectCount(tBaseList, TSpan.class);
            int spanChunkCount = getObjectCount(tBaseList, TSpanChunk.class);
            
            Assert.assertEquals(2, spanCount);
            Assert.assertEquals(1, spanChunkCount);
            
            tBaseList.clear();
        } finally {
            if (sender != null) {
                sender.stop();
            }
        }
    }

    private Span createSpan(int spanEventSize) throws InterruptedException {
        AgentInformation agentInformation = new AgentInformation("agentId", "applicationName", 0, 0, "machineName", "127.0.0.1", ServiceType.STAND_ALONE,
                Version.VERSION);
        SpanChunkFactory spanChunkFactory = new SpanChunkFactory(agentInformation);

        List<SpanEvent> spanEventList = createSpanEventList(spanEventSize);
        Span span = new Span();

        List<TSpanEvent> tSpanEventList = new ArrayList<TSpanEvent>();
        for (SpanEvent spanEvent : spanEventList) {
            tSpanEventList.add(spanEvent);
        }
        span.setSpanEventList(tSpanEventList);
        return span;
    }

    private SpanChunk createSpanChunk(int spanEventSize) throws InterruptedException {
        AgentInformation agentInformation = new AgentInformation("agentId", "applicationName", 0, 0, "machineName", "127.0.0.1", ServiceType.STAND_ALONE,
                Version.VERSION);
        SpanChunkFactory spanChunkFactory = new SpanChunkFactory(agentInformation);

        List<SpanEvent> originalSpanEventList = createSpanEventList(spanEventSize);
        SpanChunk spanChunk = spanChunkFactory.create(originalSpanEventList);
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

    private List<SpanEvent> createSpanEventList(int size) throws InterruptedException {
        // Span span = new SpanBo(new TSpan());
        Span span = new Span();

        List<SpanEvent> spanEventList = new ArrayList<SpanEvent>(size);
        for (int i = 0; i < size; i++) {
            SpanEvent spanEvent = new SpanEvent(span);
            spanEvent.markStartTime();
            Thread.sleep(1);
            spanEvent.markAfterTime();

            spanEventList.add(spanEvent);
        }

        return spanEventList;
    }

    static class TestTBaseFilter<T> implements TBaseFilter<T> {

        @Override
        public boolean filter(TBase<?, ?> tBase, T remoteHostAddress) {
            System.out.println("filter");
            return false;
        }

    }

    static class MessageHolderDispatchHandler extends AbstractDispatchHandler {

        private List<TBase> messageHolder = new ArrayList<TBase>();

        @Override
        public void dispatchSendMessage(TBase<?, ?> tBase) {
            System.out.println("dispatchSendMessage");
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