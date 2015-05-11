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
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;

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
        
        throw new IOException("can't find available port.");
    }

    public static PinpointServerAcceptor createPinpointServerFactory(int bindPort) {
        return createPinpointServerFactory(bindPort, null);
    }
    
    public static PinpointServerAcceptor createPinpointServerFactory(int bindPort, ServerMessageListener messageListener) {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.bind("127.0.0.1", bindPort);
        
        if (messageListener != null) {
            serverAcceptor.setMessageListener(messageListener);
        }

        return serverAcceptor;
    }
    
    public static void close(PinpointServerAcceptor serverAcceptor, PinpointServerAcceptor... serverAcceptors) {
        if (serverAcceptor != null) {
            serverAcceptor.close();
        }
        
        if (serverAcceptors != null) {
            for (PinpointServerAcceptor eachServerAcceptor : serverAcceptors) {
                if (eachServerAcceptor != null) {
                    eachServerAcceptor.close();
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

    public static byte[] request(PinpointServer writableServer, byte[] message) {
        Future<ResponseMessage> future = writableServer.request(message);
        future.await();
        return future.getResult().getMessage();
    }

    public static byte[] request(PinpointSocket pinpointSocket, byte[] message) {
        Future<ResponseMessage> future = pinpointSocket.request(message);
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
        public void handleSend(SendPacket sendPacket, PinpointServer pinpointServer) {
            sendPacketRepository.add(sendPacket);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointServer pinpointServer) {
            requestPacketRepository.add(requestPacket);

            logger.info("handlerRequest {}", requestPacket);
            
            pinpointServer.response(requestPacket, requestPacket.getPayload());
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
