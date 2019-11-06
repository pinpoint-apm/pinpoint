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

package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import com.navercorp.pinpoint.profiler.context.DefaultServiceInfo;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.provider.JvmInformationProvider;
import com.navercorp.pinpoint.profiler.context.provider.ServerMetaDataRegistryServiceProvider;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MetadataMessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.ThriftMessageToResultConverter;
import com.navercorp.pinpoint.profiler.sender.MessageSerializer;
import com.navercorp.pinpoint.profiler.sender.ResultResponse;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.ThriftMessageSerializer;
import com.navercorp.pinpoint.profiler.util.AgentInfoFactory;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AgentInfoSenderTest {

    public static final String HOST = "127.0.0.1";

    private final int awaitSpinDelay = 50;

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(this.awaitSpinDelay, 60000);

    private AgentInformation agentInformation;
    private ServerMetaDataRegistryService serverMetaDataRegistryService;
    private JvmInformation jvmInformation;
    private AgentInfoFactory agentInfoFactory;
    private MessageConverter<ResultResponse> resultResponseMessageConverter;

    @Before
    public void init() {
        agentInformation = createAgentInformation();
        serverMetaDataRegistryService = new ServerMetaDataRegistryServiceProvider().get();
        jvmInformation = new JvmInformationProvider().get();
        agentInfoFactory = new AgentInfoFactory(agentInformation, serverMetaDataRegistryService, jvmInformation);
        resultResponseMessageConverter = new ThriftMessageToResultConverter();

    }

    private TcpDataSender newTcpDataSender(PinpointClientFactory clientFactory, int port) {
        MessageConverter<TBase<?, ?>> messageConverter = new MetadataMessageConverter(agentInformation.getApplicationName(), agentInformation.getAgentId(), agentInformation.getStartTime());
        MessageSerializer<byte[]> messageSerializer = new ThriftMessageSerializer(messageConverter);

        return new TcpDataSender(this.getClass().getName(), HOST, port, clientFactory, messageSerializer);
    }

    @Test
    public void agentInfoShouldBeSent() throws InterruptedException {
        final long agentInfoSendRetryIntervalMs = 100L;

        final ResponseServerMessageListenerFactory messageListenerFactory = new ResponseServerMessageListenerFactory();
        ResponseServerMessageListener messageListener = messageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        TcpDataSender dataSender = newTcpDataSender(clientFactory, bindPort);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory).sendInterval(agentInfoSendRetryIntervalMs).setMessageConverter(resultResponseMessageConverter).build();

        try {
            agentInfoSender.start();
            waitExpectedRequestCount(messageListener, 1);
        } finally {
            closeAll(agentInfoSender, clientFactory);
            testPinpointServerAcceptor.close();
        }
        assertEquals(1, messageListener.getRequestCount());
        assertEquals(1, messageListener.getSuccessCount());
    }

    @Test
    public void agentInfoShouldRetryUntilSuccess() throws InterruptedException {
        final long agentInfoSendRetryIntervalMs = 100L;
        final int maxTryPerAttempt = 3;
        final int expectedTriesUntilSuccess = maxTryPerAttempt;

        final ResponseServerMessageListenerFactory messageListenerFactory = new ResponseServerMessageListenerFactory(expectedTriesUntilSuccess);
        ResponseServerMessageListener messageListener = messageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClientFactory socketFactory = createPinpointClientFactory();
        TcpDataSender dataSender = newTcpDataSender(socketFactory, bindPort);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory)
                .maxTryPerAttempt(maxTryPerAttempt)
                .sendInterval(agentInfoSendRetryIntervalMs)
                .setMessageConverter(resultResponseMessageConverter)
                .build();

        try {
            agentInfoSender.start();
            waitExpectedRequestCount(messageListener, expectedTriesUntilSuccess);
        } finally {
            closeAll(agentInfoSender, socketFactory);
            testPinpointServerAcceptor.close();
        }
        assertEquals(expectedTriesUntilSuccess, messageListener.getRequestCount());
        assertEquals(1, messageListener.getSuccessCount());
    }

    @Test
    public void agentInfoShouldInitiallyRetryIndefinitely() throws InterruptedException {
        final long agentInfoSendRetryIntervalMs = 100L;
        final int maxTryPerAttempt = 3;
        final int expectedTriesUntilSuccess = maxTryPerAttempt * 5;

        final ResponseServerMessageListenerFactory messageListenerFactory = new ResponseServerMessageListenerFactory(expectedTriesUntilSuccess);
        ResponseServerMessageListener messageListener = messageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClientFactory socketFactory = createPinpointClientFactory();
        TcpDataSender dataSender = newTcpDataSender(socketFactory, bindPort);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory)
                .maxTryPerAttempt(maxTryPerAttempt)
                .sendInterval(agentInfoSendRetryIntervalMs)
                .setMessageConverter(resultResponseMessageConverter)
                .build();

        try {
            agentInfoSender.start();
            waitExpectedRequestCount(messageListener, expectedTriesUntilSuccess);
        } finally {
            closeAll(agentInfoSender, socketFactory);
            testPinpointServerAcceptor.close();
        }
        assertEquals(expectedTriesUntilSuccess, messageListener.getRequestCount());
        assertEquals(1, messageListener.getSuccessCount());
    }

    @Test
    public void agentInfoShouldRetryUntilAttemptsAreExhaustedWhenRefreshing() throws InterruptedException {

        final long agentInfoSendRetryIntervalMs = 1000L;
        final long agentInfoSendRefreshIntervalMs = 5000L;
        final int maxTryPerAttempt = 3;
        final int expectedSuccessServerTries = 1;
        final int expectedFailServerTries = maxTryPerAttempt;
        final CountDownLatch agentReconnectLatch = new CountDownLatch(1);

        final ResponseServerMessageListenerFactory successMessageListenerFactory = new ResponseServerMessageListenerFactory();
        ResponseServerMessageListener successMessageListener = successMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(successMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        TestPinpointServerAcceptor failTestPinpointServerAcceptor = null;

        PinpointClientFactory socketFactory = createPinpointClientFactory();

        TcpDataSender dataSender = newTcpDataSender(socketFactory, bindPort);
        dataSender.addReconnectEventListener(new PinpointClientReconnectEventListener() {
            @Override
            public void reconnectPerformed(PinpointClient client) {
                agentReconnectLatch.countDown();
            }
        });
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory)
                .maxTryPerAttempt(maxTryPerAttempt)
                .refreshInterval(agentInfoSendRefreshIntervalMs)
                .sendInterval(agentInfoSendRetryIntervalMs)
                .setMessageConverter(resultResponseMessageConverter)
                .build();

        final ResponseServerMessageListenerFactory failMessageListenerFactory = new ResponseServerMessageListenerFactory(Integer.MAX_VALUE);
        ResponseServerMessageListener failMessageListener = failMessageListenerFactory.create();

        try {
            agentInfoSender.start();
            waitExpectedRequestCount(successMessageListener, expectedSuccessServerTries);
            testPinpointServerAcceptor.close();
            Thread.sleep(agentInfoSendRetryIntervalMs * maxTryPerAttempt);

            failTestPinpointServerAcceptor = new TestPinpointServerAcceptor(failMessageListenerFactory);
            failTestPinpointServerAcceptor.bind(bindPort);

            // wait till agent reconnects
            agentReconnectLatch.await();
            waitExpectedRequestCount(failMessageListener, expectedFailServerTries);
        } finally {
            closeAll(agentInfoSender, socketFactory);
            TestPinpointServerAcceptor.staticClose(failTestPinpointServerAcceptor);
            TestPinpointServerAcceptor.staticClose(testPinpointServerAcceptor);
        }
        assertEquals(1, successMessageListener.getSuccessCount() + failMessageListener.getSuccessCount());
        assertEquals(expectedSuccessServerTries, successMessageListener.getRequestCount());
        assertEquals(expectedFailServerTries, failMessageListener.getRequestCount());
    }

    @Test
    public void agentInfoShouldBeSentOnlyOnceEvenAfterReconnect() throws Exception {

        final AtomicInteger reconnectCount = new AtomicInteger();
        final int expectedReconnectCount = 3;
        final long agentInfoSendRetryIntervalMs = 100L;
        final int maxTryPerAttempt = Integer.MAX_VALUE;

        final CyclicBarrier reconnectEventBarrier = new CyclicBarrier(2);

        final ResponseServerMessageListenerFactory messageListenerFactory = new ResponseServerMessageListenerFactory();
        ResponseServerMessageListener messageListener = messageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        TcpDataSender dataSender = newTcpDataSender(clientFactory, bindPort);
        dataSender.addReconnectEventListener(new PinpointClientReconnectEventListener() {
            @Override
            public void reconnectPerformed(PinpointClient client) {
                reconnectCount.incrementAndGet();
                try {
                    reconnectEventBarrier.await();
                } catch (Exception e) {
                    // just fail
                    throw new RuntimeException(e);
                }
            }
        });
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory)
                .sendInterval(agentInfoSendRetryIntervalMs)
                .maxTryPerAttempt(maxTryPerAttempt)
                .setMessageConverter(resultResponseMessageConverter)
                .build();

        try {
            // initial connect
            agentInfoSender.start();
            waitExpectedRequestCount(messageListener, 1);
            testPinpointServerAcceptor.close();
            // reconnect
            for (int i = 0; i < expectedReconnectCount; i++) {
                TestPinpointServerAcceptor reconnectPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
                reconnectPinpointServerAcceptor.bind(bindPort);

                // wait for agent to reconnect
                reconnectEventBarrier.await();
                // wait to see if AgentInfo is sent again (it shouldn't)
                Thread.sleep(1000L);
                reconnectPinpointServerAcceptor.close();
                reconnectEventBarrier.reset();
            }
        } finally {
            closeAll(agentInfoSender, clientFactory);
            testPinpointServerAcceptor.close();
        }
        assertEquals(1, messageListener.getSuccessCount());
        assertEquals(expectedReconnectCount, reconnectCount.get());
    }

    @Test
    public void agentInfoShouldKeepRefreshing() throws InterruptedException {
        final long agentInfoSendRetryIntervalMs = 100L;
        final long agentInfoSendRefreshIntervalMs = 100L;
        final int expectedRefreshCount = 5;

        final ResponseServerMessageListenerFactory messageListenerFactory = new ResponseServerMessageListenerFactory();
        ResponseServerMessageListener messageListener = messageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClientFactory socketFactory = createPinpointClientFactory();
        TcpDataSender dataSender = newTcpDataSender(socketFactory, bindPort);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory)
                .refreshInterval(agentInfoSendRefreshIntervalMs)
                .sendInterval(agentInfoSendRetryIntervalMs)
                .setMessageConverter(resultResponseMessageConverter)
                .build();

        try {
            agentInfoSender.start();
            while (messageListener.getRequestCount() < expectedRefreshCount) {
                Thread.sleep(1000L);
            }
        } finally {
            closeAll(agentInfoSender, socketFactory);
            testPinpointServerAcceptor.close();
        }
        assertTrue(messageListener.getRequestCount() >= expectedRefreshCount);
        assertTrue(messageListener.getSuccessCount() >= expectedRefreshCount);
    }

    @Test
    public void agentInfoShouldBeRefreshedOnServerMetaDataChange() throws InterruptedException {
        // Given
        final int expectedRequestCount = 5;
        final long agentInfoSendRetryIntervalMs = 1000L;

        final ResponseServerMessageListenerFactory messageListenerFactory = new ResponseServerMessageListenerFactory();
        ResponseServerMessageListener messageListener = messageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClientFactory clientFactory = createPinpointClientFactory();

        TcpDataSender dataSender = newTcpDataSender(clientFactory, bindPort);
        final AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory).sendInterval(agentInfoSendRetryIntervalMs).setMessageConverter(resultResponseMessageConverter).build();
        serverMetaDataRegistryService.addListener(new ServerMetaDataRegistryService.OnChangeListener() {
            @Override
            public void onServerMetaDataChange() {
                agentInfoSender.refresh();
            }
        });

        // When
        try {
            for (int i = 0; i < expectedRequestCount; i++) {
                serverMetaDataRegistryService.notifyListeners();
            }

            waitExpectedRequestCount(messageListener, expectedRequestCount);
        } finally {
            closeAll(agentInfoSender, clientFactory);
            testPinpointServerAcceptor.close();
        }
        // Then
        assertEquals(expectedRequestCount, messageListener.getRequestCount());
        assertEquals(expectedRequestCount, messageListener.getSuccessCount());
    }

    @Test
    public void agentInfoShouldBeRefreshedOnServerMetaDataChangeFromMultipleThreads() throws InterruptedException {
        // Given
        final long agentInfoSendRetryIntervalMs = 1000L;
        final int threadCount = 50;
        final CountDownLatch initLatch = new CountDownLatch(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final Queue<Throwable> exceptions = new ConcurrentLinkedQueue<Throwable>();

        final ResponseServerMessageListenerFactory messageListenerFactory = new ResponseServerMessageListenerFactory();
        ResponseServerMessageListener messageListener = messageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClientFactory clientFactory = createPinpointClientFactory();

        TcpDataSender dataSender = newTcpDataSender(clientFactory, bindPort);
        final AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory).sendInterval(agentInfoSendRetryIntervalMs).setMessageConverter(resultResponseMessageConverter).build();
        serverMetaDataRegistryService.addListener(new ServerMetaDataRegistryService.OnChangeListener() {
            @Override
            public void onServerMetaDataChange() {
                agentInfoSender.refresh();
            }
        });

        // When
        for (int i = 0; i < threadCount; i++) {
            final String serviceName = "/name" + i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    initLatch.countDown();
                    try {
                        startLatch.await();
                        ServiceInfo serviceInfo = new DefaultServiceInfo(serviceName, Collections.<String>emptyList());
                        serverMetaDataRegistryService.addServiceInfo(serviceInfo);
                        serverMetaDataRegistryService.notifyListeners();
                    } catch (final Throwable t) {
                        exceptions.add(t);
                    } finally {
                        endLatch.countDown();
                    }
                }
            });
        }
        initLatch.await();
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();
        try {
            waitExpectedRequestCount(messageListener, threadCount);
            waitExpectedSuccessCount(messageListener, threadCount);
        } finally {
            closeAll(agentInfoSender, clientFactory);
            testPinpointServerAcceptor.close();
        }
        // Then
        assertTrue("Failed with exceptions : " + exceptions, exceptions.isEmpty());
        assertEquals(threadCount, messageListener.getRequestCount());
        assertEquals(threadCount, messageListener.getSuccessCount());
    }

    public void reconnectionStressTest() throws InterruptedException {
        final long stressTestTime = 60 * 1000L;
        final int randomMaxTime = 3000;
        final long agentInfoSendRetryIntervalMs = 1000L;
        final int maxTryPerAttempt = Integer.MAX_VALUE;
        final int expectedTriesUntilSuccess = (int) stressTestTime / (randomMaxTime * 2) - 1;

        final ResponseServerMessageListenerFactory messageListenerFactory = new ResponseServerMessageListenerFactory(expectedTriesUntilSuccess);
        ResponseServerMessageListener messageListener = messageListenerFactory.create();

        PinpointClientFactory clientFactory = createPinpointClientFactory();

        int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);

        TcpDataSender dataSender = newTcpDataSender(clientFactory, availableTcpPort);
        AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(dataSender, agentInfoFactory)
                .sendInterval(agentInfoSendRetryIntervalMs)
                .maxTryPerAttempt(maxTryPerAttempt)
                .setMessageConverter(resultResponseMessageConverter)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            agentInfoSender.start();

            Random random = new Random(System.currentTimeMillis());

            while (System.currentTimeMillis() < startTime + stressTestTime) {
                createAndDeleteServer(messageListenerFactory, Math.abs(random.nextInt(randomMaxTime)));
                Thread.sleep(Math.abs(random.nextInt(1000)));
            }

        } finally {
            closeAll(agentInfoSender, clientFactory);
        }
        assertEquals(1, messageListener.getSuccessCount());
        assertEquals(expectedTriesUntilSuccess, messageListener.getRequestCount());
    }

    private void createAndDeleteServer(ServerMessageListenerFactory messageListenerFactory, long waitTimeMillis) throws InterruptedException {
        int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(messageListenerFactory);
        try {
            testPinpointServerAcceptor.bind(availableTcpPort);
            Thread.sleep(waitTimeMillis);
        } finally {
            TestPinpointServerAcceptor.staticClose(testPinpointServerAcceptor);
        }
    }

    private void closeAll(AgentInfoSender agentInfoSender, PinpointClientFactory factory) {
        if (agentInfoSender != null) {
            agentInfoSender.stop();
        }

        if (factory != null) {
            factory.release();
        }
    }

    private AgentInformation createAgentInformation() {
        AgentInformation agentInfo = new DefaultAgentInformation("agentId", "appName", false, System.currentTimeMillis(), 1111, "hostname", "127.0.0.1", ServiceType.USER,
                JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VERSION), Version.VERSION);
        return agentInfo;
    }


    private static class ResponseServerMessageListenerFactory implements ServerMessageListenerFactory<ResponseServerMessageListener> {

        private final ResponseServerMessageListener responseServerMessageListener;

        public ResponseServerMessageListenerFactory() {
            this(1);
        }

        public ResponseServerMessageListenerFactory(int successCondition) {
            this.responseServerMessageListener = new ResponseServerMessageListener(successCondition);
        }

        @Override
        public ResponseServerMessageListener create() {
            return responseServerMessageListener;
        }

    }

    private static class ResponseServerMessageListener implements ServerMessageListener {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final AtomicInteger requestCount;
        private final AtomicInteger successCount;

        private final int successCondition;

        public ResponseServerMessageListener() {
            this(1);
        }

        public ResponseServerMessageListener(int successCondition) {
            this.requestCount = new AtomicInteger();
            this.successCount = new AtomicInteger();
            this.successCondition = successCondition;
        }

        public int getRequestCount() {
            return requestCount.get();
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            logger.debug("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            logger.debug("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

            int requestCount = this.requestCount.incrementAndGet();
            if (requestCount < successCondition) {
                return;
            }

            try {
                HeaderTBaseSerializer serializer = HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer();

                TResult result = new TResult(true);
                byte[] resultBytes = serializer.serialize(result);

                this.successCount.incrementAndGet();

                pinpointSocket.response(requestPacket.getRequestId(), resultBytes);
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
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            logger.debug("ping received packet:{}, remote:{}", pingPacket, pinpointServer);
        }

    }

    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setWriteTimeoutMillis(1000 * 3);
        clientFactory.setRequestTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.<String, Object> emptyMap());

        return clientFactory;
    }

    private void waitExpectedRequestCount(final ResponseServerMessageListener listener, final int expectedRequestCount) {
        TestAwaitTaskUtils task = new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return listener.getRequestCount() == expectedRequestCount;
            }
        };
        awaitTask(task);
    }

    private void waitExpectedSuccessCount(final ResponseServerMessageListener listener, final int expectedRequestCount) {
        TestAwaitTaskUtils task = new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return listener.getSuccessCount() == expectedRequestCount;
            }
        };
        awaitTask(task);
    }

    private void awaitTask(TestAwaitTaskUtils awaitTaskUtils) {
        boolean pass = awaitUtils.await(awaitTaskUtils);
        Assert.assertTrue(pass);
    }
}
