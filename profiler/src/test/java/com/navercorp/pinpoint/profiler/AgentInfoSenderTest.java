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

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaData;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataHolder;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;
import com.navercorp.pinpoint.rpc.packet.*;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;

import org.apache.thrift.TException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AgentInfoSenderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = 10050;
    public static final String HOST = "127.0.0.1";

    @Test
    public void agentInfoShouldBeSent() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 100L;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, clientFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo()).sendInterval(agentInfoSendRetryIntervalMs).build();

        try {
            agentInfoSender.start();
            Thread.sleep(1000L);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, pinpointClient, clientFactory);
        }
        assertEquals(1, requestCount.get());
        assertEquals(1, successCount.get());
    }

    @Test
    public void agentInfoShouldRetryUntilSuccess() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 100L;
        final int expectedTriesUntilSuccess = AgentInfoSender.DEFAULT_MAX_TRY_COUNT_PER_ATTEMPT;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount, expectedTriesUntilSuccess);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointClientFactory socketFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, socketFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo()).sendInterval(agentInfoSendRetryIntervalMs).build();

        try {
            agentInfoSender.start();
            Thread.sleep(agentInfoSendRetryIntervalMs * expectedTriesUntilSuccess);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, pinpointClient, socketFactory);
        }
        assertEquals(expectedTriesUntilSuccess, requestCount.get());
        assertEquals(1, successCount.get());
    }

    @Test
    public void agentInfoShouldInitiallyRetryIndefinitely() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 100L;
        final int expectedTriesUntilSuccess = 15;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount, expectedTriesUntilSuccess);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointClientFactory socketFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, socketFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo()).sendInterval(agentInfoSendRetryIntervalMs).build();

        try {
            agentInfoSender.start();
            Thread.sleep(agentInfoSendRetryIntervalMs * expectedTriesUntilSuccess);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, pinpointClient, socketFactory);
        }
        assertEquals(expectedTriesUntilSuccess, requestCount.get());
        assertEquals(1, successCount.get());
    }
    
    @Test
    public void agentInfoShouldRetryUntilAttemptsAreExhaustedWhenRefreshing() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 100L;
        final long agentInfoSendRefreshIntervalMs = 1000L;
        final int expectedTries = AgentInfoSender.DEFAULT_MAX_TRY_COUNT_PER_ATTEMPT + 1;
        final CountDownLatch agentReconnectLatch = new CountDownLatch(1);

        ResponseServerMessageListener successServerListener = new ResponseServerMessageListener(requestCount, successCount);
        ResponseServerMessageListener failServerListener = new ResponseServerMessageListener(requestCount, successCount, Integer.MAX_VALUE);

        PinpointServerAcceptor successServerAcceptor = createServerAcceptor(successServerListener);
        PinpointServerAcceptor failServerAcceptor = null;

        PinpointClientFactory socketFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, socketFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        dataSender.addReconnectEventListener(new PinpointClientReconnectEventListener() {
            @Override
            public void reconnectPerformed(PinpointClient client) {
                agentReconnectLatch.countDown();
            }
        });
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo())
                .refreshInterval(agentInfoSendRefreshIntervalMs)
                .sendInterval(agentInfoSendRetryIntervalMs)
                .build();
        try {
            agentInfoSender.start();
            Thread.sleep(agentInfoSendRetryIntervalMs);
            successServerAcceptor.close();
            // wait till agent reconnects
            failServerAcceptor = createServerAcceptor(failServerListener);
            agentReconnectLatch.await();
            Thread.sleep(agentInfoSendRefreshIntervalMs);
        } finally {
            closeAll(failServerAcceptor, agentInfoSender, pinpointClient, socketFactory);
        }
        assertEquals(1, successCount.get());
        assertEquals(expectedTries, requestCount.get());
    }

    @Test
    public void agentInfoShouldBeSentOnlyOnceEvenAfterReconnect() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 100L;
        final int maxTryCountPerAttempt = Integer.MAX_VALUE;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, clientFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo())
                .sendInterval(agentInfoSendRetryIntervalMs)
                .maxTryPerAttempt(maxTryCountPerAttempt)
                .build();

        try {
            agentInfoSender.start();
            createAndDeleteServer(serverListener, 1000L);
            Thread.sleep(500L);
            createAndDeleteServer(serverListener, 1000L);
            Thread.sleep(500L);
            createAndDeleteServer(serverListener, 1000L);
        } finally {
            closeAll(null, agentInfoSender, pinpointClient, clientFactory);
        }
        assertEquals(1, successCount.get());
    }

    @Test
    public void agentInfoShouldKeepRefreshing() throws InterruptedException {
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 100L;
        final long agentInfoSendRefreshIntervalMs = 1000L;
        final int expectedRefreshCount = 5;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointClientFactory socketFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, socketFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo())
                .refreshInterval(agentInfoSendRefreshIntervalMs)
                .sendInterval(agentInfoSendRetryIntervalMs)
                .build();

        try {
            agentInfoSender.start();
            Thread.sleep(agentInfoSendRefreshIntervalMs * expectedRefreshCount);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, pinpointClient, socketFactory);
        }
        assertTrue(requestCount.get() >= expectedRefreshCount);
        assertTrue(successCount.get() >= expectedRefreshCount);
    }

    @Test
    public void serverMetaDataShouldBeSentOnPublish() throws InterruptedException {
        // Given
        final AtomicInteger requestCount = new AtomicInteger();
        final AtomicInteger successCount = new AtomicInteger();
        final long agentInfoSendRetryIntervalMs = 1000L;

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointServerAcceptor serverAcceptor = createServerAcceptor(serverListener);

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, clientFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo()).sendInterval(agentInfoSendRetryIntervalMs).build();
        final List<ServerMetaData> serverMetaDataObjects = new ArrayList<ServerMetaData>();
        serverMetaDataObjects.add(new DefaultServerMetaData("server1", Collections.<String> emptyList(), Collections.<Integer, String> emptyMap(), Collections.<ServiceInfo> emptyList()));
        serverMetaDataObjects.add(new DefaultServerMetaData("server2", Collections.<String> emptyList(), Collections.<Integer, String> emptyMap(), Collections.<ServiceInfo> emptyList()));
        serverMetaDataObjects.add(new DefaultServerMetaData("server3", Collections.<String> emptyList(), Collections.<Integer, String> emptyMap(), Collections.<ServiceInfo> emptyList()));
        serverMetaDataObjects.add(new DefaultServerMetaData("server4", Collections.<String> emptyList(), Collections.<Integer, String> emptyMap(), Collections.<ServiceInfo> emptyList()));
        serverMetaDataObjects.add(new DefaultServerMetaData("server5", Collections.<String> emptyList(), Collections.<Integer, String> emptyMap(), Collections.<ServiceInfo> emptyList()));
        // When
        try {
            for (ServerMetaData serverMetaData : serverMetaDataObjects) {
                agentInfoSender.publishServerMetaData(serverMetaData);
            }
            Thread.sleep(10000L);
        } finally {
            closeAll(serverAcceptor, agentInfoSender, pinpointClient, clientFactory);
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

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, clientFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo()).sendInterval(agentInfoSendRetryIntervalMs).build();
        final ServerMetaDataHolder metaDataContext = new DefaultServerMetaDataHolder(Collections.<String> emptyList());
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
            closeAll(serverAcceptor, agentInfoSender, pinpointClient, clientFactory);
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
        final int maxTryPerAttempt = Integer.MAX_VALUE;
        final int expectedTriesUntilSuccess = (int) stressTestTime / (randomMaxTime * 2);

        ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount, expectedTriesUntilSuccess);

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        PinpointClient pinpointClient = ClientFactoryUtils.createPinpointClient(HOST, PORT, clientFactory);

        TcpDataSender dataSender = new TcpDataSender(pinpointClient);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, getAgentInfo())
                .sendInterval(agentInfoSendRetryIntervalMs)
                .maxTryPerAttempt(maxTryPerAttempt)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            agentInfoSender.start();

            Random random = new Random(System.currentTimeMillis());

            while (System.currentTimeMillis() < startTime + stressTestTime) {
                createAndDeleteServer(serverListener, Math.abs(random.nextInt(randomMaxTime)));
                Thread.sleep(Math.abs(random.nextInt(1000)));
            }

        } finally {
            closeAll(null, agentInfoSender, pinpointClient, clientFactory);
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

    private void closeAll(PinpointServerAcceptor serverAcceptor, AgentInfoSender agentInfoSender, PinpointClient pinpointClient, PinpointClientFactory factory) {
        if (serverAcceptor != null) {
            serverAcceptor.close();
        }

        if (agentInfoSender != null) {
            agentInfoSender.stop();
        }

        if (pinpointClient != null) {
            pinpointClient.close();
        }

        if (factory != null) {
            factory.release();
        }
    }

    private AgentInformation getAgentInfo() {
        AgentInformation agentInfo = new AgentInformation("agentId", "appName", System.currentTimeMillis(), 1111, "hostname", "127.0.0.1", ServiceType.USER,
                JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VERSION), Version.VERSION);
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
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            logger.info("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            logger.info("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

            int requestCount = this.requestCount.incrementAndGet();
            if (requestCount < successCondition) {
                return;
            }

            try {
                HeaderTBaseSerializer serializer = HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer();

                TResult result = new TResult(true);
                byte[] resultBytes = serializer.serialize(result);

                this.successCount.incrementAndGet();

                pinpointSocket.response(requestPacket, resultBytes);
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

    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new PinpointClientFactory();
        clientFactory.setTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.<String, Object> emptyMap());

        return clientFactory;
    }

}
