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

import com.navercorp.pinpoint.rpc.codec.TestCodec;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.PingSimplePacket;
import com.navercorp.pinpoint.rpc.packet.PongPacket;
import com.navercorp.pinpoint.rpc.server.CountCheckServerMessageListenerFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.util.IOUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

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
        final CountDownLatch pingLatch = new CountDownLatch(1);

        CountCheckServerMessageListenerFactory messageListenerFactory = new CountCheckServerMessageListenerFactory();
        messageListenerFactory.setPingCountDownLatch(pingLatch);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, messageListenerFactory);

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            sendPingAndReceivePongPacket(socket, PingPacket.PING_PACKET);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertFalse(isSuccess(pingLatch));
    }

    @Test
    public void legacyHealthCheckTest2() throws Exception {
        final CountDownLatch pingLatch = new CountDownLatch(1);

        CountCheckServerMessageListenerFactory messageListenerFactory = new CountCheckServerMessageListenerFactory();
        messageListenerFactory.setPingCountDownLatch(pingLatch);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, messageListenerFactory);

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            sendPingAndReceivePongPacket(socket, new PingPacket(1, (byte) 1, (byte) 10));
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertTrue(isSuccess(pingLatch));
    }

    private boolean isSuccess(CountDownLatch latch) {
        if (latch != null && latch.getCount() == 0) {
            return true;
        }
        return false;
    }

    @Test
    public void healthCheckTest() throws Exception {
        final CountDownLatch pingLatch = new CountDownLatch(1);

        CountCheckServerMessageListenerFactory messageListenerFactory = new CountCheckServerMessageListenerFactory();
        messageListenerFactory.setPingCountDownLatch(pingLatch);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, messageListenerFactory);

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            // RUN_WITHOUT_HANDSHAKE
            sendPingAndReceivePongPacket(socket, new PingPayloadPacket(1, (byte) 1, (byte) 10));
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }

        Assert.assertTrue(isSuccess(pingLatch));
    }

    @Test
    public void healthCheckSimplePingTest() throws Exception {
        final CountDownLatch pingLatch = new CountDownLatch(1);

        CountCheckServerMessageListenerFactory messageListenerFactory = new CountCheckServerMessageListenerFactory();
        messageListenerFactory.setPingCountDownLatch(pingLatch);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, messageListenerFactory);

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

        Assert.assertFalse(isSuccess(pingLatch));
    }

    @Test
    public void stateSyncFailTest() throws Exception {
        final CountDownLatch pingLatch = new CountDownLatch(1);

        CountCheckServerMessageListenerFactory messageListenerFactory = new CountCheckServerMessageListenerFactory();
        messageListenerFactory.setPingCountDownLatch(pingLatch);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, messageListenerFactory);

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

        Assert.assertFalse(isSuccess(pingLatch));
    }

    @Ignore
    @Test
    public void expiredHealthCheckTest() throws Exception {
        final CountDownLatch pingLatch = new CountDownLatch(1);

        CountCheckServerMessageListenerFactory messageListenerFactory = new CountCheckServerMessageListenerFactory();
        messageListenerFactory.setPingCountDownLatch(pingLatch);

        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, messageListenerFactory);

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

        Assert.assertFalse(isSuccess(pingLatch));
    }

    private void sendPingAndReceivePongPacket(Socket socket, Packet pingPacket) throws IOException, ProtocolException {
        sendPingPacket(socket.getOutputStream(), pingPacket);
        PongPacket pongPacket = readPongPacket(socket.getInputStream());
        Assert.assertNotNull(pongPacket);
    }

    private void sendPingPacket(OutputStream outputStream, Packet pingPacket) throws ProtocolException, IOException {
        byte[] payload = TestCodec.encodePacket(pingPacket);
        IOUtils.write(outputStream, payload);
    }

    private PongPacket readPongPacket(InputStream inputStream) throws ProtocolException, IOException {
        byte[] payload = IOUtils.read(inputStream, 50, 3000);
        return (PongPacket) TestCodec.decodePacket(payload);
    }

}
