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

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.RecordedStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.TestByteUtils;
import com.navercorp.pinpoint.rpc.client.SimpleMessageListener;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.test.client.TestPinpointClient;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StreamChannelManagerTest {

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(10, 1000);
    private final TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX);

    // Client to Server Stream
    @Test
    public void streamSuccessTest1() throws IOException, InterruptedException, StreamException {
        SimpleStreamBO bo = new SimpleStreamBO();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory, new ServerListener(bo));
        int bindPort = testPinpointServerAcceptor.bind();

        TestPinpointClient testPinpointClient = new TestPinpointClient();
        try {
            testPinpointClient.connect(bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannel clientStreamChannel = testPinpointClient.openStream(new byte[0], clientListener);

            int sendCount = 4;
            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }
            clientListener.getLatch().await();

            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

            clientStreamChannel.close();
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    // Client to Server Stream
    @Test
    public void streamSuccessTest2() throws IOException, InterruptedException, StreamException {
        SimpleStreamBO bo = new SimpleStreamBO();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory, new ServerListener(bo));
        int bindPort = testPinpointServerAcceptor.bind();

        TestPinpointClient testPinpointClient = new TestPinpointClient();
        try {
            testPinpointClient.connect(bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);
            ClientStreamChannel clientStreamChannel = testPinpointClient.openStream(new byte[0], clientListener);

            RecordedStreamChannelMessageListener clientListener2 = new RecordedStreamChannelMessageListener(8);
            ClientStreamChannel clientStreamChannel2 = testPinpointClient.openStream(new byte[0], clientListener2);

            int sendCount = 4;
            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }

            clientListener.getLatch().await();
            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

            clientStreamChannel.close();

            sendCount = 4;
            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }
            clientListener2.getLatch().await();

            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());
            Assert.assertEquals(8, clientListener2.getReceivedMessage().size());

            clientStreamChannel2.close();
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void streamSuccessTest3() throws IOException, InterruptedException, StreamException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        SimpleStreamBO bo = new SimpleStreamBO();

        TestPinpointClient testPinpointClient = new TestPinpointClient(SimpleMessageListener.INSTANCE, new ServerListener(bo));
        try {
            testPinpointClient.connect(bindPort);
            testPinpointServerAcceptor.assertAwaitClientConnected(1000);

            List<PinpointSocket> writableServerList = testPinpointServerAcceptor.getConnectedPinpointSocketList();
            Assert.assertEquals(1, writableServerList.size());

            PinpointSocket writableServer = writableServerList.get(0);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            if (writableServer instanceof  PinpointServer) {
                ClientStreamChannel clientStreamChannel = ((PinpointServer)writableServer).openStream(new byte[0], clientListener);

                int sendCount = 4;
                for (int i = 0; i < sendCount; i++) {
                    sendRandomBytes(bo);
                }
                clientListener.getLatch().await();

                Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

                clientStreamChannel.close();
            } else {
                Assert.fail();
            }
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    @Test(expected = StreamException.class)
    public void streamClosedTest1() throws IOException, InterruptedException, StreamException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        TestPinpointClient testPinpointClient = new TestPinpointClient();
        try {
            testPinpointClient.connect(bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            testPinpointClient.openStream(new byte[0], clientListener);
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void streamClosedTest2() throws IOException, InterruptedException, StreamException {
        final SimpleStreamBO bo = new SimpleStreamBO();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory, new ServerListener(bo));
        int bindPort = testPinpointServerAcceptor.bind();

        TestPinpointClient testPinpointClient = new TestPinpointClient();
        try {
            testPinpointClient.connect(bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannel clientStreamChannel = testPinpointClient.openStream(new byte[0], clientListener);
            Assert.assertEquals(1, bo.getStreamChannelContextSize());

            clientStreamChannel.close();

            awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return bo.getStreamChannelContextSize() == 0;
                }
            });

            Assert.assertEquals(0, bo.getStreamChannelContextSize());
        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    // ServerSocket to Client Stream

    // ServerStreamChannel first close.
    @Test(expected = PinpointSocketException.class)
    public void streamClosedTest3() throws IOException, InterruptedException, StreamException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        SimpleStreamBO bo = new SimpleStreamBO();

        ServerListener serverStreamChannelMessageHandler = new ServerListener(bo);
        TestPinpointClient testPinpointClient = new TestPinpointClient(SimpleMessageListener.INSTANCE, serverStreamChannelMessageHandler);
        testPinpointClient.connect(bindPort);
        try {
            testPinpointServerAcceptor.assertAwaitClientConnected(1000);

            List<PinpointSocket> writableServerList = testPinpointServerAcceptor.getConnectedPinpointSocketList();
            Assert.assertEquals(1, writableServerList.size());

            PinpointSocket writableServer = writableServerList.get(0);

            if (writableServer instanceof  PinpointServer) {
                RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

                ClientStreamChannel clientStreamChannel = ((PinpointServer)writableServer).openStream(new byte[0], clientListener);

                StreamChannel streamChannel = serverStreamChannelMessageHandler.bo.serverStreamChannelList.get(0);

                streamChannel.close();

                sendRandomBytes(bo);

                Thread.sleep(100);

                clientStreamChannel.close();
            } else {
                Assert.fail();
            }

        } finally {
            testPinpointClient.closeAll();
            testPinpointServerAcceptor.close();
        }
    }

    private void sendRandomBytes(SimpleStreamBO bo) {
        byte[] openBytes = TestByteUtils.createRandomByte(30);
        bo.sendResponse(openBytes);
    }

    class ServerListener extends ServerStreamChannelMessageHandler {

        private final SimpleStreamBO bo;

        public ServerListener(SimpleStreamBO bo) {
            this.bo = bo;
        }

        @Override
        public StreamCode handleStreamCreatePacket(ServerStreamChannel streamChannel, StreamCreatePacket packet) {
            bo.addServerStreamChannelContext(streamChannel);
            return StreamCode.OK;
        }

        @Override
        public void handleStreamClosePacket(ServerStreamChannel streamChannel, StreamClosePacket packet) {
            bo.removeServerStreamChannelContext(streamChannel);
        }

    }

    class SimpleStreamBO {

        private final List<ServerStreamChannel> serverStreamChannelList;

        public SimpleStreamBO() {
            serverStreamChannelList = new CopyOnWriteArrayList<ServerStreamChannel>();
        }

        public void addServerStreamChannelContext(ServerStreamChannel serverStreamChannel) {
            serverStreamChannelList.add(serverStreamChannel);
        }

        public void removeServerStreamChannelContext(ServerStreamChannel serverStreamChannel) {
            serverStreamChannelList.remove(serverStreamChannel);
        }

        void sendResponse(byte[] data) {

            for (ServerStreamChannel serverStreamChannel : serverStreamChannelList) {
                serverStreamChannel.sendData(data);
            }
        }

        int getStreamChannelContextSize() {
            return serverStreamChannelList.size();
        }

        public List<ServerStreamChannel> getServerStreamChannelList() {
            return serverStreamChannelList;
        }
    }

}
