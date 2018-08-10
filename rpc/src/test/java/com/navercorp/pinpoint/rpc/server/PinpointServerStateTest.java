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
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.test.client.TestPinpointClient;
import com.navercorp.pinpoint.test.client.TestRawSocket;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class PinpointServerStateTest {

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 1000);
    private final TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX);

    @Test
    public void closeByPeerTest() {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);

        TestPinpointClient testPinpointClient = new TestPinpointClient(testServerMessageListenerFactory.create(), PinpointRPCTestUtils.getParams());
        try {
            int bindPort = testPinpointServerAcceptor.bind();

            testPinpointClient.connect(bindPort);
            testPinpointServerAcceptor.assertAwaitClientConnected(1000);

            PinpointSocket pinpointServer = testPinpointServerAcceptor.getConnectedPinpointSocketList().get(0);
            if (pinpointServer instanceof PinpointServer) {
                Assert.assertEquals(SocketStateCode.RUN_DUPLEX, ((PinpointServer) pinpointServer).getCurrentStateCode());

                testPinpointClient.disconnect();
                assertPinpointServerState(SocketStateCode.CLOSED_BY_CLIENT, (PinpointServer) pinpointServer);
            } else {
                Assert.fail();
            }
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void closeTest() {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);

        TestPinpointClient testPinpointClient = new TestPinpointClient(testServerMessageListenerFactory.create(), PinpointRPCTestUtils.getParams());
        try {
            int bindPort = testPinpointServerAcceptor.bind();

            testPinpointClient.connect(bindPort);
            testPinpointServerAcceptor.assertAwaitClientConnected(1000);

            PinpointSocket pinpointServer = testPinpointServerAcceptor.getConnectedPinpointSocketList().get(0);
            Assert.assertEquals(SocketStateCode.RUN_DUPLEX, ((PinpointServer) pinpointServer).getCurrentStateCode());

            testPinpointServerAcceptor.close();
            assertPinpointServerState(SocketStateCode.CLOSED_BY_SERVER, (PinpointServer) pinpointServer);
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void unexpectedCloseByPeerTest() throws IOException, ProtocolException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        try {
            int bindPort = testPinpointServerAcceptor.bind();

            TestRawSocket testRawSocket = new TestRawSocket();
            testRawSocket.connect(bindPort);

            testRawSocket.sendHandshakePacket(PinpointRPCTestUtils.getParams());
            testPinpointServerAcceptor.assertAwaitClientConnected(1, 1000);

            PinpointSocket pinpointServer = testPinpointServerAcceptor.getConnectedPinpointSocketList().get(0);
            if (!(pinpointServer instanceof PinpointServer)) {
                testRawSocket.close();
                Assert.fail();
            }

            Assert.assertEquals(SocketStateCode.RUN_DUPLEX, ((PinpointServer) pinpointServer).getCurrentStateCode());
            testRawSocket.close();
            assertPinpointServerState(SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT, (PinpointServer) pinpointServer);
        } finally {
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void unexpectedCloseTest() {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);

        TestPinpointClient testPinpointClient = new TestPinpointClient(testServerMessageListenerFactory.create(), PinpointRPCTestUtils.getParams());
        try {
            int bindPort = testPinpointServerAcceptor.bind();

            testPinpointClient.connect(bindPort);
            testPinpointServerAcceptor.assertAwaitClientConnected(1000);

            PinpointSocket pinpointServer = testPinpointServerAcceptor.getConnectedPinpointSocketList().get(0);
            Assert.assertEquals(SocketStateCode.RUN_DUPLEX, ((PinpointServer) pinpointServer).getCurrentStateCode());

            ((DefaultPinpointServer) pinpointServer).stop(true);
            assertPinpointServerState(SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER, (PinpointServer) pinpointServer);
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
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
