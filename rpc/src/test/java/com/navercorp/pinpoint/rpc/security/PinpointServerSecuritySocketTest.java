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

package com.navercorp.pinpoint.rpc.security;

import com.navercorp.pinpoint.rpc.DiscardServerHandler;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import com.navercorp.pinpoint.rpc.TestAwaitTaskUtils;
import com.navercorp.pinpoint.rpc.TestAwaitUtils;
import com.navercorp.pinpoint.rpc.codec.TestCodec;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlConnectionHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlConnectionHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.server.ChannelFilter;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerCodecPipelineFactory;
import com.navercorp.pinpoint.rpc.util.IOUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.jboss.netty.channel.ChannelPipeline;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Taejin Koo
 */
public class PinpointServerSecuritySocketTest {

    private static final String AUTH_KEY = "secret";

    // 1. success authentication,
    // 2. success send & receive
    @Test
    public void authenticationSuccessTest() throws Exception {
        final int sendPingCount = 3;

        int bindPort = SocketUtils.findAvailableTcpPort();

        PinpointServerAcceptor serverAcceptor = null;
        Socket socket = null;
        try {
            AuthenticationManager authenticationManager = MockAuthenticationManager.createServer(AUTH_KEY);
            AuthenticationServerHandler authenticationServerHandler = new AuthenticationServerHandler(authenticationManager);

            serverAcceptor = new PinpointServerAcceptor(ChannelFilter.BYPASS, new TestPipelineFactory(authenticationServerHandler));

            final DiscardServerHandler messageHandler = new DiscardServerHandler();
            serverAcceptor.setMessageHandler(messageHandler);
            serverAcceptor.bind("127.0.0.1", bindPort);

            socket = new Socket("127.0.0.1", bindPort);

            Assert.assertFalse(authenticationServerHandler.isAuthenticated());

            sendHandshake(socket, AUTH_KEY);

            Packet packet = receiveHandshakeResponse(socket);
            Assert.assertTrue(packet instanceof ControlConnectionHandshakeResponsePacket);
            Assert.assertTrue(authenticationServerHandler.isAuthenticated());

            sendPing(socket, sendPingCount);

            boolean success = TestAwaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return messageHandler.getMessageReceivedCount() == sendPingCount;
                }
            }, 10, 500);
            Assert.assertTrue(success);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);

            IOUtils.close(socket);
        }
    }

    // 1. fail authentication,
    // 2. channel close
    // 3. fail send & receive
    @Test
    public void authenticationFailTest() throws Exception {
        final int sendPingCount = 3;

        int bindPort = SocketUtils.findAvailableTcpPort();

        PinpointServerAcceptor serverAcceptor = null;
        Socket socket = null;
        try {
            AuthenticationManager authenticationManager = MockAuthenticationManager.createServer(AUTH_KEY);
            AuthenticationServerHandler authenticationServerHandler = new AuthenticationServerHandler(authenticationManager);

            serverAcceptor = new PinpointServerAcceptor(ChannelFilter.BYPASS, new TestPipelineFactory(authenticationServerHandler));

            final DiscardServerHandler messageHandler = new DiscardServerHandler();
            serverAcceptor.setMessageHandler(messageHandler);
            serverAcceptor.bind("127.0.0.1", bindPort);

            socket = new Socket("127.0.0.1", bindPort);

            sendHandshake(socket, AUTH_KEY + "fail");

            Packet packet = receiveHandshakeResponse(socket);
            Assert.assertTrue(packet instanceof ControlConnectionHandshakeResponsePacket);
            Assert.assertFalse(authenticationServerHandler.isAuthenticated());

            sendPing(socket, sendPingCount);

            boolean success = TestAwaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return messageHandler.getMessageReceivedCount() == sendPingCount;
                }
            }, 10, 500);

            Assert.assertFalse(success);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);

            IOUtils.close(socket);
        }
    }

    // 1. skip authentication,
    // 2. channel close
    // 3. fail send & receive
    @Test(expected = IOException.class)
    public void authenticationFailTest2() throws Exception {
        int bindPort = SocketUtils.findAvailableTcpPort();

        PinpointServerAcceptor serverAcceptor = null;
        Socket socket = null;
        try {
            AuthenticationManager authenticationManager = MockAuthenticationManager.createServer(AUTH_KEY);
            AuthenticationServerHandler authenticationServerHandler = new AuthenticationServerHandler(authenticationManager);

            serverAcceptor = new PinpointServerAcceptor(ChannelFilter.BYPASS, new TestPipelineFactory(authenticationServerHandler));

            final DiscardServerHandler messageHandler = new DiscardServerHandler();
            serverAcceptor.setMessageHandler(messageHandler);
            serverAcceptor.bind("127.0.0.1", bindPort);

            socket = new Socket("127.0.0.1", bindPort);

            sendPing(socket);

            Packet packet = receiveHandshakeResponse(socket);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);

            IOUtils.close(socket);
        }
    }

    private void sendHandshake(Socket socket, String key) throws IOException {
        ControlConnectionHandshakePacket packet = new ControlConnectionHandshakePacket(key.getBytes());
        byte[] payload = TestCodec.encodePacket(packet);
        IOUtils.write(socket.getOutputStream(), payload);
    }

    private Packet receiveHandshakeResponse(Socket socket) throws IOException {
        byte[] payload = IOUtils.read(socket.getInputStream(), 50, 3000);
        return TestCodec.decodePacket(payload);
    }

    private void sendPing(Socket socket) throws ProtocolException, IOException {
        sendPing(socket, 1);
    }

    private void sendPing(Socket socket, int count) throws ProtocolException, IOException {
        for (int i = 0; i < count; i++) {
            PingPayloadPacket packet = new PingPayloadPacket(1, (byte) 0);
            byte[] payload = TestCodec.encodePacket(packet);
            IOUtils.write(socket.getOutputStream(), payload);
        }
    }

    private static class TestPipelineFactory implements PipelineFactory {

        private final AuthenticationServerHandler authenticationHandler;

        private TestPipelineFactory(AuthenticationServerHandler authenticationHandler) {
            this.authenticationHandler = authenticationHandler;
        }

        private final ServerCodecPipelineFactory pipelineFactory = new ServerCodecPipelineFactory();

        @Override
        public ChannelPipeline newPipeline() {
            ChannelPipeline pipeline = pipelineFactory.newPipeline();
            pipeline.addLast("auth", authenticationHandler);
            return pipeline;
        }
    }

}
