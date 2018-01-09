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

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.LoggingStateChangeEventListener;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PinpointRPCTestUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(PinpointRPCTestUtils.class);

    private PinpointRPCTestUtils() {
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
    
    public static PinpointClientFactory createClientFactory(Map<String, Object> param) {
        return createClientFactory(param, null);
    }
    
    public static PinpointClientFactory createClientFactory(Map<String, Object> param, MessageListener messageListener) {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setProperties(param);
        clientFactory.addStateChangeEventListener(LoggingStateChangeEventListener.INSTANCE);

        if (messageListener != null) {
            clientFactory.setMessageListener(messageListener);
        }
        
        return clientFactory;
    }

    public static byte[] request(PinpointSocket writableServer, byte[] message) {
        Future<ResponseMessage> future = writableServer.request(message);
        future.await();
        return future.getResult().getMessage();
    }

    public static byte[] request(PinpointClient client, byte[] message) {
        Future<ResponseMessage> future = client.request(message);
        future.await();
        return future.getResult().getMessage();
    }

    public static void close(PinpointClient client, PinpointClient... clients) {
        if (client != null) {
            client.close();
        }
        
        if (clients != null) {
            for (PinpointClient eachSocket : clients) {
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

    public static Map<String, Object> getParams() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(HandshakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(HandshakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(HandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(HandshakePropertyType.IP.getName(), "ip");
        properties.put(HandshakePropertyType.PID.getName(), 1111);
        properties.put(HandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(HandshakePropertyType.START_TIMESTAMP.getName(), System.currentTimeMillis());
        properties.put(HandshakePropertyType.VERSION.getName(), "1.0");

        return properties;
    }

    public static class EchoServerListener implements ServerMessageListener {
        private final List<SendPacket> sendPacketRepository = new ArrayList<SendPacket>();
        private final List<RequestPacket> requestPacketRepository = new ArrayList<RequestPacket>();

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            logger.debug("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
            sendPacketRepository.add(sendPacket);
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            logger.debug("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

            requestPacketRepository.add(requestPacket);
            pinpointSocket.response(requestPacket, requestPacket.getPayload());
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.debug("handle Handshake {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
        }

    }
    
    public static class EchoClientListener implements MessageListener {
        private final List<SendPacket> sendPacketRepository = new ArrayList<SendPacket>();
        private final List<RequestPacket> requestPacketRepository = new ArrayList<RequestPacket>();

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            sendPacketRepository.add(sendPacket);

            byte[] payload = sendPacket.getPayload();
            logger.debug(new String(payload));
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            requestPacketRepository.add(requestPacket);

            byte[] payload = requestPacket.getPayload();
            logger.debug(new String(payload));

            pinpointSocket.response(requestPacket, payload);
        }

        public List<SendPacket> getSendPacketRepository() {
            return sendPacketRepository;
        }

        public List<RequestPacket> getRequestPacketRepository() {
            return requestPacketRepository;
        }
    }

}
