package com.nhn.pinpoint.rpc.stream;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Assert;

import org.junit.Test;

import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.RecordedStreamChannelMessageListener;
import com.nhn.pinpoint.rpc.TestByteUtils;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.client.SimpleLoggingMessageListener;
import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.TestSeverMessageListener;

public class StreamChannelManagerTest {

	// Client to Server Stream
	@Test
	public void stream1() throws IOException, InterruptedException {
		SimpleStreamBO bo = new SimpleStreamBO();

		PinpointServerSocket ss = createServerSocket(new TestSeverMessageListener(), new ServerListener(bo));
		ss.bind("localhost", 10234);

		PinpointSocketFactory pinpointSocketFactory = createSocketFactory();
		try {
			PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", 10234);

			RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

			ClientStreamChannelContext clientContext = socket.createStreamChannel(new byte[0], clientListener);

			int sendCount = 4;

			for (int i = 0; i < 4; i++) {
				sendRandomBytes(bo);
			}

			Thread.sleep(100);

			Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

			clientContext.getStreamChannel().close();
			socket.close();
		} finally {
			pinpointSocketFactory.release();
			ss.close();
		}
	}

	@Test(expected = PinpointSocketException.class)
	public void stream2() throws IOException, InterruptedException {
		PinpointServerSocket ss = createServerSocket(new TestSeverMessageListener(), null);
		ss.bind("localhost", 10234);

		PinpointSocketFactory pinpointSocketFactory = createSocketFactory();
		try {
			PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", 10234);

			RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

			ClientStreamChannelContext clientContext = socket.createStreamChannel(new byte[0], clientListener);

			Thread.sleep(100);

			clientContext.getStreamChannel().close();
			socket.close();
		} finally {
			pinpointSocketFactory.release();
			ss.close();
		}
	}

	@Test(expected = PinpointSocketException.class)
	public void stream3() throws IOException, InterruptedException {
		SimpleStreamBO bo = new SimpleStreamBO();

		PinpointServerSocket ss = createServerSocket(new TestSeverMessageListener(), new ServerListener(bo));
		ss.bind("localhost", 10234);

		PinpointSocketFactory pinpointSocketFactory = createSocketFactory();

		PinpointSocket socket = null;
		try {
			socket = pinpointSocketFactory.connect("127.0.0.1", 10234);

			RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

			ClientStreamChannelContext clientContext = socket.createStreamChannel(new byte[0], clientListener);
			Thread.sleep(100);

			clientContext.getStreamChannel().close();

			Thread.sleep(100);

			sendRandomBytes(bo);

		} finally {
			if (socket != null) {
				socket.close();
			}

			pinpointSocketFactory.release();
			ss.close();
		}
	}

	// ServerSocket to Client Stream
	@Test
	public void stream4() throws IOException, InterruptedException {
		PinpointServerSocket ss = createServerSocket(new TestSeverMessageListener(), null);
		ss.bind("localhost", 10234);

		SimpleStreamBO bo = new SimpleStreamBO();

		PinpointSocketFactory pinpointSocketFactory = createSocketFactory(new TestListener(), new ServerListener(bo));

		try {
			PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", 10234);

			Thread.sleep(100);

			List<ChannelContext> contextList = ss.getDuplexCommunicationChannelContext();
			Assert.assertEquals(1, contextList.size());

			ChannelContext context = contextList.get(0);

			RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(4);

			ClientStreamChannelContext clientContext = context.createStreamChannel(new byte[0], clientListener);

			int sendCount = 4;

			for (int i = 0; i < 4; i++) {
				sendRandomBytes(bo);
			}

			Thread.sleep(100);

			Assert.assertEquals(sendCount, clientListener.getReceivedMessage().size());

			clientContext.getStreamChannel().close();
			socket.close();
		} finally {
			pinpointSocketFactory.release();
			ss.close();
		}
	}

	private PinpointServerSocket createServerSocket(ServerMessageListener severMessageListener,
			ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
		PinpointServerSocket serverSocket = new PinpointServerSocket();

		if (severMessageListener != null) {
			serverSocket.setMessageListener(severMessageListener);
		}

		if (serverStreamChannelMessageListener != null) {
			serverSocket.setServerStreamChannelMessageListener(serverStreamChannelMessageListener);
		}

		return serverSocket;
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
		public void handleStreamClose(StreamChannelContext streamChannelContext, StreamClosePacket packet) {

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

	}

}
