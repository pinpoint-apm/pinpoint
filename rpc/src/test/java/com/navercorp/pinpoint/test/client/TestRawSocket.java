/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test.client;

import com.navercorp.pinpoint.rpc.codec.TestCodec;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.PongPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.IOUtils;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class TestRawSocket {

    private final Socket socket;

    public TestRawSocket() {
        socket = new Socket();
    }

    public void connect(int port) throws IOException {
        socket.connect(new InetSocketAddress(TestPinpointServerAcceptor.LOCALHOST, port));
    }


    public void sendPingPacket(Packet pingPacket) throws ProtocolException, IOException {
        byte[] payload = TestCodec.encodePacket(pingPacket);
        IOUtils.write(socket.getOutputStream(), payload);
    }

    public PongPacket readPongPacket(long maxWaitTIme) throws ProtocolException, IOException {
        byte[] payload = IOUtils.read(socket.getInputStream(), 50, maxWaitTIme);
        return (PongPacket) TestCodec.decodePacket(payload);
    }

    public void sendRequestPacket() throws ProtocolException, IOException {
        byte[] packet = TestCodec.encodePacket(new RequestPacket(10, new byte[0]));
        IOUtils.write(socket.getOutputStream(), packet);
    }

    public ResponsePacket readResponsePacket(long maxWaitTime) throws ProtocolException, IOException {
        byte[] payload = IOUtils.read(socket.getInputStream(), 50, maxWaitTime);
        return (ResponsePacket) TestCodec.decodePacket(payload);
    }

    public void sendHandshakePacket(Map<String, Object> properties) throws ProtocolException, IOException {
        byte[] payload = ControlMessageEncodingUtils.encode(properties);
        byte[] packet = TestCodec.encodePacket(new ControlHandshakePacket(1, payload));
        IOUtils.write(socket.getOutputStream(), packet);
    }

    public Map<Object, Object>  readHandshakeResponseData(long maxWaitTime) throws ProtocolException, IOException {
        byte[] payload = IOUtils.read(socket.getInputStream(), 50, maxWaitTime);
        ControlHandshakeResponsePacket responsePacket = (ControlHandshakeResponsePacket) TestCodec.decodePacket(payload);
        Map<Object, Object> result = (Map<Object, Object>) ControlMessageEncodingUtils.decode(responsePacket.getPayload());
        return result;
    }

    public void close() {
        IOUtils.closeQuietly(socket);
    }

}
