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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;

/**
 * @author emeroad
 */
public class TcpDataSenderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = 10050;
    public static final String HOST = "127.0.0.1";

    private PinpointServerAcceptor serverAcceptor;
    private CountDownLatch sendLatch;

    @Before
    public void serverStart() {
        serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.setMessageListener(new ServerMessageListener() {
            
            @Override
            public void handleSend(SendPacket sendPacket, PinpointServer pinpointServer) {
                logger.info("handleSend:{}", sendPacket);
                if (sendLatch != null) {
                    sendLatch.countDown();
                }
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, PinpointServer pinpointServer) {
                logger.info("handleRequest:{}", requestPacket);
            }
            
            @Override
            public HandshakeResponseCode handleHandshake(Map arg0) {
                return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
            }

            @Override
            public void handlePing(PingPacket pingPacket, PinpointServer pinpointServer) {
                logger.info("ping received {} {} ", pingPacket, pinpointServer);
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
        
        PinpointClient client = createPinpointClient(HOST, PORT, clientFactory);
        
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
        PinpointClientFactory clientFactory = new PinpointClientFactory();
        clientFactory.setTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.EMPTY_MAP);

        return clientFactory;
    }
    
    private PinpointClient createPinpointClient(String host, int port, PinpointClientFactory clientFactory) {
        PinpointClient client = null;
        for (int i = 0; i < 3; i++) {
            try {
                client = clientFactory.connect(host, port);
                logger.info("tcp connect success:{}/{}", host, port);
                return client;
            } catch (PinpointSocketException e) {
                logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
        logger.warn("change background tcp connect mode  {}/{} ", host, port);
        client = clientFactory.scheduledConnect(host, port);

        return client;
    }
}
