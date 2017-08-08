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

import com.navercorp.pinpoint.profiler.TestAwaitTaskUtils;
import com.navercorp.pinpoint.profiler.TestAwaitUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @author emeroad
 */
public class TcpDataSenderReconnectTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = SocketUtils.findAvailableTcpPort(50050);
    public static final String HOST = "127.0.0.1";

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 5000);

    private int send;

    public PinpointServerAcceptor serverAcceptorStart() {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.setMessageListener(new ServerMessageListener() {

            @Override
            public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
                logger.debug("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
                send++;
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
                logger.debug("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());
            }

            @Override
            public HandshakeResponseCode handleHandshake(Map properties) {
                return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
            }

            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                logger.debug("ping received packet:{}, remote:{}", pingPacket, pinpointServer);
            }
        });
        serverAcceptor.bind(HOST, PORT);
        return serverAcceptor;
    }


    @Test
    public void connectAndSend() throws InterruptedException {
        PinpointServerAcceptor oldAcceptor = serverAcceptorStart();

        PinpointClientFactory clientFactory = createPinpointClientFactory();
        PinpointClient client = ClientFactoryUtils.createPinpointClient(HOST, PORT, clientFactory);

        TcpDataSender sender = new TcpDataSender(client);
        waitClientConnected(oldAcceptor);

        oldAcceptor.close();
        waitClientDisconnected(client);

        logger.debug("Server start------------------");
        PinpointServerAcceptor serverAcceptor = serverAcceptorStart();
        waitClientConnected(serverAcceptor);

        logger.debug("sendMessage------------------");
        sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));

        Thread.sleep(500);
        logger.debug("sender stop------------------");
        sender.stop();

        serverAcceptor.close();
        client.close();
        clientFactory.release();
    }
    
    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.EMPTY_MAP);

        return clientFactory;
    }

    private void waitClientDisconnected(final PinpointClient client) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !client.isConnected();
            }
        });

        Assert.assertTrue(pass);
    }

    private void waitClientConnected(final PinpointServerAcceptor acceptor) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !acceptor.getWritableSocketList().isEmpty();
            }
        });

        Assert.assertTrue(pass);
    }

}
