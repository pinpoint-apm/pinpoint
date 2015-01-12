package com.navercorp.pinpoint.rpc.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.SimpleLoggingServerMessageListener;

public class ClientMessageListenerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void clientMessageListenerTest1() throws InterruptedException {
        PinpointServerSocket serverSocket = new PinpointServerSocket();
        serverSocket.bind("127.0.0.1", 10234);
        serverSocket.setMessageListener(new AlwaysHandshakeSuccessListener());

        EchoMessageListener echoMessageListener = new EchoMessageListener();
        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        socketFactory.setMessageListener(echoMessageListener);

        try {
            PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234);
            Thread.sleep(500);

            List<ChannelContext> channelContextList = serverSocket.getDuplexCommunicationChannelContext();
            if (channelContextList.size() != 1) {
                Assert.fail();
            }

            ChannelContext channelContext = channelContextList.get(0);
            assertSendMessage(channelContextList.get(0), "simple", echoMessageListener);
            assertRequestMessage(channelContext, "request", echoMessageListener);

            socket.close();
        } finally {
            socketFactory.release();
            serverSocket.close();
        }
    }

    @Test
    public void clientMessageListenerTest2() throws InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        ss.setMessageListener(new AlwaysHandshakeSuccessListener());

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

            // channelcontext matching error
            ChannelContext channelContext = channelContextList.get(0);
            assertRequestMessage(channelContext, "socket1", null);

            ChannelContext channelContext2 = channelContextList.get(1);
            assertRequestMessage(channelContext2, "socket2", null);

            Assert.assertEquals(1, echoMessageListener1.getRequestPacketRepository().size());
            Assert.assertEquals(1, echoMessageListener2.getRequestPacketRepository().size());
            
            socket.close();
            socket2.close();
        } finally {
            socketFactory1.release();
            socketFactory2.release();

            ss.close();
        }
    }

    private void assertSendMessage(ChannelContext channelContext, String message, EchoMessageListener echoMessageListener) throws InterruptedException {
        channelContext.getSocketChannel().sendMessage(message.getBytes());
        Thread.sleep(100);

        Assert.assertEquals(message, new String(echoMessageListener.getSendPacketRepository().get(0).getPayload()));
    }

    private void assertRequestMessage(ChannelContext channelContext, String message, EchoMessageListener echoMessageListener) throws InterruptedException {
        Future<ResponseMessage> future = channelContext.getSocketChannel().sendRequestMessage(message.getBytes());
        future.await();

        ResponseMessage result = future.getResult();
        Assert.assertEquals(message, new String(result.getMessage()));

        if (echoMessageListener != null) {
            Assert.assertEquals(message, new String(echoMessageListener.getRequestPacketRepository().get(0).getPayload()));
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

    private class AlwaysHandshakeSuccessListener extends SimpleLoggingServerMessageListener {
        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.info("handleEnableWorker {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;

        }
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

}
