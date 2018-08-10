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
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils.EchoClientListener;
import com.navercorp.pinpoint.test.client.TestPinpointClient;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class ClientMessageListenerTest {

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(10, 1000);
    private final TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX);

    @Test
    public void clientMessageListenerTest1() throws InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        EchoClientListener echoMessageListener = new EchoClientListener();
        TestPinpointClient testPinpointClient = new TestPinpointClient(echoMessageListener, PinpointRPCTestUtils.getParams());
        try {
            testPinpointClient.connect(bindPort);
            testPinpointServerAcceptor.assertAwaitClientConnected(1, 1000);

            PinpointSocket writableServer = testPinpointServerAcceptor.getConnectedPinpointSocketList().get(0);
            assertSendMessage(writableServer, "simple", echoMessageListener);
            assertRequestMessage(writableServer, "request", echoMessageListener);
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void clientMessageListenerTest2() throws InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        TestServerMessageListenerFactory.TestServerMessageListener echoMessageListener1 = testServerMessageListenerFactory.create();
        TestPinpointClient testPinpointClient1 = new TestPinpointClient(echoMessageListener1, PinpointRPCTestUtils.getParams());

        TestServerMessageListenerFactory.TestServerMessageListener echoMessageListener2 = testServerMessageListenerFactory.create();
        TestPinpointClient testPinpointClient2 = new TestPinpointClient(echoMessageListener2, PinpointRPCTestUtils.getParams());

        try {
            testPinpointClient1.connect(bindPort);
            testPinpointClient2.connect(bindPort);
            testPinpointServerAcceptor.assertAwaitClientConnected(2, 1000);

            PinpointSocket writableServer = testPinpointServerAcceptor.getConnectedPinpointSocketList().get(0);
            assertRequestMessage(writableServer, "socket1", null);

            PinpointSocket writableServer2 = testPinpointServerAcceptor.getConnectedPinpointSocketList().get(1);
            assertRequestMessage(writableServer2, "socket2", null);


            echoMessageListener1.awaitAssertExpectedRequestCount(1, 0);
            echoMessageListener2.awaitAssertExpectedRequestCount(1, 0);
        } finally {
            testPinpointClient1.closeAll();
            testPinpointClient2.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    private void assertSendMessage(PinpointSocket writableServer, String message, final EchoClientListener echoMessageListener) throws InterruptedException {
        writableServer.send(message.getBytes());

        awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return echoMessageListener.getSendPacketRepository().size() > 0;
            }
        });

        Assert.assertEquals(message, new String(echoMessageListener.getSendPacketRepository().get(0).getPayload()));
    }

    private void assertRequestMessage(PinpointSocket writableServer, String message, EchoClientListener echoMessageListener) throws InterruptedException {
        byte[] response = PinpointRPCTestUtils.request(writableServer, message.getBytes());
        Assert.assertEquals(message, new String(response));

        if (echoMessageListener != null) {
            Assert.assertEquals(message, new String(echoMessageListener.getRequestPacketRepository().get(0).getPayload()));
        }
    }

}
