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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.TestAwaitTaskUtils;
import com.navercorp.pinpoint.rpc.TestAwaitUtils;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.IOUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class PinpointServerStateTest {

    private static int bindPort;

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 1000);

    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
    }

    @Test
    public void closeByPeerTest() throws InterruptedException {
        PinpointServerAcceptor serverAcceptor = null;
        PinpointClient client = null;
        PinpointClientFactory clientFactory = null;
        try {
            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, PinpointRPCTestUtils.createEchoServerListener());

            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
            client = clientFactory.connect("127.0.0.1", bindPort);
            assertAvailableWritableSocket(serverAcceptor);

            List<PinpointSocket> pinpointServerList = serverAcceptor.getWritableSocketList();
            PinpointSocket pinpointServer = pinpointServerList.get(0);

            if (pinpointServer instanceof PinpointServer) {
                Assert.assertEquals(SocketStateCode.RUN_DUPLEX, ((PinpointServer) pinpointServer).getCurrentStateCode());

                client.close();

                assertPinpointServerState(SocketStateCode.CLOSED_BY_CLIENT, (PinpointServer) pinpointServer);
            } else {
                Assert.fail();
            }

        } finally {
            PinpointRPCTestUtils.close(client);
            if (clientFactory != null) {
                clientFactory.release();
            }
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void closeTest() throws InterruptedException {
        PinpointServerAcceptor serverAcceptor = null;
        PinpointClient client = null;
        PinpointClientFactory clientFactory = null;
        try {
            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, PinpointRPCTestUtils.createEchoServerListener());

            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
            client = clientFactory.connect("127.0.0.1", bindPort);
            assertAvailableWritableSocket(serverAcceptor);

            List<PinpointSocket> pinpointServerList = serverAcceptor.getWritableSocketList();
            PinpointSocket pinpointServer = pinpointServerList.get(0);
            Assert.assertEquals(SocketStateCode.RUN_DUPLEX, ((PinpointServer) pinpointServer).getCurrentStateCode());

            serverAcceptor.close();
            assertPinpointServerState(SocketStateCode.CLOSED_BY_SERVER, (PinpointServer) pinpointServer);
        } finally {
            PinpointRPCTestUtils.close(client);
            if (clientFactory != null) {
                clientFactory.release();
            }
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void unexpectedCloseByPeerTest() throws InterruptedException, IOException, ProtocolException {
        PinpointServerAcceptor serverAcceptor = null;
        try {
            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, PinpointRPCTestUtils.createEchoServerListener());

            Socket socket = new Socket("127.0.0.1", bindPort);
            IOUtils.write(socket.getOutputStream(), createHandshakePayload(PinpointRPCTestUtils.getParams()));

            final PinpointServerAcceptor ImmutableServerAcceptor = serverAcceptor;
            awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return ImmutableServerAcceptor.getWritableSocketList().size() == 1;
                }
            });

            List<PinpointSocket> pinpointServerList = serverAcceptor.getWritableSocketList();
            PinpointSocket pinpointServer = pinpointServerList.get(0);
            if (!(pinpointServer instanceof PinpointServer)) {
                IOUtils.close(socket);
                Assert.fail();
            }

            Assert.assertEquals(SocketStateCode.RUN_DUPLEX, ((PinpointServer) pinpointServer).getCurrentStateCode());
            IOUtils.close(socket);
            assertPinpointServerState(SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT, (PinpointServer) pinpointServer);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void unexpectedCloseTest() throws InterruptedException, IOException, ProtocolException {
        PinpointServerAcceptor serverAcceptor = null;
        PinpointClient client = null;
        PinpointClientFactory clientFactory = null;
        try {
            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, PinpointRPCTestUtils.createEchoServerListener());

            clientFactory = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
            client = clientFactory.connect("127.0.0.1", bindPort);
            assertAvailableWritableSocket(serverAcceptor);

            List<PinpointSocket> pinpointServerList = serverAcceptor.getWritableSocketList();
            PinpointSocket pinpointServer = pinpointServerList.get(0);

            Assert.assertEquals(SocketStateCode.RUN_DUPLEX, ((PinpointServer) pinpointServer).getCurrentStateCode());

            ((DefaultPinpointServer) pinpointServer).stop(true);
            assertPinpointServerState(SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER, (PinpointServer) pinpointServer);
        } finally {
            PinpointRPCTestUtils.close(client);
            if (clientFactory != null) {
                clientFactory.release();
            }
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    private byte[] createHandshakePayload(Map<String, Object> data) throws ProtocolException {
        byte[] payload = ControlMessageEncodingUtils.encode(data);
        ControlHandshakePacket handshakePacket = new ControlHandshakePacket(payload);
        ChannelBuffer channelBuffer = handshakePacket.toBuffer();
        return channelBuffer.toByteBuffer().array();
    }

    private void assertAvailableWritableSocket(final PinpointServerAcceptor serverAcceptor) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !serverAcceptor.getWritableSocketList().isEmpty();
            }
        });

        Assert.assertTrue(pass);
    }

    private void assertPinpointServerState(final SocketStateCode stateCode, final PinpointServer pinpointServer) {
        boolean passed = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return pinpointServer.getCurrentStateCode() == stateCode;
            }
        });

        Assert.assertTrue(passed);
    }


}
