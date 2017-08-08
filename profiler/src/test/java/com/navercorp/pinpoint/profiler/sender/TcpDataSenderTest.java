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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.*;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TcpDataSenderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = SocketUtils.findAvailableTcpPort(50050);
    public static final String HOST = "127.0.0.1";

    private PinpointServerAcceptor serverAcceptor;
    private CountDownLatch sendLatch;

    @Before
    public void serverStart() {
        serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.setMessageListener(new ServerMessageListener() {

            @Override
            public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
                logger.debug("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
                if (sendLatch != null) {
                    sendLatch.countDown();
                }
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
                logger.debug("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());
            }

            @Override
            public HandshakeResponseCode handleHandshake(Map arg0) {
                return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
            }

            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                logger.debug("ping received packet:{}, remote:{}", pingPacket, pinpointServer);
            }
        });
        serverAcceptor.bind(HOST, PORT);
    }

    @After
    public void serverShutdown() {
        if (serverAcceptor != null) {
            serverAcceptor.close();
        }
    }

    @Test
    public void connectAndSend() throws InterruptedException {
        this.sendLatch = new CountDownLatch(2);

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        
        PinpointClient client = ClientFactoryUtils.createPinpointClient(HOST, PORT, clientFactory);
        
        TcpDataSender sender = new TcpDataSender(client);
        try {
            sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));
            sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));


            boolean received = sendLatch.await(1000, TimeUnit.MILLISECONDS);
            Assert.assertTrue(received);
        } finally {
            sender.stop();
            
            if (client != null) {
                client.close();
            }
            
            if (clientFactory != null) {
                clientFactory.release();
            }
        }
    }
    
    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.EMPTY_MAP);

        return clientFactory;
    }

}
