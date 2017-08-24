/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.config.DataReceiverGroupConfiguration;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.UdpDataSender;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import com.navercorp.pinpoint.thrift.dto.TResult;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class DataReceiverGroupTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataReceiverGroupTest.class);

    @Test
    public void receiverGroupTest1() throws Exception {
        DataReceiverGroupConfiguration mockConfig = createMockConfig(true, true);

        CountDownLatch sendLatch = new CountDownLatch(2);
        CountDownLatch requestLatch = new CountDownLatch(1);

        DataReceiverGroup receiver = null;
        DataSender udpDataSender = null;
        TcpDataSender tcpDataSender = null;
        PinpointClient pinpointClient = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            receiver = new DataReceiverGroup("name", mockConfig, null, new TestDispatchHandler(sendLatch, requestLatch));
            receiver.start();

            udpDataSender = new UdpDataSender("127.0.0.1", mockConfig.getUdpBindPort(), "test", 10, 1000, 1024 * 64 * 100);

            pinpointClientFactory = createPinpointClientFactory();
            pinpointClient = ClientFactoryUtils.createPinpointClient("127.0.0.1", mockConfig.getTcpBindPort(), pinpointClientFactory);
            tcpDataSender = new TcpDataSender(pinpointClient);

            udpDataSender.send(new TResult());

            tcpDataSender.send(new TResult());
            tcpDataSender.request(new TResult());

            Assert.assertTrue(pinpointClient.isConnected());

            Assert.assertTrue(sendLatch.await(1000, TimeUnit.MILLISECONDS));
            Assert.assertTrue(requestLatch.await(1000, TimeUnit.MILLISECONDS));
        } finally {
            closeDataSender(udpDataSender);
            closeDataSender(tcpDataSender);
            closeClient(pinpointClient);
            closeClientFactory(pinpointClientFactory);
        }
    }

    @Test
    public void receiverGroupTest2() throws Exception {
        DataReceiverGroupConfiguration mockConfig = createMockConfig(true, false);

        CountDownLatch sendLatch = new CountDownLatch(1);
        CountDownLatch requestLatch = new CountDownLatch(1);

        DataReceiverGroup receiver = null;
        DataSender udpDataSender = null;
        TcpDataSender tcpDataSender = null;
        PinpointClient pinpointClient = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            receiver = new DataReceiverGroup("name", mockConfig, null, new TestDispatchHandler(sendLatch, requestLatch));
            receiver.start();

            udpDataSender = new UdpDataSender("127.0.0.1", mockConfig.getUdpBindPort(), "test", 10, 1000, 1024 * 64 * 100);
            udpDataSender.send(new TResult());

            Assert.assertFalse(sendLatch.await(1000, TimeUnit.MILLISECONDS));

            pinpointClientFactory = createPinpointClientFactory();
            pinpointClient = ClientFactoryUtils.createPinpointClient("127.0.0.1", mockConfig.getTcpBindPort(), pinpointClientFactory);
            tcpDataSender = new TcpDataSender(pinpointClient);

            Assert.assertTrue(pinpointClient.isConnected());

            tcpDataSender.send(new TResult());
            tcpDataSender.request(new TResult());

            Assert.assertTrue(sendLatch.await(1000, TimeUnit.MILLISECONDS));
            Assert.assertTrue(requestLatch.await(1000, TimeUnit.MILLISECONDS));
        } finally {
            closeDataSender(udpDataSender);
            closeDataSender(tcpDataSender);
            closeClient(pinpointClient);
            closeClientFactory(pinpointClientFactory);
        }
    }

    @Test
    public void receiverGroupTest3() throws Exception {
        DataReceiverGroupConfiguration mockConfig = createMockConfig(false, true);

        CountDownLatch sendLatch = new CountDownLatch(1);

        DataReceiverGroup receiver = null;
        DataSender udpDataSender = null;
        TcpDataSender tcpDataSender = null;
        PinpointClient pinpointClient = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            receiver = new DataReceiverGroup("name", mockConfig, null, new TestDispatchHandler(sendLatch, new CountDownLatch(1)));
            receiver.start();

            udpDataSender = new UdpDataSender("127.0.0.1", mockConfig.getUdpBindPort(), "test", 10, 1000, 1024 * 64 * 100);
            udpDataSender.send(new TResult());

            Assert.assertTrue(sendLatch.await(1000, TimeUnit.MILLISECONDS));

            pinpointClientFactory = createPinpointClientFactory();
            pinpointClient = ClientFactoryUtils.createPinpointClient("127.0.0.1", mockConfig.getTcpBindPort(), pinpointClientFactory);
            tcpDataSender = new TcpDataSender(pinpointClient);

            Assert.assertFalse(pinpointClient.isConnected());
        } finally {
            closeDataSender(udpDataSender);
            closeDataSender(tcpDataSender);
            closeClient(pinpointClient);
            closeClientFactory(pinpointClientFactory);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiverGroupTest4() throws Exception {
        DataReceiverGroupConfiguration mockConfig = createMockConfig(false, false);

        new DataReceiverGroup("name", mockConfig, null, new TestDispatchHandler(new CountDownLatch(1), new CountDownLatch(1)));
    }

    private void closeReceiver(DataReceiver receiver) {
        try {
            if (receiver != null) {
                receiver.shutdown();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void closeDataSender(DataSender dataSender) {
        try {
            if (dataSender != null) {
                dataSender.stop();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void closeClient(PinpointClient client) {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void closeClientFactory(PinpointClientFactory factory) {
        try {
            if (factory != null) {
                factory.release();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private DataReceiverGroupConfiguration createMockConfig(boolean tcpEnable, boolean udpEnable) {
        DataReceiverGroupConfiguration config = mock(DataReceiverGroupConfiguration.class);

        when(config.isTcpEnable()).thenReturn(tcpEnable);
        when(config.getTcpBindIp()).thenReturn("0.0.0.0");
        when(config.getTcpBindPort()).thenReturn(SocketUtils.findAvailableTcpPort(19099));

        when(config.isUdpEnable()).thenReturn(udpEnable);
        when(config.getUdpBindIp()).thenReturn("0.0.0.0");
        when(config.getUdpBindPort()).thenReturn(SocketUtils.findAvailableTcpPort(29099));

        when(config.getWorkerThreadSize()).thenReturn(2);
        when(config.getWorkerQueueSize()).thenReturn(10);
        when(config.getUdpReceiveBufferSize()).thenReturn(65535);
        when(config.isWorkerMonitorEnable()).thenReturn(false);

        return config;
    }

    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.<String, Object>emptyMap());

        return clientFactory;
    }


    private static class TestDispatchHandler implements DispatchHandler {

        private final CountDownLatch sendLatch;
        private final CountDownLatch requestLatch;

        public TestDispatchHandler(CountDownLatch sendLatch, CountDownLatch requestLatch) {
            this.sendLatch = sendLatch;
            this.requestLatch = requestLatch;
        }

        @Override
        public void dispatchSendMessage(TBase<?, ?> tBase) {
            LOGGER.info("===================================== send {}", tBase);
            sendLatch.countDown();
        }

        @Override
        public TBase dispatchRequestMessage(TBase<?, ?> tBase) {
            LOGGER.info("===================================== request {}", tBase);
            requestLatch.countDown();
            return new TResult();
        }
    }


}
