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

import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.test.client.TestRawSocket;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author koo.taejin
 */
public class ControlPacketServerTest {

    // Test for being possible to send messages in case of failure of registering packet ( return code : 2, lack of parameter)
    @Test
    public void registerAgentTest1() throws Exception {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new HandshakeVerifyMessageListenerFactory());
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);

            sendAndReceiveSimplePacket(testRawSocket);

            int code= sendAndReceiveRegisterPacket(testRawSocket);
            Assert.assertEquals(2, code);

            sendAndReceiveSimplePacket(testRawSocket);
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }
    }

    // Test for being possible to send messages in case of success of registering packet ( return code : 0)
    @Test
    public void registerAgentTest2() throws Exception {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new HandshakeVerifyMessageListenerFactory());
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);

            sendAndReceiveSimplePacket(testRawSocket);

            int code= sendAndReceiveRegisterPacket(testRawSocket, PinpointRPCTestUtils.getParams());
            Assert.assertEquals(0, code);

            sendAndReceiveSimplePacket(testRawSocket);
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }
    }

    // when failure of registering and retrying to register, confirm to return same code ( return code : 2
    @Test
    public void registerAgentTest3() throws Exception {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new HandshakeVerifyMessageListenerFactory());
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);
            int code = sendAndReceiveRegisterPacket(testRawSocket);
            Assert.assertEquals(2, code);

            code = sendAndReceiveRegisterPacket(testRawSocket);
            Assert.assertEquals(2, code);

            sendAndReceiveSimplePacket(testRawSocket);
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }
    }

    // after success of registering, when success message are sent repeatedly.
    // test 1) confirm to return success code, 2) confirm to return already success code.
    @Test
    public void registerAgentTest4() throws Exception {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new HandshakeVerifyMessageListenerFactory());
        int bindPort = testPinpointServerAcceptor.bind();

        TestRawSocket testRawSocket = new TestRawSocket();
        try {
            testRawSocket.connect(bindPort);

            sendAndReceiveSimplePacket(testRawSocket);

            int code = sendAndReceiveRegisterPacket(testRawSocket, PinpointRPCTestUtils.getParams());
            Assert.assertEquals(0, code);

            sendAndReceiveSimplePacket(testRawSocket);

            code = sendAndReceiveRegisterPacket(testRawSocket, PinpointRPCTestUtils.getParams());
            Assert.assertEquals(1, code);

            sendAndReceiveSimplePacket(testRawSocket);
        } finally {
            testRawSocket.close();
            testPinpointServerAcceptor.close();
        }
    }

    private int sendAndReceiveRegisterPacket(TestRawSocket testRawSocket) throws ProtocolException, IOException {
        return sendAndReceiveRegisterPacket(testRawSocket, Collections.<String, Object>emptyMap());
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

}
