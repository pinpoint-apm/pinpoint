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

package com.navercorp.pinpoint.collector.receiver;

import com.google.common.util.concurrent.MoreExecutors;
import com.navercorp.pinpoint.collector.config.DataReceiverGroupConfiguration;
import com.navercorp.pinpoint.collector.receiver.thrift.PinpointServerAcceptorProvider;
import com.navercorp.pinpoint.collector.receiver.thrift.TCPReceiverBean;
import com.navercorp.pinpoint.collector.receiver.thrift.UDPReceiverBean;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.profiler.context.thrift.BypassMessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.ByteMessage;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.MessageSerializer;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.ThriftUdpMessageSerializer;
import com.navercorp.pinpoint.profiler.sender.UdpDataSender;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.dto.TResult;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
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

        TestDispatchHandler dispatchHandler = new TestDispatchHandler(2, 1);

        UDPReceiverBean udpReceiverBean = createUdpReceiverBean(mockConfig, dispatchHandler);
        TCPReceiverBean tcpReceiverBean = createTcpReceiverBean(mockConfig, dispatchHandler);


        DataSender udpDataSender = null;
        TcpDataSender tcpDataSender = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            udpReceiverBean.afterPropertiesSet();
            tcpReceiverBean.afterPropertiesSet();

            udpDataSender = newUdpDataSender(mockConfig);

            pinpointClientFactory = createPinpointClientFactory();
            tcpDataSender = new TcpDataSender(this.getClass().getName(), "127.0.0.1", mockConfig.getTcpBindPort(), pinpointClientFactory);

            udpDataSender.send(new TResult());

            tcpDataSender.send(new TResult());
            tcpDataSender.request(new TResult());

            Assert.assertTrue(tcpDataSender.isConnected());

            Assert.assertTrue(dispatchHandler.getSendLatch().await(1000, TimeUnit.MILLISECONDS));
            Assert.assertTrue(dispatchHandler.getRequestLatch().await(1000, TimeUnit.MILLISECONDS));
        } finally {
            closeDataSender(udpDataSender);
            closeDataSender(tcpDataSender);
            closeClientFactory(pinpointClientFactory);
            closeBean(udpReceiverBean);
            closeBean(tcpReceiverBean);
        }
    }

    public UdpDataSender newUdpDataSender(DataReceiverGroupConfiguration mockConfig) {
        String threadName = this.getClass().getName();
        MessageConverter<TBase<?, ?>> messageConverter = new BypassMessageConverter<>();
        final MessageSerializer<ByteMessage> thriftMessageSerializer = new ThriftUdpMessageSerializer(messageConverter, ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH);
        return new UdpDataSender("127.0.0.1", mockConfig.getUdpBindPort(), threadName, 10, 1000, 1024 * 64 * 100,
                thriftMessageSerializer);
    }

    private PinpointServerAcceptorProvider createPinpointAcceptorProvider() {
        return new PinpointServerAcceptorProvider();
    }

    private TCPReceiverBean createTcpReceiverBean(DataReceiverGroupConfiguration mockConfig, DispatchHandler dispatchHandler) {
        TCPReceiverBean tcpReceiverBean = new TCPReceiverBean();
        tcpReceiverBean.setBeanName("tcpReceiver");
        tcpReceiverBean.setBindIp(mockConfig.getTcpBindIp());
        tcpReceiverBean.setBindPort(mockConfig.getTcpBindPort());
        tcpReceiverBean.setAcceptorProvider(createPinpointAcceptorProvider());
        tcpReceiverBean.setDispatchHandler(dispatchHandler);
        tcpReceiverBean.setExecutor(MoreExecutors.directExecutor());
        tcpReceiverBean.setEnable(true);
        return tcpReceiverBean;
    }

    private UDPReceiverBean createUdpReceiverBean(DataReceiverGroupConfiguration mockConfig, DispatchHandler dispatchHandler) {
        UDPReceiverBean udpReceiverBean = new UDPReceiverBean();
        udpReceiverBean.setBeanName("udpReceiver");
        udpReceiverBean.setBindIp(mockConfig.getUdpBindIp());
        udpReceiverBean.setBindPort(mockConfig.getUdpBindPort());
        udpReceiverBean.setAddressFilter(AddressFilter.ALL);
        udpReceiverBean.setDispatchHandler(dispatchHandler);
        udpReceiverBean.setExecutor(MoreExecutors.directExecutor());
        udpReceiverBean.setUdpBufferSize(mockConfig.getUdpReceiveBufferSize());
        udpReceiverBean.setEnable(true);
        return udpReceiverBean;
    }

    @Test
    public void receiverGroupTest2() throws Exception {
        DataReceiverGroupConfiguration mockConfig = createMockConfig(true, false);

        TestDispatchHandler testDispatchHandler = new TestDispatchHandler(1, 1);

        TCPReceiverBean receiver = createTcpReceiverBean(mockConfig, testDispatchHandler);
        DataSender udpDataSender = null;
        TcpDataSender tcpDataSender = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            receiver.afterPropertiesSet();

            udpDataSender = newUdpDataSender(mockConfig);
            udpDataSender.send(new TResult());

            Assert.assertFalse(testDispatchHandler.getSendLatch().await(1000, TimeUnit.MILLISECONDS));

            pinpointClientFactory = createPinpointClientFactory();
            tcpDataSender = new TcpDataSender(this.getClass().getName(), "127.0.0.1", mockConfig.getTcpBindPort(), pinpointClientFactory);

            Assert.assertTrue(tcpDataSender.isConnected());

            tcpDataSender.send(new TResult());
            tcpDataSender.request(new TResult());

            Assert.assertTrue(testDispatchHandler.getSendLatch().await(1000, TimeUnit.MILLISECONDS));
            Assert.assertTrue(testDispatchHandler.getRequestLatch().await(1000, TimeUnit.MILLISECONDS));
        } finally {
            closeDataSender(udpDataSender);
            closeDataSender(tcpDataSender);
            closeClientFactory(pinpointClientFactory);
            closeBean(receiver);
        }
    }

    @Test
    public void receiverGroupTest3() throws Exception {
        DataReceiverGroupConfiguration mockConfig = createMockConfig(false, true);

        TestDispatchHandler testDispatchHandler = new TestDispatchHandler(1, 1);

        UDPReceiverBean receiver = createUdpReceiverBean(mockConfig, testDispatchHandler);
        DataSender udpDataSender = null;
        TcpDataSender tcpDataSender = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            receiver.afterPropertiesSet();

            udpDataSender = newUdpDataSender(mockConfig);
            udpDataSender.send(new TResult());

            Assert.assertTrue(testDispatchHandler.getSendLatch().await(1000, TimeUnit.MILLISECONDS));

            pinpointClientFactory = createPinpointClientFactory();
            tcpDataSender = new TcpDataSender(this.getClass().getName(), "127.0.0.1", mockConfig.getTcpBindPort(), pinpointClientFactory);

            Assert.assertFalse(tcpDataSender.isConnected());
        } finally {
            closeDataSender(udpDataSender);
            closeDataSender(tcpDataSender);
            closeClientFactory(pinpointClientFactory);
            closeBean(receiver);
        }
    }


    private void closeBean(DisposableBean bean) {
        try {
            if (bean != null) {
                bean.destroy();
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
        when(config.getUdpReceiveBufferSize()).thenReturn(65535);

        when(config.getWorkerThreadSize()).thenReturn(2);
        when(config.getWorkerQueueSize()).thenReturn(10);
        when(config.isWorkerMonitorEnable()).thenReturn(false);

        return config;
    }

    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setWriteTimeoutMillis(1000 * 3);
        clientFactory.setRequestTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.emptyMap());

        return clientFactory;
    }


    private static class TestDispatchHandler implements DispatchHandler {

        private final CountDownLatch sendLatch;
        private final CountDownLatch requestLatch;

        public TestDispatchHandler(int sendLatchCount, int requestLatchCount) {
            this.sendLatch = new CountDownLatch(sendLatchCount);
            this.requestLatch = new CountDownLatch(requestLatchCount);
        }

        public CountDownLatch getSendLatch() {
            return sendLatch;
        }

        public CountDownLatch getRequestLatch() {
            return requestLatch;
        }


        @Override
        public void dispatchSendMessage(ServerRequest serverRequest) {
            LOGGER.debug("===================================== send {}", serverRequest);
            sendLatch.countDown();
        }

        @Override
        public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
            LOGGER.debug("===================================== request {}", serverRequest);
            requestLatch.countDown();
            Object tResult = new TResult();

            serverResponse.write(tResult);
        }

    }

}
