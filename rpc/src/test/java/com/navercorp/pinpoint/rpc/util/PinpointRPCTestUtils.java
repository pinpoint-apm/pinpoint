/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.util;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.LoggingStateChangeEventListener;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
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

    public static DefaultPinpointClientFactory createClientFactory(Map<String, Object> param, MessageListener messageListener) {
        DefaultPinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setConnectTimeout(100);
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

            pinpointSocket.response(requestPacket.getRequestId(), payload);
        }

        public List<SendPacket> getSendPacketRepository() {
            return sendPacketRepository;
        }

        public List<RequestPacket> getRequestPacketRepository() {
            return requestPacketRepository;
        }
    }

}
