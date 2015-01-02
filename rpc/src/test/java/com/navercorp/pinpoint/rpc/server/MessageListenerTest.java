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

package com.navercorp.pinpoint.rpc.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.client.SimpleLoggingMessageListener;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.navercorp.pinpoint.rpc.server.SimpleLoggingServerMessageListener;

public class MessageListenerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void serverMessageListenerTest1() throws InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        ss.setMessageListener(new SimpleListener());

        PinpointSocketFactory socketFactory1 = createPinpointSocketFactory();
        socketFactory1.setMessageListener(new EchoMessageListener());

        PinpointSocketFactory socketFactory2 = createPinpointSocketFactory();

        try {

            PinpointSocket socket = socketFactory1.connect("127.0.0.1", 10234);
            PinpointSocket socket2 = socketFactory2.connect("127.0.0.1", 10234);

            Thread.sleep(500);

            List<ChannelContext> channelContextList = ss.getDuplexCommunicationChannelContext();
            if (channelContextList.size() != 2) {
                Assert.fail();
            }

            socket.close();
            socket2.close();
        } finally {
            socketFactory1.release();
            socketFactory2.release();

            ss.close();
        }
    }

    @Test
    public void serverMessageListenerTest2() throws InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        ss.setMessageListener(new SimpleListener());

        EchoMessageListener echoMessageListener = new EchoMessageListener();

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        socketFactory.setMessageListener(echoMessageListener);

        try {

            PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234);
            Thread.sleep(500);

            List<ChannelContext> channelContextList = ss.getDuplexCommunicationChannelContext();
            if (channelContextList.size() != 1) {
                Assert.fail();
            }

            ChannelContext channelContext = channelContextList.get(0);

            channelContext.getSocketChannel().sendMessage("simple".getBytes());
            Thread.sleep(100);

            Assert.assertEquals("simple", new String(echoMessageListener.getSendPacketRepository().get(0).getPayload()));

            Future<ResponseMessage> future = channelContext.getSocketChannel().sendRequestMessage("request".getBytes());
            future.await();
            ResponseMessage message = future.getResult();
            Assert.assertEquals("request", new String(message.getMessage()));
            Assert.assertEquals("request", new String(echoMessageListener.getRequestPacketRepository().get(0).getPayload()));

            socket.close();
        } finally {
            socketFactory.release();
            ss.close();
        }
    }

    @Test
    public void serverMessageListenerTest3() throws InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        ss.setMessageListener(new SimpleListener());

        PinpointSocketFactory socketFactory1 = createPinpointSocketFactory();
        EchoMessageListener echoMessageListener1 = new EchoMessageListener();
        socketFactory1.setMessageListener(echoMessageListener1);

        PinpointSocketFactory socketFactory2 = createPinpointSocketFactory();
        EchoMessageListener echoMessageListener2 = new EchoMessageListener();
        socketFactory2.setMessageListener(echoMessageListener2);

        try {

            PinpointSocket socket = socketFactory1.connect("127.0.0.1", 10234);
            PinpointSocket socket2 = socketFactory2.connect("127.0.0.1", 10234);

            Thread.sleep(500);

            List<ChannelContext> channelContextList = ss.getDuplexCommunicationChannelContext();
            if (channelContextList.size() != 2) {
                Assert.fail();
            }

            ChannelContext channelContext = channelContextList.get(0);
            Future<ResponseMessage> future = channelContext.getSocketChannel().sendRequestMessage("socket1".getBytes());
            ChannelContext channelContext2 = channelContextList.get(1);
            Future<ResponseMessage> future2 = channelContext2.getSocketChannel().sendRequestMessage("socket2".getBytes());

            future.await();
            future2.await();
            Assert.assertEquals("socket1", new String(future.getResult().getMessage()));
            Assert.assertEquals("socket2", new String(future2.getResult().getMessage()));

            socket.close();
            socket2.close();
        } finally {
            socketFactory1.release();
            socketFactory2.release();

            ss.close();
        }
    }

    @Test
    public void serverMessageListenerTest4() throws InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        ss.setMessageListener(new SimpleListener());

        Map params = getParams();
        PinpointSocketFactory socketFactory = createPinpointSocketFactory(params);
        socketFactory.setMessageListener(new EchoMessageListener());

        try {

            PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234);

            Thread.sleep(500);

            ChannelContext channelContext = getChannelContext("application", "agent", (Long) params.get(AgentHandshakePropertyType.START_TIMESTAMP.getName()), ss.getDuplexCommunicationChannelContext());
            Assert.assertNotNull(channelContext);

            channelContext = getChannelContext("application", "agent", (Long) params.get(AgentHandshakePropertyType.START_TIMESTAMP.getName()) + 1, ss.getDuplexCommunicationChannelContext());
            Assert.assertNull(channelContext);

            socket.close();
        } finally {
            socketFactory.release();
            ss.close();
        }
    }

    @Test
    public void serverMessageListenerTest5() throws InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        ss.setMessageListener(new SimpleListener());

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        socketFactory.setMessageListener(SimpleLoggingMessageListener.LISTENER);
        try {
            // SimpleLoggingMessageListener.LISTENER as default listener can't connect by duplex mode.
            PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234);

            Thread.sleep(500);

            List<ChannelContext> channelContextList = ss.getDuplexCommunicationChannelContext();
            if (channelContextList.size() != 1) {
                Assert.fail();
            }

            socket.close();
        } finally {
            socketFactory.release();
            ss.close();
        }
    }

    // confirm how many times retry when packet has not been received.
    @Test
    public void serverMessageListenerTest6() throws InterruptedException {
        DuplexCheckListener serverListener = new DuplexCheckListener();

        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        ss.setMessageListener(serverListener);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        socketFactory.setEnableWorkerPacketDelay(500);
        socketFactory.setMessageListener(new EchoMessageListener());

        try {

            // SimpleLoggingMessageListener.LISTENER as default listener can't connect by duplex mode.
            PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234);
            Thread.sleep(5000);

            List<ChannelContext> channelContextList = ss.getDuplexCommunicationChannelContext();
            if (channelContextList.size() != 0) {
                Assert.fail();
            }

            System.out.println(serverListener.getReceiveEnableWorkerPacketCount());
            if (serverListener.getReceiveEnableWorkerPacketCount() < 8) {
                Assert.fail();
            }

            socket.close();
        } finally {
            socketFactory.release();
            ss.close();
        }
    }

    private PinpointSocketFactory createPinpointSocketFactory() {
        return createPinpointSocketFactory(getParams());
    }

    private PinpointSocketFactory createPinpointSocketFactory(Map param) {
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setProperties(param);

        return pinpointSocketFactory;
    }

    private Map getParams() {
        Map properties = new HashMap();

        properties.put(AgentHandshakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(AgentHandshakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(AgentHandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(AgentHandshakePropertyType.IP.getName(), "ip");
        properties.put(AgentHandshakePropertyType.PID.getName(), 1111);
        properties.put(AgentHandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(AgentHandshakePropertyType.START_TIMESTAMP.getName(), System.currentTimeMillis());
        properties.put(AgentHandshakePropertyType.VERSION.getName(), "1.0");
        
        return properties;
    }

    class EchoMessageListener implements MessageListener {
        private final List<SendPacket> sendPacketRepository = new ArrayList<SendPacket>();
        private final List<RequestPacket> requestPacketRepository = new ArrayList<RequestPacket>();

        @Override
        public void handleSend(SendPacket sendPacket, Channel channel) {
            sendPacketRepository.add(sendPacket);

            byte[] payload = sendPacket.getPayload();
            logger.debug(new String(payload));
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, Channel channel) {
            requestPacketRepository.add(requestPacket);

            byte[] payload = requestPacket.getPayload();
            logger.debug(new String(payload));

            channel.write(new ResponsePacket(requestPacket.getRequestId(), requestPacket.getPayload()));
        }

        public List<SendPacket> getSendPacketRepository() {
            return sendPacketRepository;
        }

        public List<RequestPacket> getRequestPacketRepository() {
            return requestPacketRepository;
        }
    }

    class DuplexCheckListener extends SimpleLoggingServerMessageListener {

        private final AtomicInteger receiveEnableWorkerPacketCount = new AtomicInteger();

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            receiveEnableWorkerPacketCount.incrementAndGet();
            return HandshakeResponseType.Error.UNKNOWN_ERROR;
        }

        public int getReceiveEnableWorkerPacketCount() {
            return receiveEnableWorkerPacketCount.get();
        }

    }

    private ChannelContext getChannelContext(String applicationName, String agentId, long startTimeMillis, List<ChannelContext> duplexChannelContextList) {
        if (applicationName == null) {
            return null;
        }

        if (agentId == null) {
            return null;
        }

        if (startTimeMillis <= 0) {
            return null;
        }

        List<ChannelContext> channelContextList = new ArrayList<ChannelContext>();

        for (ChannelContext eachContext : duplexChannelContextList) {
            if (eachContext.getCurrentStateCode() == PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION) {
                Map agentProperties = eachContext.getChannelProperties();

                if (!applicationName.equals(agentProperties.get(AgentHandshakePropertyType.APPLICATION_NAME.getName()))) {
                    continue;
                }

                if (!agentId.equals(agentProperties.get(AgentHandshakePropertyType.AGENT_ID.getName()))) {
                    continue;
                }

                if (startTimeMillis != (Long) agentProperties.get(AgentHandshakePropertyType.START_TIMESTAMP.getName())) {
                    continue;
                }

                channelContextList.add(eachContext);
            }
        }


        if (channelContextList.size() == 0) {
            return null;
        }

        if (channelContextList.size() == 1) {
            return channelContextList.get(0);
        } else {
            logger.warn("Ambiguous Channel Context {}, {}, {} (Valid Agent list={}).", applicationName, agentId, startTimeMillis, channelContextList);
            return null;
        }
    }

    private class SimpleListener extends SimpleLoggingServerMessageListener {

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.info("handleEnableWorker {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;

        }

    }


}
