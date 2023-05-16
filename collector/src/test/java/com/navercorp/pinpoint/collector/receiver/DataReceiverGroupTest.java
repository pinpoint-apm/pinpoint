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
import com.navercorp.pinpoint.collector.receiver.thrift.PinpointServerAcceptorProvider;
import com.navercorp.pinpoint.collector.receiver.thrift.TCPReceiverBean;
import com.navercorp.pinpoint.collector.receiver.thrift.UDPReceiverBean;
import com.navercorp.pinpoint.collector.thrift.config.DataReceiverGroupProperties;
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
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.test.util.TestSocketUtils;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class DataReceiverGroupTest {

    private static final Logger LOGGER = LogManager.getLogger(DataReceiverGroupTest.class);

    @Test
    public void receiverGroupTest1() throws Exception {
        DataReceiverGroupProperties properties = createMockProperties(true, true);

        TestDispatchHandler dispatchHandler = new TestDispatchHandler(2, 1);

        UDPReceiverBean udpReceiverBean = createUdpReceiverBean(properties, dispatchHandler);
        TCPReceiverBean tcpReceiverBean = createTcpReceiverBean(properties, dispatchHandler);


        DataSender<TBase<?, ?>> udpDataSender = null;
        TcpDataSender<TBase<?, ?>> tcpDataSender = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            udpReceiverBean.afterPropertiesSet();
            tcpReceiverBean.afterPropertiesSet();

            udpDataSender = newUdpDataSender(properties);

            pinpointClientFactory = createPinpointClientFactory();
            tcpDataSender = new TcpDataSender<>(this.getClass().getName(), "127.0.0.1", properties.getTcpBindPort(), pinpointClientFactory);

            udpDataSender.send(new TResult());

            tcpDataSender.send(new TResult());
            tcpDataSender.request(new TResult());

            Assertions.assertTrue(tcpDataSender.isConnected());

            Assertions.assertTrue(dispatchHandler.getSendLatch().await(1000, TimeUnit.MILLISECONDS));
            Assertions.assertTrue(dispatchHandler.getRequestLatch().await(1000, TimeUnit.MILLISECONDS));
        } finally {
            closeDataSender(udpDataSender);
            closeDataSender(tcpDataSender);
            closeClientFactory(pinpointClientFactory);
            closeBean(udpReceiverBean);
            closeBean(tcpReceiverBean);
        }
    }

    public UdpDataSender<TBase<?, ?>> newUdpDataSender(DataReceiverGroupProperties properties) {
        String threadName = this.getClass().getName();
        MessageConverter<TBase<?, ?>, TBase<?, ?>> messageConverter = new BypassMessageConverter<>();
        SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH);
        final MessageSerializer<TBase<?, ?>, ByteMessage> thriftMessageSerializer = new ThriftUdpMessageSerializer(messageConverter, serializerFactory.createSerializer());
        return new UdpDataSender<>("127.0.0.1", properties.getUdpBindPort(), threadName, 10, 1000, 1024 * 64 * 100,
                thriftMessageSerializer);
    }

    private PinpointServerAcceptorProvider createPinpointAcceptorProvider() {
        return new PinpointServerAcceptorProvider();
    }

    private TCPReceiverBean createTcpReceiverBean(DataReceiverGroupProperties properties, DispatchHandler<TBase<?, ?>, TBase<?, ?>> dispatchHandler) {
        TCPReceiverBean tcpReceiverBean = new TCPReceiverBean();
        tcpReceiverBean.setBeanName("tcpReceiver");
        tcpReceiverBean.setBindIp(properties.getTcpBindIp());
        tcpReceiverBean.setBindPort(properties.getTcpBindPort());
        tcpReceiverBean.setAcceptorProvider(createPinpointAcceptorProvider());
        tcpReceiverBean.setDispatchHandler(dispatchHandler);
        tcpReceiverBean.setExecutor(MoreExecutors.directExecutor());
        tcpReceiverBean.setEnable(true);
        return tcpReceiverBean;
    }

    private UDPReceiverBean createUdpReceiverBean(DataReceiverGroupProperties properties, DispatchHandler<TBase<?, ?>, TBase<?, ?>> dispatchHandler) {
        UDPReceiverBean udpReceiverBean = new UDPReceiverBean();
        udpReceiverBean.setBeanName("udpReceiver");
        udpReceiverBean.setBindIp(properties.getUdpBindIp());
        udpReceiverBean.setBindPort(properties.getUdpBindPort());
        udpReceiverBean.setAddressFilter(AddressFilter.ALL);
        udpReceiverBean.setDispatchHandler(dispatchHandler);
        udpReceiverBean.setExecutor(MoreExecutors.directExecutor());
        udpReceiverBean.setUdpBufferSize(properties.getUdpReceiveBufferSize());
        udpReceiverBean.setEnable(true);
        return udpReceiverBean;
    }

    @Test
    public void receiverGroupTest2() throws Exception {
        DataReceiverGroupProperties properties = createMockProperties(true, false);

        TestDispatchHandler testDispatchHandler = new TestDispatchHandler(1, 1);

        TCPReceiverBean receiver = createTcpReceiverBean(properties, testDispatchHandler);
        DataSender<TBase<?, ?>> udpDataSender = null;
        TcpDataSender<TBase<?, ?>> tcpDataSender = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            receiver.afterPropertiesSet();

            udpDataSender = newUdpDataSender(properties);
            udpDataSender.send(new TResult());

            Assertions.assertFalse(testDispatchHandler.getSendLatch().await(1000, TimeUnit.MILLISECONDS));

            pinpointClientFactory = createPinpointClientFactory();
            tcpDataSender = new TcpDataSender<>(this.getClass().getName(), "127.0.0.1", properties.getTcpBindPort(), pinpointClientFactory);

            Assertions.assertTrue(tcpDataSender.isConnected());

            tcpDataSender.send(new TResult());
            tcpDataSender.request(new TResult());

            Assertions.assertTrue(testDispatchHandler.getSendLatch().await(1000, TimeUnit.MILLISECONDS));
            Assertions.assertTrue(testDispatchHandler.getRequestLatch().await(1000, TimeUnit.MILLISECONDS));
        } finally {
            closeDataSender(udpDataSender);
            closeDataSender(tcpDataSender);
            closeClientFactory(pinpointClientFactory);
            closeBean(receiver);
        }
    }

    @Test
    public void receiverGroupTest3() throws Exception {
        DataReceiverGroupProperties properties = createMockProperties(false, true);

        TestDispatchHandler testDispatchHandler = new TestDispatchHandler(1, 1);

        UDPReceiverBean receiver = createUdpReceiverBean(properties, testDispatchHandler);
        DataSender<TBase<?, ?>> udpDataSender = null;
        TcpDataSender<TBase<?, ?>> tcpDataSender = null;
        PinpointClientFactory pinpointClientFactory = null;

        try {
            receiver.afterPropertiesSet();

            udpDataSender = newUdpDataSender(properties);
            udpDataSender.send(new TResult());

            Assertions.assertTrue(testDispatchHandler.getSendLatch().await(1000, TimeUnit.MILLISECONDS));

            pinpointClientFactory = createPinpointClientFactory();
            tcpDataSender = new TcpDataSender<>(this.getClass().getName(), "127.0.0.1", properties.getTcpBindPort(), pinpointClientFactory);

            Assertions.assertFalse(tcpDataSender.isConnected());
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

    private void closeDataSender(DataSender<?> dataSender) {
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

    private DataReceiverGroupProperties createMockProperties(boolean tcpEnable, boolean udpEnable) {
        DataReceiverGroupProperties properties = mock(DataReceiverGroupProperties.class);

        when(properties.isTcpEnable()).thenReturn(tcpEnable);
        when(properties.getTcpBindIp()).thenReturn("0.0.0.0");
        when(properties.getTcpBindPort()).thenReturn(TestSocketUtils.findAvailableTcpPort());

        when(properties.isUdpEnable()).thenReturn(udpEnable);
        when(properties.getUdpBindIp()).thenReturn("0.0.0.0");
        when(properties.getUdpBindPort()).thenReturn(TestSocketUtils.findAvailableTcpPort());
        when(properties.getUdpReceiveBufferSize()).thenReturn(65535);

        when(properties.getWorkerThreadSize()).thenReturn(2);
        when(properties.getWorkerQueueSize()).thenReturn(10);
        when(properties.isWorkerMonitorEnable()).thenReturn(false);

        return properties;
    }

    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setWriteTimeoutMillis(1000 * 3);
        clientFactory.setRequestTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.emptyMap());

        return clientFactory;
    }


    private static class TestDispatchHandler implements DispatchHandler<TBase<?, ?>, TBase<?, ?>> {

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
        public void dispatchSendMessage(ServerRequest<TBase<?, ?>> serverRequest) {
            LOGGER.debug("===================================== send {}", serverRequest);
            sendLatch.countDown();
        }

        @Override
        public void dispatchRequestMessage(ServerRequest<TBase<?, ?>> serverRequest, ServerResponse<TBase<?, ?>> serverResponse) {
            LOGGER.debug("===================================== request {}", serverRequest);
            requestLatch.countDown();
            TBase<?, ?> tResult = new TResult();
            serverResponse.write(tResult);
        }

    }

}
