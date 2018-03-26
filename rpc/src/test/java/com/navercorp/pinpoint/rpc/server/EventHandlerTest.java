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

import com.navercorp.pinpoint.rpc.codec.TestCodec;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.IOUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

/**
 * @author koo.taejin
 */
public class EventHandlerTest {

    private static int bindPort;

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
        serverAcceptor.setMessageListener(SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE);
        serverAcceptor.bind("127.0.0.1", bindPort);

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            sendAndReceiveSimplePacket(socket);
            Assert.assertEquals(eventHandler.getCode(), SocketStateCode.RUN_WITHOUT_HANDSHAKE);

            int code = sendAndReceiveRegisterPacket(socket, PinpointRPCTestUtils.getParams());
            Assert.assertEquals(eventHandler.getCode(), SocketStateCode.RUN_DUPLEX);

            sendAndReceiveSimplePacket(socket);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void registerAgentFailTest() throws Exception {
        ThrowExceptionEventHandler eventHandler = new ThrowExceptionEventHandler();

        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.addStateChangeEventHandler(eventHandler);
        serverAcceptor.setMessageListener(SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE);
        serverAcceptor.bind("127.0.0.1", bindPort);

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            sendAndReceiveSimplePacket(socket);

            Assert.assertTrue(eventHandler.getErrorCount() > 0);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    private int sendAndReceiveRegisterPacket(Socket socket, Map<String, Object> properties) throws ProtocolException, IOException {
        sendRegisterPacket(socket.getOutputStream(), properties);
        ControlHandshakeResponsePacket packet = receiveRegisterConfirmPacket(socket.getInputStream());
        Map<Object, Object> result = (Map<Object, Object>) ControlMessageEncodingUtils.decode(packet.getPayload());

        return MapUtils.getInteger(result, "code", -1);
    }

    private void sendAndReceiveSimplePacket(Socket socket) throws ProtocolException, IOException {
        sendSimpleRequestPacket(socket.getOutputStream());
        ResponsePacket responsePacket = readSimpleResponsePacket(socket.getInputStream());
        Assert.assertNotNull(responsePacket);
    }

    private void sendRegisterPacket(OutputStream outputStream, Map<String, Object> properties) throws ProtocolException, IOException {
        byte[] payload = ControlMessageEncodingUtils.encode(properties);

        byte[] packet = TestCodec.encodePacket(new ControlHandshakePacket(1, payload));
        IOUtils.write(outputStream, packet);
    }

    private void sendSimpleRequestPacket(OutputStream outputStream) throws ProtocolException, IOException {
        byte[] packet = TestCodec.encodePacket(new RequestPacket(10, new byte[0]));
        IOUtils.write(outputStream, packet);
    }

    private ControlHandshakeResponsePacket receiveRegisterConfirmPacket(InputStream inputStream) throws ProtocolException, IOException {
        byte[] payload = IOUtils.read(inputStream, 50, 3000);
        return (ControlHandshakeResponsePacket) TestCodec.decodePacket(payload);
    }

    private ResponsePacket readSimpleResponsePacket(InputStream inputStream) throws ProtocolException, IOException {
        byte[] payload = IOUtils.read(inputStream, 50, 3000);
        return (ResponsePacket) TestCodec.decodePacket(payload);
    }

    class EventHandler implements ServerStateChangeEventHandler {

        private SocketStateCode code;

        @Override
        public void eventPerformed(PinpointServer pinpointServer, SocketStateCode stateCode) {
            this.code = stateCode;
        }

        @Override
        public void exceptionCaught(PinpointServer pinpointServer, SocketStateCode stateCode, Throwable e) {
        }

        public SocketStateCode getCode() {
            return code;
        }
    }

    class ThrowExceptionEventHandler implements ServerStateChangeEventHandler {

        private int errorCount = 0;

        @Override
        public void eventPerformed(PinpointServer pinpointServer, SocketStateCode stateCode) throws Exception {
            throw new Exception("always error.");
        }

        @Override
        public void exceptionCaught(PinpointServer pinpointServer, SocketStateCode stateCode, Throwable e) {
            errorCount++;
        }

        public int getErrorCount() {
            return errorCount;
        }

    }

}
