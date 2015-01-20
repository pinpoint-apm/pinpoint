package com.navercorp.pinpoint.rpc.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.SocketChannel;

public final class PinpointRPCTestUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(PinpointRPCTestUtils.class);

    private PinpointRPCTestUtils() {
    }
    

    public static int findAvailablePort() throws IOException {
        return findAvailablePort(21111);
    }

    public static int findAvailablePort(int defaultPort) throws IOException {
        int bindPort = defaultPort;

        ServerSocket serverSocket = null;
        while (0xFFFF >= bindPort && serverSocket == null) {
            try {
                serverSocket = new ServerSocket(bindPort);
            } catch (IOException ex) {
                bindPort++;
            }
        }
        
        if (serverSocket != null) {
            serverSocket.close();
            return bindPort;
        } 
        
        throw new IOException("can't find avaiable port.");
    }

    public static PinpointServerSocket createServerSocket(int bindPort) {
        return createServerSocket(bindPort, null);
    }
    
    public static PinpointServerSocket createServerSocket(int bindPort, ServerMessageListener messageListener) {
        PinpointServerSocket serverSocket = new PinpointServerSocket();
        serverSocket.bind("127.0.0.1", bindPort);
        
        if (messageListener != null) {
            serverSocket.setMessageListener(messageListener);
        }

        return serverSocket;
    }
    
    public static void close(PinpointServerSocket serverSocket, PinpointServerSocket... serverSockets) {
        if (serverSocket != null) {
            serverSocket.close();
        }
        
        if (serverSockets != null) {
            for (PinpointServerSocket eachServerSocket : serverSockets) {
                if (eachServerSocket != null) {
                    eachServerSocket.close();
                }
            }
        }
    }
    
    public static PinpointSocketFactory createSocketFactory(Map param) {
        return createSocketFactory(param, null);
    }
    
    public static PinpointSocketFactory createSocketFactory(Map param, MessageListener messageListener) {
        PinpointSocketFactory socketFactory = new PinpointSocketFactory();
        socketFactory.setProperties(param);

        if (messageListener != null) {
            socketFactory.setMessageListener(messageListener);
        }
        
        return socketFactory;
    }

    public static byte[] request(PinpointSocket socket, byte[] message) {
        Future<ResponseMessage> future = socket.request(message);
        future.await();
        return future.getResult().getMessage();
    }
    
    public static byte[] request(ChannelContext channelContext, byte[] message) {
        Future<ResponseMessage> future = channelContext.getSocketChannel().sendRequestMessage(message);
        future.await();

        return future.getResult().getMessage();
    }
    
    public static void close(PinpointSocket socket, PinpointSocket... sockets) {
        if (socket != null) {
            socket.close();
        }
        
        if (sockets != null) {
            for (PinpointSocket eachSocket : sockets) {
                if (eachSocket != null) {
                    eachSocket.close();
                }
            }
        }
    }
    
    public static void close(Socket socket, Socket... sockets) throws IOException {
        if (socket != null) {
            socket.close();
        }
        
        if (sockets != null) {
            for (Socket eachSocket : sockets) {
                if (eachSocket != null) {
                    eachSocket.close();
                }
            }
        }
    }

    
    public static EchoServerListener createEchoServerListener() {
        return new EchoServerListener();
    }

    public static EchoClientListener createEchoClientListener() {
        return new EchoClientListener();
    }

    public static Map getParams() {
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

    public static class EchoServerListener implements ServerMessageListener {
        private final List<SendPacket> sendPacketRepository = new ArrayList<SendPacket>();
        private final List<RequestPacket> requestPacketRepository = new ArrayList<RequestPacket>();
        
        @Override
        public void handleSend(SendPacket sendPacket, SocketChannel channel) {
            sendPacketRepository.add(sendPacket);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
            requestPacketRepository.add(requestPacket);

            logger.info("handlerRequest {}", requestPacket, channel);
            channel.sendResponseMessage(requestPacket, requestPacket.getPayload());
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.info("handle Handshake {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }
    }
    
    public static class EchoClientListener implements MessageListener {
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
