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
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils.EchoClientListener;
import com.navercorp.pinpoint.test.client.TestPinpointClient;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.greaterThan;

/**
 * @author Taejin Koo
 */
public class ClientMessageListenerTest {

    private ConditionFactory awaitility() {
        ConditionFactory conditionFactory = Awaitility.await()
                .pollDelay(10, TimeUnit.MILLISECONDS)
                .timeout(1000, TimeUnit.MILLISECONDS);
        return conditionFactory;
    }

    private final TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX);

    @Test
    public void clientMessageListenerTest1() {
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
    public void clientMessageListenerTest2() {
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

    private void assertSendMessage(PinpointSocket writableServer, String message, final EchoClientListener echoMessageListener) {
        writableServer.send(message.getBytes());

        awaitility().until(new Callable<List<SendPacket>>() {
            @Override
            public List<SendPacket> call() {
                return echoMessageListener.getSendPacketRepository();
            }
        }, Matchers.<SendPacket>hasSize(greaterThan(0)));

        Assert.assertEquals(message, new String(echoMessageListener.getSendPacketRepository().get(0).getPayload()));
    }

    private void assertRequestMessage(PinpointSocket writableServer, String message, EchoClientListener echoMessageListener) {
        byte[] response = PinpointRPCTestUtils.request(writableServer, message.getBytes());
        Assert.assertEquals(message, new String(response));

        if (echoMessageListener != null) {
            Assert.assertEquals(message, new String(echoMessageListener.getRequestPacketRepository().get(0).getPayload()));
        }
    }

}
