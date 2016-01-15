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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.TestAwaitTaskUtils;
import com.navercorp.pinpoint.rpc.TestAwaitUtils;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.DefaultPinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class PinpointClientStateTest {

    private static int bindPort;

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 2000);

    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
    }

    @Test
    public void connectFailedStateTest() throws InterruptedException {
        PinpointClientFactory clientFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
            handler = connect(clientFactory);

            assertHandlerState(SocketStateCode.CONNECT_FAILED, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientFactory);
        }
    }

    @Test
    public void closeStateTest() throws InterruptedException {
        PinpointServerAcceptor serverAcceptor = null;
        PinpointClientFactory clientSocketFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, PinpointRPCTestUtils.createEchoServerListener());

            clientSocketFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
            handler = connect(clientSocketFactory);
            assertHandlerState(SocketStateCode.RUN_DUPLEX, handler);

            handler.close();
            assertHandlerState(SocketStateCode.CLOSED_BY_CLIENT, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientSocketFactory);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void closeByPeerStateTest() throws InterruptedException {
        PinpointServerAcceptor serverAcceptor = null;
        PinpointClientFactory clientFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, PinpointRPCTestUtils.createEchoServerListener());

            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
            handler = connect(clientFactory);
            assertHandlerState(SocketStateCode.RUN_DUPLEX, handler);

            serverAcceptor.close();
            assertHandlerState(SocketStateCode.CLOSED_BY_SERVER, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientFactory);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void unexpectedCloseStateTest() throws InterruptedException {
        PinpointServerAcceptor serverAcceptor = null;
        PinpointClientFactory clientFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, PinpointRPCTestUtils.createEchoServerListener());

            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
            handler = connect(clientFactory);
            assertHandlerState(SocketStateCode.RUN_DUPLEX, handler);

            clientFactory.release();
            assertHandlerState(SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientFactory);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void unexpectedCloseByPeerStateTest() throws InterruptedException {
        PinpointServerAcceptor serverAcceptor = null;
        PinpointClientFactory clientFactory = null;
        DefaultPinpointClientHandler handler = null;
        try {
            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, PinpointRPCTestUtils.createEchoServerListener());

            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
            handler = connect(clientFactory);
            assertHandlerState(SocketStateCode.RUN_DUPLEX, handler);

            List<PinpointSocket> pinpointServerList = serverAcceptor.getWritableSocketList();
            PinpointSocket pinpointServer = pinpointServerList.get(0);
            ((DefaultPinpointServer) pinpointServer).stop(true);
            assertHandlerState(SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER, handler);
        } finally {
            closeHandler(handler);
            closeSocketFactory(clientFactory);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    private void assertHandlerState(final SocketStateCode stateCode, final DefaultPinpointClientHandler handler) {
        boolean passed = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return handler.getCurrentStateCode() == stateCode;
            }
        });

        Assert.assertTrue(passed);
    }

    private DefaultPinpointClientHandler connect(PinpointClientFactory factory) {
        ChannelFuture future = factory.reconnect(new InetSocketAddress("127.0.0.1", bindPort));
        PinpointClientHandler handler = getSocketHandler(future, new InetSocketAddress("127.0.0.1", bindPort));
        return (DefaultPinpointClientHandler) handler;
    }

    PinpointClientHandler getSocketHandler(ChannelFuture channelConnectFuture, SocketAddress address) {
        if (address == null) {
            throw new NullPointerException("address");
        }

        Channel channel = channelConnectFuture.getChannel();
        PinpointClientHandler pinpointClientHandler = (PinpointClientHandler) channel.getPipeline().getLast();
        pinpointClientHandler.setConnectSocketAddress(address);

        return pinpointClientHandler;
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
