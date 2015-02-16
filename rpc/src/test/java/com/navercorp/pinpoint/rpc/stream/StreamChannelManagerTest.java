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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.RecordedStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.TestByteUtils;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.client.SimpleLoggingMessageListener;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.TestSeverMessageListener;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;

public class StreamChannelManagerTest {

    private static int bindPort;
    
    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = PinpointRPCTestUtils.findAvailablePort();
    }

    // Client to Server Stream
    @Test
    public void streamSuccessTest1() throws IOException, InterruptedException {
        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointServerAcceptor serverAcceptor = createServerFactory(new TestSeverMessageListener(), new ServerListener(bo));
        serverAcceptor.bind("localhost", bindPort);

        PinpointSocketFactory pinpointSocketFactory = createSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannelContext clientContext = socket.createStreamChannel(new byte[0], clientListener);

            int sendCount = 4;

            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }

            Thread.sleep(100);

            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

            clientContext.getStreamChannel().close();
            
            PinpointRPCTestUtils.close(socket);
        } finally {
            pinpointSocketFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    // Client to Server Stream
    @Test
    public void streamSuccessTest2() throws IOException, InterruptedException {
        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointServerAcceptor serverAcceptor = createServerFactory(new TestSeverMessageListener(), new ServerListener(bo));
        serverAcceptor.bind("localhost", bindPort);

        PinpointSocketFactory pinpointSocketFactory = createSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);
            ClientStreamChannelContext clientContext = socket.createStreamChannel(new byte[0], clientListener);

            RecordedStreamChannelMessageListener clientListener2 = new RecordedStreamChannelMessageListener(4);
            ClientStreamChannelContext clientContext2 = socket.createStreamChannel(new byte[0], clientListener2);


            int sendCount = 4;
            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }

            Thread.sleep(100);

            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());
            Assert.assertEquals(sendCount, clientListener2.getReceivedMessage().size());

            clientContext.getStreamChannel().close();

            Thread.sleep(100);

            sendCount = 4;
            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }

            Thread.sleep(100);

            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());
            Assert.assertEquals(8, clientListener2.getReceivedMessage().size());


            clientContext2.getStreamChannel().close();

            PinpointRPCTestUtils.close(socket);
        } finally {
            pinpointSocketFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void streamSuccessTest3() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = createServerFactory(new TestSeverMessageListener(), null);
        serverAcceptor.bind("localhost", bindPort);

        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointSocketFactory pinpointSocketFactory = createSocketFactory(new TestListener(), new ServerListener(bo));

        try {
            PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", bindPort);

            Thread.sleep(100);

            List<PinpointServer> writableServerList = serverAcceptor.getWritableServerList();
            Assert.assertEquals(1, writableServerList.size());

            PinpointServer writableServer = writableServerList.get(0);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannelContext clientContext = writableServer.createStream(new byte[0], clientListener);

            int sendCount = 4;

            for (int i = 0; i < sendCount; i++) {
                sendRandomBytes(bo);
            }

            Thread.sleep(100);

            Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

            clientContext.getStreamChannel().close();
            
            PinpointRPCTestUtils.close(socket);
        } finally {
            pinpointSocketFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test(expected = PinpointSocketException.class)
    public void streamClosedTest1() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = createServerFactory(new TestSeverMessageListener(), null);
        serverAcceptor.bind("localhost", bindPort);

        PinpointSocketFactory pinpointSocketFactory = createSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannelContext clientContext = socket.createStreamChannel(new byte[0], clientListener);

            Thread.sleep(100);

            clientContext.getStreamChannel().close();
            
            PinpointRPCTestUtils.close(socket);
        } finally {
            pinpointSocketFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void streamClosedTest2() throws IOException, InterruptedException {
        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointServerAcceptor serverAcceptor = createServerFactory(new TestSeverMessageListener(), new ServerListener(bo));
        serverAcceptor.bind("localhost", bindPort);

        PinpointSocketFactory pinpointSocketFactory = createSocketFactory();

        PinpointSocket socket = null;
        try {
            socket = pinpointSocketFactory.connect("127.0.0.1", bindPort);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannelContext clientContext = socket.createStreamChannel(new byte[0], clientListener);
            Thread.sleep(100);

            Assert.assertEquals(1, bo.getStreamChannelContextSize());

            clientContext.getStreamChannel().close();
            Thread.sleep(100);

            Assert.assertEquals(0, bo.getStreamChannelContextSize());

        } finally {
            PinpointRPCTestUtils.close(socket);
            pinpointSocketFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    // ServerSocket to Client Stream


    // ServerStreamChannel first close.
    @Test(expected = PinpointSocketException.class)
    public void streamClosedTest3() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = createServerFactory(new TestSeverMessageListener(), null);
        serverAcceptor.bind("localhost", bindPort);

        SimpleStreamBO bo = new SimpleStreamBO();

        PinpointSocketFactory pinpointSocketFactory = createSocketFactory(new TestListener(), new ServerListener(bo));

        PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", bindPort);
        try {

            Thread.sleep(100);

            List<PinpointServer> writableServerList = serverAcceptor.getWritableServerList();
            Assert.assertEquals(1, writableServerList.size());

            PinpointServer writableServer = writableServerList.get(0);

            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

            ClientStreamChannelContext clientContext = writableServer.createStream(new byte[0], clientListener);


            StreamChannelContext aaa = socket.findStreamChannel(2);

            aaa.getStreamChannel().close();

            sendRandomBytes(bo);

            Thread.sleep(100);


            clientContext.getStreamChannel().close();
        } finally {
            PinpointRPCTestUtils.close(socket);
            pinpointSocketFactory.release();
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

    private PinpointSocketFactory createSocketFactory() {
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        return pinpointSocketFactory;
    }

    private PinpointSocketFactory createSocketFactory(MessageListener messageListener, ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setMessageListener(messageListener);
        pinpointSocketFactory.setServerStreamChannelMessageListener(serverStreamChannelMessageListener);

        return pinpointSocketFactory;
    }

    class TestListener extends SimpleLoggingMessageListener {

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
        public short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
            bo.addServerStreamChannelContext(streamChannelContext);
            return 0;
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
