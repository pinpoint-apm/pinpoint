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
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.IOUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

/**
 * @author koo.taejin
 */
public class ControlPacketServerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static int bindPort;
    
    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
    }

    // Test for being possible to send messages in case of failure of registering packet ( return code : 2, lack of parameter)
    @Test
    public void registerAgentTest1() throws Exception {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new SimpleListener());

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);

            sendAndReceiveSimplePacket(socket);

            int code= sendAndReceiveRegisterPacket(socket);
            Assert.assertEquals(2, code);

            sendAndReceiveSimplePacket(socket);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    // Test for being possible to send messages in case of success of registering packet ( return code : 0)
    @Test
    public void registerAgentTest2() throws Exception {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new SimpleListener());

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);

            sendAndReceiveSimplePacket(socket);

            int code= sendAndReceiveRegisterPacket(socket, PinpointRPCTestUtils.getParams());
            Assert.assertEquals(0, code);

            sendAndReceiveSimplePacket(socket);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    // when failure of registering and retrying to register, confirm to return same code ( return code : 2
    @Test
    public void registerAgentTest3() throws Exception {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new SimpleListener());

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            int code = sendAndReceiveRegisterPacket(socket);
            Assert.assertEquals(2, code);

            code = sendAndReceiveRegisterPacket(socket);
            Assert.assertEquals(2, code);

            sendAndReceiveSimplePacket(socket);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    // after success of registering, when success message are sent repeatedly.
    // test 1) confirm to return success code, 2) confirm to return already success code.
    @Test
    public void registerAgentTest4() throws Exception {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new SimpleListener());

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", bindPort);
            sendAndReceiveSimplePacket(socket);

            int code = sendAndReceiveRegisterPacket(socket, PinpointRPCTestUtils.getParams());
            Assert.assertEquals(0, code);

            sendAndReceiveSimplePacket(socket);

            code = sendAndReceiveRegisterPacket(socket, PinpointRPCTestUtils.getParams());
            Assert.assertEquals(1, code);

            sendAndReceiveSimplePacket(socket);
        } finally {
            IOUtils.close(socket);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }


    private int sendAndReceiveRegisterPacket(Socket socket) throws ProtocolException, IOException {
        return sendAndReceiveRegisterPacket(socket, Collections.<String, Object>emptyMap());
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
        ControlHandshakePacket packet = new ControlHandshakePacket(1, payload);

        ByteBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffer().writerIndex());
        IOUtils.write(outputStream, bb.array());
    }

    private void sendSimpleRequestPacket(OutputStream outputStream) throws ProtocolException, IOException {
        RequestPacket packet = new RequestPacket(new byte[0]);
        packet.setRequestId(10);

        ByteBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffer().writerIndex());
        IOUtils.write(outputStream, bb.array());
    }

    private ControlHandshakeResponsePacket receiveRegisterConfirmPacket(InputStream inputStream) throws ProtocolException, IOException {

        byte[] payload = IOUtils.read(inputStream, 50, 3000);
        ChannelBuffer cb = ChannelBuffers.wrappedBuffer(payload);

        short packetType = cb.readShort();

        ControlHandshakeResponsePacket packet = ControlHandshakeResponsePacket.readBuffer(packetType, cb);
        return packet;
    }

    private ResponsePacket readSimpleResponsePacket(InputStream inputStream) throws ProtocolException, IOException {
        byte[] payload = IOUtils.read(inputStream, 50, 3000);
        ChannelBuffer cb = ChannelBuffers.wrappedBuffer(payload);

        short packetType = cb.readShort();

        ResponsePacket packet = ResponsePacket.readBuffer(packetType, cb);
        return packet;
    }

    class SimpleListener implements ServerMessageListener {

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            logger.debug("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            logger.debug("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());
            pinpointSocket.response(requestPacket, requestPacket.getPayload());
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            if (properties == null) {
                return HandshakeResponseType.ProtocolError.PROTOCOL_ERROR;
            }

            boolean hasRequiredKeys = HandshakePropertyType.hasRequiredKeys(properties);
            if (!hasRequiredKeys) {
                return HandshakeResponseType.PropertyError.PROPERTY_ERROR;
            }

            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
        }

    }

}
