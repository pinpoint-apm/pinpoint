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

import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.test.client.TestRawSocket;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author koo.taejin
 */
public class EventHandlerTest {

    private static int bindPort;
    private final TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX);

    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
    }

    // Test for being possible to send messages in case of failure of registering packet ( return code : 2, lack of parameter)
    @Test
    public void registerAgentSuccessTest() throws Exception {
        EventHandler eventHandler = new EventHandler();

        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.addStateChangeEventHandler(eventHandler);
        serverAcceptor.setMessageListenerFactory(testServerMessageListenerFactory);
        serverAcceptor.bind("127.0.0.1", bindPort);

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);

            sendAndReceiveSimplePacket(testRawSocket);
            Assert.assertEquals(eventHandler.getCode(), SocketStateCode.RUN_WITHOUT_HANDSHAKE);

            int code = sendAndReceiveRegisterPacket(testRawSocket, PinpointRPCTestUtils.getParams());
            Assert.assertEquals(eventHandler.getCode(), SocketStateCode.RUN_DUPLEX);

            sendAndReceiveSimplePacket(testRawSocket);
        } finally {
            testRawSocket.close();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void registerAgentFailTest() throws Exception {
        ThrowExceptionEventHandler eventHandler = new ThrowExceptionEventHandler();

        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.addStateChangeEventHandler(eventHandler);
        serverAcceptor.setMessageListenerFactory(testServerMessageListenerFactory);
        serverAcceptor.bind("127.0.0.1", bindPort);

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);

            sendAndReceiveSimplePacket(testRawSocket);

            Assert.assertTrue(eventHandler.getErrorCount() > 0);
        } finally {
            testRawSocket.close();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    private int sendAndReceiveRegisterPacket(TestRawSocket testRawSocket, Map<String, Object> properties) throws ProtocolException, IOException {
        testRawSocket.sendHandshakePacket(properties);
        Map<Object, Object> responseData = testRawSocket.readHandshakeResponseData(3000);
        return MapUtils.getInteger(responseData, "code", -1);
    }

    private void sendAndReceiveSimplePacket(TestRawSocket testRawSocket) throws ProtocolException, IOException {
        testRawSocket.sendRequestPacket();
        Assert.assertNotNull(testRawSocket.readResponsePacket(3000));
    }

    class EventHandler extends ServerStateChangeEventHandler {

        private SocketStateCode code;

        @Override
        public void stateUpdated(PinpointServer pinpointSocket, SocketStateCode updatedStateCode) {
            this.code = updatedStateCode;
        }

        public SocketStateCode getCode() {
            return code;
        }
    }

    class ThrowExceptionEventHandler extends ServerStateChangeEventHandler {

        private int errorCount = 0;

        @Override
        public void stateUpdated(PinpointServer pinpointSocket, SocketStateCode updatedStateCode) throws Exception {
            try {
                throw new Exception("always error.");
            } catch (Exception e) {
                errorCount++;
                throw new Exception("always error.");
            }
        }

        public int getErrorCount() {
            return errorCount;
        }

    }

}
