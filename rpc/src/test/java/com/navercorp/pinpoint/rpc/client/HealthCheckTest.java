/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.PingSimplePacket;
import com.navercorp.pinpoint.rpc.packet.PongPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.util.IOUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
public class HealthCheckTest {

    private static int bindPort;

    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
    }

    @Test
    public void legacyHealthCheckTest1() throws Exception {
        final AtomicBoolean pingHandled = new AtomicBoolean(false);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new PinpointRPCTestUtils.EchoServerListener() {
            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                pingHandled.compareAndSet(false, true);
            }
        });

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            sendPingAndReceivePongPacket(socket, PingPacket.PING_PACKET);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertFalse(pingHandled.get());
    }

    @Test
    public void legacyHealthCheckTest2() throws Exception {
        final AtomicBoolean pingHandled = new AtomicBoolean(false);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new PinpointRPCTestUtils.EchoServerListener() {
            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                pingHandled.compareAndSet(false, true);
            }
        });

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            sendPingAndReceivePongPacket(socket, new PingPacket(1, (byte) 1, (byte) 10));
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertTrue(pingHandled.get());
    }

    @Test
    public void healthCheckTest() throws Exception {
        final AtomicBoolean pingHandled = new AtomicBoolean(false);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new PinpointRPCTestUtils.EchoServerListener() {
            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                pingHandled.compareAndSet(false, true);
            }
        });

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            // RUN_WITHOUT_HANDSHAKE
            sendPingAndReceivePongPacket(socket, new PingPayloadPacket(1, (byte) 1, (byte) 10));
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertTrue(pingHandled.get());
    }

    @Test
    public void healthCheckSimplePingTest() throws Exception {
        final AtomicBoolean pingHandled = new AtomicBoolean(false);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new PinpointRPCTestUtils.EchoServerListener() {
            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                pingHandled.compareAndSet(false, true);
            }
        });

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            sendPingAndReceivePongPacket(socket, new PingSimplePacket());
            Assert.fail();
        } catch (Exception e) {
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertFalse(pingHandled.get());
    }

    @Test
    public void stateSyncFailTest() throws Exception {
        final AtomicBoolean pingHandled = new AtomicBoolean(false);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new PinpointRPCTestUtils.EchoServerListener() {
            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                pingHandled.compareAndSet(false, true);
            }
        });

        boolean isSuccess = false;
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);

            sendPingAndReceivePongPacket(socket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            sendPingAndReceivePongPacket(socket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            sendPingAndReceivePongPacket(socket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            sendPingAndReceivePongPacket(socket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            isSuccess = true;

            sendPingAndReceivePongPacket(socket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(isSuccess);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertFalse(pingHandled.get());
    }

    @Ignore
    @Test
    public void expiredHealthCheckTest() throws Exception {
        final AtomicBoolean pingHandled = new AtomicBoolean(false);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new PinpointRPCTestUtils.EchoServerListener() {
            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                pingHandled.compareAndSet(false, true);
            }
        });

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);

            Thread.sleep(35 * 60 * 1000);

            sendPingAndReceivePongPacket(socket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            Assert.fail();
        } catch (Exception e) {
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertFalse(pingHandled.get());
    }

    private void sendPingAndReceivePongPacket(Socket socket, Packet pingPacket) throws IOException, ProtocolException {
        sendPingPacket(socket.getOutputStream(), pingPacket);
        PongPacket pongPacket = readPongPacket(socket.getInputStream());
        Assert.assertNotNull(pongPacket);
    }

    private void sendPingPacket(OutputStream outputStream, Packet pingPacket) throws ProtocolException, IOException {
        ByteBuffer bb = pingPacket.toBuffer().toByteBuffer(0, pingPacket.toBuffer().writerIndex());
        IOUtils.write(outputStream, bb.array());
    }


    private PongPacket readPongPacket(InputStream inputStream) throws ProtocolException, IOException {
        byte[] payload = IOUtils.read(inputStream, 50, 3000);
        ChannelBuffer cb = ChannelBuffers.wrappedBuffer(payload);

        short packetType = cb.readShort();

        PongPacket pongPacket = PongPacket.readBuffer(packetType, cb);
        return pongPacket;
    }

}
