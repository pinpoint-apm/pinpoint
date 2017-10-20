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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.*;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.SimpleMessageListener;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.SimpleServerMessageListener;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StreamChannelManagerTest {

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(10, 1000);

    private static int bindPort;

    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
    }

    // Client to Server Stream
    @Test
    public void streamSuccessTest1() throws IOException, InterruptedException {
        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointServerAcceptor serverAcceptor = createServerFactory(SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE, new ServerListener(bo));
        serverAcceptor.bind("localhost", bindPort);

        PinpointClientFactory clientFactory = createSocketFactory();
        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannelContext clientContext = client.openStream(new byte[0], clientListener);

            int sendCount = 4;
            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }
            clientListener.getLatch().await();

            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

            clientContext.getStreamChannel().close();
            
            PinpointRPCTestUtils.close(client);
        } finally {
            clientFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    // Client to Server Stream
    @Test
    public void streamSuccessTest2() throws IOException, InterruptedException {
        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointServerAcceptor serverAcceptor = createServerFactory(SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE, new ServerListener(bo));
        serverAcceptor.bind("localhost", bindPort);

        PinpointClientFactory clientFactory = createSocketFactory();
        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);
            ClientStreamChannelContext clientContext = client.openStream(new byte[0], clientListener);

            RecordedStreamChannelMessageListener clientListener2 = new RecordedStreamChannelMessageListener(8);
            ClientStreamChannelContext clientContext2 = client.openStream(new byte[0], clientListener2);

            int sendCount = 4;
            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }

            clientListener.getLatch().await();
            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

            clientContext.getStreamChannel().close();

            sendCount = 4;
            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }
            clientListener2.getLatch().await();

            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());
            Assert.assertEquals(8, clientListener2.getReceivedMessage().size());

            clientContext2.getStreamChannel().close();

            PinpointRPCTestUtils.close(client);
        } finally {
            clientFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void streamSuccessTest3() throws IOException, InterruptedException {
        final PinpointServerAcceptor serverAcceptor = createServerFactory(SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE, null);
        serverAcceptor.bind("localhost", bindPort);

        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointClientFactory clientFactory = createSocketFactory(SimpleMessageListener.INSTANCE, new ServerListener(bo));

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return CollectionUtils.hasLength(serverAcceptor.getWritableSocketList());
                }
            });

            List<PinpointSocket> writableServerList = serverAcceptor.getWritableSocketList();
            Assert.assertEquals(1, writableServerList.size());

            PinpointSocket writableServer = writableServerList.get(0);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            if (writableServer instanceof  PinpointServer) {
                ClientStreamChannelContext clientContext = ((PinpointServer)writableServer).openStream(new byte[0], clientListener);

                int sendCount = 4;
                for (int i = 0; i < sendCount; i++) {
                    sendRandomBytes(bo);
                }
                clientListener.getLatch().await();

                Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

                clientContext.getStreamChannel().close();
            } else {
                Assert.fail();
            }

            PinpointRPCTestUtils.close(client);
        } finally {
            clientFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void streamClosedTest1() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = createServerFactory(SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE, null);
        serverAcceptor.bind("localhost", bindPort);

        PinpointClientFactory clientFactory = createSocketFactory();
        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannelContext clientContext = client.openStream(new byte[0], clientListener);
            StreamCreateFailPacket createFailPacket = clientContext.getCreateFailPacket();
            if (createFailPacket == null) {
                Assert.fail();
            }

            clientContext.getStreamChannel().close();
            
            PinpointRPCTestUtils.close(client);
        } finally {
            clientFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void streamClosedTest2() throws IOException, InterruptedException {
        final SimpleStreamBO bo = new SimpleStreamBO();

        final PinpointServerAcceptor serverAcceptor = createServerFactory(SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE, new ServerListener(bo));
        serverAcceptor.bind("localhost", bindPort);

        PinpointClientFactory clientFactory = createSocketFactory();

        PinpointClient client = null;
        try {
            client = clientFactory.connect("127.0.0.1", bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannelContext clientContext = client.openStream(new byte[0], clientListener);
            Assert.assertEquals(1, bo.getStreamChannelContextSize());

            clientContext.getStreamChannel().close();

            awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return bo.getStreamChannelContextSize() == 0;
                }
            });

            Assert.assertEquals(0, bo.getStreamChannelContextSize());
        } finally {
            PinpointRPCTestUtils.close(client);
            clientFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    // ServerSocket to Client Stream

    // ServerStreamChannel first close.
    @Test(expected = PinpointSocketException.class)
    public void streamClosedTest3() throws IOException, InterruptedException {
        final PinpointServerAcceptor serverAcceptor = createServerFactory(SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE, null);
        serverAcceptor.bind("localhost", bindPort);

        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointClientFactory clientFactory = createSocketFactory(SimpleMessageListener.INSTANCE, new ServerListener(bo));

        PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);
        try {

            awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return CollectionUtils.hasLength(serverAcceptor.getWritableSocketList());
                }
            });

            List<PinpointSocket> writableServerList = serverAcceptor.getWritableSocketList();
            Assert.assertEquals(1, writableServerList.size());

            PinpointSocket writableServer = writableServerList.get(0);

            if (writableServer instanceof  PinpointServer) {
                RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

                ClientStreamChannelContext clientContext = ((PinpointServer)writableServer).openStream(new byte[0], clientListener);

                StreamChannelContext streamChannelContext = client.findStreamChannel(2);

                streamChannelContext.getStreamChannel().close();

                sendRandomBytes(bo);

                Thread.sleep(100);

                clientContext.getStreamChannel().close();
            } else {
                Assert.fail();
            }

        } finally {
            PinpointRPCTestUtils.close(client);
            clientFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }


    private PinpointServerAcceptor createServerFactory(ServerMessageListener severMessageListener, ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();

        if (severMessageListener != null) {
            serverAcceptor.setMessageListener(severMessageListener);
        }

        if (serverStreamChannelMessageListener != null) {
            serverAcceptor.setServerStreamChannelMessageListener(serverStreamChannelMessageListener);
        }

        return serverAcceptor;
    }

    private PinpointClientFactory createSocketFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        return clientFactory;
    }

    private PinpointClientFactory createSocketFactory(MessageListener messageListener, ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setMessageListener(messageListener);
        clientFactory.setServerStreamChannelMessageListener(serverStreamChannelMessageListener);

        return clientFactory;
    }

    private void sendRandomBytes(SimpleStreamBO bo) {
        byte[] openBytes = TestByteUtils.createRandomByte(30);
        bo.sendResponse(openBytes);
    }

    class ServerListener implements ServerStreamChannelMessageListener {

        private final SimpleStreamBO bo;

        public ServerListener(SimpleStreamBO bo) {
            this.bo = bo;
        }

        @Override
        public StreamCode handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
            bo.addServerStreamChannelContext(streamChannelContext);
            return StreamCode.OK;
        }

        @Override
        public void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet) {
            bo.removeServerStreamChannelContext(streamChannelContext);
        }

    }

    class SimpleStreamBO {

        private final List<ServerStreamChannelContext> serverStreamChannelContextList;

        public SimpleStreamBO() {
            serverStreamChannelContextList = new CopyOnWriteArrayList<ServerStreamChannelContext>();
        }

        public void addServerStreamChannelContext(ServerStreamChannelContext context) {
            serverStreamChannelContextList.add(context);
        }

        public void removeServerStreamChannelContext(ServerStreamChannelContext context) {
            serverStreamChannelContextList.remove(context);
        }

        void sendResponse(byte[] data) {

            for (ServerStreamChannelContext context : serverStreamChannelContextList) {
                context.getStreamChannel().sendData(data);
            }
        }

        int getStreamChannelContextSize() {
            return serverStreamChannelContextList.size();
        }
    }

}
