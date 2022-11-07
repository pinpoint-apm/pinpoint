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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.testcase.util.SocketUtils;
import org.awaitility.Awaitility;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Taejin Koo
 */
public class PinpointClientStateTest {

    private final TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX);

    @Test
    public void connectFailedStateTest() throws InterruptedException {
        DefaultPinpointClientFactory clientFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);

            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), testServerMessageListenerFactory.create());
            handler = connect(clientFactory, availableTcpPort);

            assertHandlerState(SocketStateCode.CONNECT_FAILED, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientFactory);
        }
    }

    @Test
    public void closeStateTest() throws InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        DefaultPinpointClientFactory clientSocketFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            clientSocketFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), testServerMessageListenerFactory.create());
            handler = connect(clientSocketFactory, bindPort);
            assertHandlerState(SocketStateCode.RUN_DUPLEX, handler);

            handler.close();
            assertHandlerState(SocketStateCode.CLOSED_BY_CLIENT, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientSocketFactory);
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void closeByPeerStateTest() throws InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        DefaultPinpointClientFactory clientFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), testServerMessageListenerFactory.create());
            handler = connect(clientFactory, bindPort);
            assertHandlerState(SocketStateCode.RUN_DUPLEX, handler);

            testPinpointServerAcceptor.close();
            assertHandlerState(SocketStateCode.CLOSED_BY_SERVER, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientFactory);
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void unexpectedCloseStateTest() throws InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        DefaultPinpointClientFactory clientFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), testServerMessageListenerFactory.create());
            handler = connect(clientFactory, bindPort);
            assertHandlerState(SocketStateCode.RUN_DUPLEX, handler);

            clientFactory.release();
            assertHandlerState(SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientFactory);
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void unexpectedCloseByPeerStateTest() throws InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        DefaultPinpointClientFactory clientFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), testServerMessageListenerFactory.create());
            handler = connect(clientFactory, bindPort);
            assertHandlerState(SocketStateCode.RUN_DUPLEX, handler);

            PinpointSocket pinpointServer = testPinpointServerAcceptor.getConnectedPinpointSocketList().get(0);
            ((DefaultPinpointServer) pinpointServer).stop(true);
            assertHandlerState(SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientFactory);
            testPinpointServerAcceptor.close();
        }
    }

    private void assertHandlerState(final SocketStateCode stateCode, final DefaultPinpointClientHandler handler) {
        Awaitility.await()
                .untilAsserted(() -> assertThat(handler.getCurrentStateCode()).isEqualTo(stateCode));
    }

    private DefaultPinpointClientHandler connect(DefaultPinpointClientFactory factory, int port) {
        ChannelFuture future = factory.reconnect(new InetSocketAddress("127.0.0.1", port));
        PinpointClientHandler handler = getSocketHandler(future, new InetSocketAddress("127.0.0.1", port));
        return (DefaultPinpointClientHandler) handler;
    }

    PinpointClientHandler getSocketHandler(ChannelFuture channelConnectFuture, SocketAddress address) {
        Objects.requireNonNull(address, "address");

        Channel channel = channelConnectFuture.getChannel();

        return (PinpointClientHandler) channel.getPipeline().getLast();
    }

    private void closeHandler(DefaultPinpointClientHandler handler) {
        if (handler != null) {
            handler.close();
        }
    }

    private void closeSocketFactory(PinpointClientFactory factory) {
        if (factory != null) {
            factory.release();
        }
    }

}
