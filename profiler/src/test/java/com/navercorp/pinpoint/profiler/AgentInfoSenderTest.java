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

package com.navercorp.pinpoint.profiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import org.apache.thrift.TException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.context.DefaultServerMetaData;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataHolder;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;

public class AgentInfoSenderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = 10050;
    public static final String HOST = "127.0.0.1";

    @Test
    public void agentInfoShouldBeSent() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 1000L;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender sender = new TcpDataSender(socket);
        AgentInfoSender agentInfoSender = new AgentInfoSender(sender, agentInfoSendRetryIntervalMs, getAgentInfo());

        try {
            agentInfoSender.start();
            Thread.sleep(10000L);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, socket, socketFactory);
        }
        assertEquals(1, requestCount.get());
        assertEquals(1, successCount.get());
    }

    @Test
    public void agentInfoShouldRetryUntilSuccess() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 1000L;
        final int expectedTriesUntilSuccess = 5;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount, expectedTriesUntilSuccess);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender dataSender = new TcpDataSender(socket);
        AgentInfoSender agentInfoSender = new AgentInfoSender(dataSender, agentInfoSendRetryIntervalMs, getAgentInfo());

        try {
            agentInfoSender.start();
            Thread.sleep(agentInfoSendRetryIntervalMs * expectedTriesUntilSuccess);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, socket, socketFactory);
        }
        assertEquals(expectedTriesUntilSuccess, requestCount.get());
        assertEquals(1, successCount.get());
    }

    @Test
    public void agentInfoShouldBeSentOnlyOnceEvenAfterReconnect() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 1000L;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender dataSender = new TcpDataSender(socket);
        AgentInfoSender agentInfoSender = new AgentInfoSender(dataSender, agentInfoSendRetryIntervalMs, getAgentInfo());

        try {
            agentInfoSender.start();
            createAndDeleteServer(serverListener, 5000L);
            Thread.sleep(1000L);
            createAndDeleteServer(serverListener, 5000L);
            Thread.sleep(1000L);
            createAndDeleteServer(serverListener, 5000L);
        } finally {
            closeAll(null, agentInfoSender, socket, socketFactory);
        }
        assertEquals(1, requestCount.get());
        assertEquals(1, successCount.get());
    }

    @Test
    public void agentInfoShouldKeepRetrying() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 1000L;
        final long minimumAgentInfoSendRetryCount = 10;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount, Integer.MAX_VALUE);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender dataSender = new TcpDataSender(socket);
        AgentInfoSender agentInfoSender = new AgentInfoSender(dataSender, agentInfoSendRetryIntervalMs, getAgentInfo());

        try {
            agentInfoSender.start();
            Thread.sleep(agentInfoSendRetryIntervalMs * minimumAgentInfoSendRetryCount);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, socket, socketFactory);
        }
        assertTrue(requestCount.get() >= minimumAgentInfoSendRetryCount);
        assertEquals(0, successCount.get());
    }
    
    @Test
    public void serverMetaDataShouldBeSentOnPublish() throws InterruptedException {
        // Given
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 1000L;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender sender = new TcpDataSender(socket);
        AgentInfoSender agentInfoSender = new AgentInfoSender(sender, agentInfoSendRetryIntervalMs, getAgentInfo());
        final List<ServerMetaData> serverMetaDataObjects = new ArrayList<ServerMetaData>();
        serverMetaDataObjects.add(new DefaultServerMetaData("server1", Collections.<String>emptyList(), Collections.<Integer, String>emptyMap(), Collections.<ServiceInfo>emptyList()));
        serverMetaDataObjects.add(new DefaultServerMetaData("server2", Collections.<String>emptyList(), Collections.<Integer, String>emptyMap(), Collections.<ServiceInfo>emptyList()));
        serverMetaDataObjects.add(new DefaultServerMetaData("server3", Collections.<String>emptyList(), Collections.<Integer, String>emptyMap(), Collections.<ServiceInfo>emptyList()));
        serverMetaDataObjects.add(new DefaultServerMetaData("server4", Collections.<String>emptyList(), Collections.<Integer, String>emptyMap(), Collections.<ServiceInfo>emptyList()));
        serverMetaDataObjects.add(new DefaultServerMetaData("server5", Collections.<String>emptyList(), Collections.<Integer, String>emptyMap(), Collections.<ServiceInfo>emptyList()));
        // When
        try {
            for (ServerMetaData serverMetaData : serverMetaDataObjects) {
                agentInfoSender.publishServerMetaData(serverMetaData);
            }
            Thread.sleep(10000L);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, socket, socketFactory);
        }
        // Then
        assertEquals(5, requestCount.get());
        assertEquals(5, successCount.get());
    }
    
    @Test
    public void serverMetaDataCouldBePublishedFromMultipleThreads() throws InterruptedException {
        // Given
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 1000L;
        final int threadCount = 50;
        final CountDownLatch initLatch = new CountDownLatch(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final Queue<Throwable> exceptions = new ConcurrentLinkedQueue<Throwable>();

        ResponseServerMessageListener delayedServerListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(delayedServerListener);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender sender = new TcpDataSender(socket);
        AgentInfoSender agentInfoSender = new AgentInfoSender(sender, agentInfoSendRetryIntervalMs, getAgentInfo());
        final ServerMetaDataHolder metaDataContext = new DefaultServerMetaDataHolder(Collections.<String>emptyList());
        metaDataContext.addListener(agentInfoSender);
        // When
        for (int i = 0; i < threadCount; ++i) {
            executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    initLatch.countDown();
                    try {
                        startLatch.await();
                        metaDataContext.publishServerMetaData();
                    } catch (final Throwable t) {
                        exceptions.add(t);
                    } finally {
                        endLatch.countDown();
                    }
                    return null;
                }
            });
        }
        initLatch.await();
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();
        try {
            Thread.sleep(10000L);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, socket, socketFactory);
        }
        // Then
        assertTrue("Failed with exceptions : " + exceptions, exceptions.isEmpty());
        assertEquals(threadCount, requestCount.get());
        assertEquals(threadCount, successCount.get());
    }

    public void reconnectionStressTest() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long stressTestTime = 60 * 1000L;
        final int randomMaxTime = 3000;
        final long agentInfoSendRetryIntervalMs = 1000L;
        final int expectedTriesUntilSuccess = (int)stressTestTime / (randomMaxTime * 2);

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount, expectedTriesUntilSuccess);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender dataSender = new TcpDataSender(socket);
        AgentInfoSender agentInfoSender = new AgentInfoSender(dataSender, agentInfoSendRetryIntervalMs, getAgentInfo());

        long startTime = System.currentTimeMillis();

        try {
            agentInfoSender.start();

            Random random = new Random(System.currentTimeMillis());

            while (System.currentTimeMillis() < startTime + stressTestTime) {
                createAndDeleteServer(serverListener, Math.abs(random.nextInt(randomMaxTime)));
                Thread.sleep(Math.abs(random.nextInt(1000)));
            }

        } finally {
            closeAll(null, agentInfoSender, socket, socketFactory);
        }
        assertEquals(1, successCount.get());
        assertEquals(expectedTriesUntilSuccess, requestCount.get());
    }

    private PinpointServerAcceptor createServerAcceptor(ServerMessageListener listener) {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        // server.setMessageListener(new
        // NoResponseServerMessageListener(requestCount));
        serverAcceptor.setMessageListener(listener);
        serverAcceptor.bind(HOST, PORT);

        return serverAcceptor;
    }

    private void createAndDeleteServer(ServerMessageListener listener, long waitTimeMillis) throws InterruptedException {
        PinpointServerAcceptor server = null;
        try {
            server = createServerAcceptor(listener);
            Thread.sleep(waitTimeMillis);
        } finally {
            if (server != null) {
                server.close();
            }
        }
    }

    private void closeAll(PinpointServerAcceptor serverAcceptor, AgentInfoSender agentInfoSender, PinpointSocket socket, PinpointSocketFactory factory) {
        if (serverAcceptor != null) {
            serverAcceptor.close();
        }

        if (agentInfoSender != null) {
            agentInfoSender.stop();
        }

        if (socket != null) {
            socket.close();
        }

        if (factory != null) {
            factory.release();
        }
    }

    private AgentInformation getAgentInfo() {
        AgentInformation agentInfo = new AgentInformation("agentId", "appName", System.currentTimeMillis(), 1111, "hostname", "127.0.0.1", ServiceType.USER, "1");
        return agentInfo;
    }

    class ResponseServerMessageListener implements ServerMessageListener {
        private final AtomicInteger requestCount;
        private final AtomicInteger successCount;

        private final int successCondition;

        public ResponseServerMessageListener(AtomicInteger requestCount, AtomicInteger successCount) {
            this(requestCount, successCount, 1);
        }

        public ResponseServerMessageListener(AtomicInteger requestCount, AtomicInteger successCount, int successCondition) {
            this.requestCount = requestCount;
            this.successCount = successCount;
            this.successCondition = successCondition;
        }

        @Override
        public void handleSend(SendPacket sendPacket, PinpointServer pinpointServer) {
            logger.info("handleSend:{}", sendPacket);

        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointServer pinpointServer) {
            int requestCount = this.requestCount.incrementAndGet();

            if (requestCount < successCondition) {
                return;
            }

            logger.info("handleRequest~~~:{}", requestPacket);

            try {
                HeaderTBaseSerializer serializer = HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer();

                TResult result = new TResult(true);
                byte[] resultBytes = serializer.serialize(result);

                this.successCount.incrementAndGet();

                pinpointServer.response(requestPacket, resultBytes);
            } catch (TException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public HandshakeResponseCode handleHandshake(@SuppressWarnings("rawtypes") Map arg0) {
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPacket pingPacket, PinpointServer pinpointServer) {
            logger.info("ping received {} {} ", pingPacket, pinpointServer);
        }
    }
    
    private PinpointSocketFactory createPinpointSocketFactory() {
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setTimeoutMillis(1000 * 5);
        pinpointSocketFactory.setProperties(Collections.<String, Object>emptyMap());

        return pinpointSocketFactory;
    }

    
    private PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory) {
        PinpointSocket socket = null;
        for (int i = 0; i < 3; i++) {
            try {
                socket = factory.connect(host, port);
                logger.info("tcp connect success:{}/{}", host, port);
                return socket;
            } catch (PinpointSocketException e) {
                logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
        logger.warn("change background tcp connect mode  {}/{} ", host, port);
        socket = factory.scheduledConnect(host, port);
        
        return socket;
    }

}
