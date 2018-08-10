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
import com.navercorp.pinpoint.test.client.TestRawSocket;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class HealthCheckTest {

    @Test
    public void legacyHealthCheckTest1() throws Exception {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);
            sendPingAndReceivePongPacket(testRawSocket, PingPacket.PING_PACKET);
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }

        Assert.assertFalse(serverMessageListener.hasReceivedPing());
    }

    @Test
    public void legacyHealthCheckTest2() throws Exception {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);
            sendPingAndReceivePongPacket(testRawSocket, new PingPacket(1, (byte) 1, (byte) 10));
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }

        Assert.assertTrue(serverMessageListener.hasReceivedPing());
    }

    @Test
    public void healthCheckTest() throws Exception {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);
            // RUN_WITHOUT_HANDSHAKE
            sendPingAndReceivePongPacket(testRawSocket, new PingPayloadPacket(1, (byte) 1, (byte) 10));
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }

        Assert.assertTrue(serverMessageListener.hasReceivedPing());
    }

    @Test
    public void healthCheckSimplePingTest() throws Exception {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);
            sendPingAndReceivePongPacket(testRawSocket, new PingSimplePacket());
            Assert.fail();
        } catch (Exception e) {
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }

        Assert.assertFalse(serverMessageListener.hasReceivedPing());
    }

    @Test
    public void stateSyncFailTest() throws Exception {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        boolean isSuccess = false;
        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);

            sendPingAndReceivePongPacket(testRawSocket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            sendPingAndReceivePongPacket(testRawSocket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            sendPingAndReceivePongPacket(testRawSocket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            sendPingAndReceivePongPacket(testRawSocket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            isSuccess = true;

            sendPingAndReceivePongPacket(testRawSocket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(isSuccess);
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }

        Assert.assertFalse(serverMessageListener.hasReceivedPing());
    }

    @Ignore
    @Test
    public void expiredHealthCheckTest() throws Exception {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);

            Thread.sleep(35 * 60 * 1000);

            sendPingAndReceivePongPacket(testRawSocket, new PingPayloadPacket(1, (byte) 1, (byte) 1));
            Assert.fail();
        } catch (Exception e) {
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }

        Assert.assertFalse(serverMessageListener.hasReceivedPing());
    }

    private void sendPingAndReceivePongPacket(TestRawSocket testRawSocket, Packet pingPacket) throws IOException, ProtocolException {
        testRawSocket.sendPingPacket(pingPacket);
        Assert.assertNotNull(testRawSocket.readPongPacket(3000));
    }

}
