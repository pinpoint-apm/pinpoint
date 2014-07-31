package com.nhn.pinpoint.rpc.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.client.SimpleLoggingMessageListener;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;

public class MessageListenerTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void serverMessageListenerTest1() throws InterruptedException {
		PinpointServerSocket ss = new PinpointServerSocket();
		ss.bind("127.0.0.1", 10234);

		PinpointSocketFactory socketFactory = createPinpointSocketFactory();
		try {

			// 리스터를 등록한 것만 RegisterAgent 로 나옴
			PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234, SimpleLoggingMessageListener.LISTENER);
			PinpointSocket socket2 = socketFactory.connect("127.0.0.1", 10234);

			Thread.sleep(500);

			List<ChannelContext> channelContextList = ss.getRegisterAgentChannelContext();
			if (channelContextList.size() != 1) {
				Assert.fail();
			}

			socket.close();
			socket2.close();
		} finally {
			socketFactory.release();
			ss.close();
		}
	}

	@Test
	public void serverMessageListenerTest2() throws InterruptedException {
		PinpointServerSocket ss = new PinpointServerSocket();
		ss.bind("127.0.0.1", 10234);

		PinpointSocketFactory socketFactory = createPinpointSocketFactory();

		try {

			EchoMessageListener echoMessageListener = new EchoMessageListener();
			
			// 리스터를 등록한 것만 RegisterAgent 로 나옴
			PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234, echoMessageListener);
			Thread.sleep(500);

			List<ChannelContext> channelContextList = ss.getRegisterAgentChannelContext();
			if (channelContextList.size() != 1) {
				Assert.fail();
			}

			ChannelContext channelContext = channelContextList.get(0);
			
			channelContext.getSocketChannel().sendMessage("simple".getBytes());
			Thread.sleep(100);
			
			Assert.assertEquals("simple", new String(echoMessageListener.getSendPacketRepository().get(0).getPayload()));
			
			Future<ResponseMessage> future = channelContext.getSocketChannel().sendRequestMessage("request".getBytes());
			future.await();
			ResponseMessage message = future.getResult();
			Assert.assertEquals("request", new String(message.getMessage()));
			Assert.assertEquals("request", new String(echoMessageListener.getRequestPacketRepository().get(0).getPayload()));
			
			socket.close();
		} finally {
			socketFactory.release();
			ss.close();
		}
	}

	@Test
	public void serverMessageListenerTest3() throws InterruptedException {
		PinpointServerSocket ss = new PinpointServerSocket();
		ss.bind("127.0.0.1", 10234);

		PinpointSocketFactory socketFactory = createPinpointSocketFactory();

		try {

			EchoMessageListener echoMessageListener1 = new EchoMessageListener();
			EchoMessageListener echoMessageListener2 = new EchoMessageListener();
			
			
			// 리스터를 등록한 것만 RegisterAgent 로 나옴
			PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234, echoMessageListener1);
			PinpointSocket socket2 = socketFactory.connect("127.0.0.1", 10234, echoMessageListener2);
			
			Thread.sleep(500);

			List<ChannelContext> channelContextList = ss.getRegisterAgentChannelContext();
			if (channelContextList.size() != 2) {
				Assert.fail();
			}

			ChannelContext channelContext = channelContextList.get(0);
			Future<ResponseMessage> future = channelContext.getSocketChannel().sendRequestMessage("socket1".getBytes());
			ChannelContext channelContext2 = channelContextList.get(1);
			Future<ResponseMessage> future2 = channelContext2.getSocketChannel().sendRequestMessage("socket2".getBytes());

			future.await();
			future2.await();
			Assert.assertEquals("socket1", new String(future.getResult().getMessage()));
			Assert.assertEquals("socket2", new String(future2.getResult().getMessage()));
			
			socket.close();
			socket2.close();
		} finally {
			socketFactory.release();
			ss.close();
		}
	}

	
	private PinpointSocketFactory createPinpointSocketFactory() {
		PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
		pinpointSocketFactory.setAgentProperties(getParams());

		return pinpointSocketFactory;
	}

	// @Test
	public void pingInternal() throws IOException, InterruptedException {
		PinpointServerSocket ss = new PinpointServerSocket();
		ss.bind("127.0.0.1", 10234);
		PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
		// pinpointSocketFactory.setPingDelay(100);
		pinpointSocketFactory.setAgentProperties(getParams());

		try {
			PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", 10234, SimpleLoggingMessageListener.LISTENER);
			// socket.setMessageListener(SimpleLoggingMessageListener.LISTENER);

			PinpointSocket socket2 = pinpointSocketFactory.connect("127.0.0.1", 10234, SimpleLoggingMessageListener.LISTENER);
			// socket2.setMessageListener(SimpleLoggingMessageListener.LISTENER);

			Thread.sleep(1000);

			List<ChannelContext> channelContextList = ss.getRegisterAgentChannelContext();

			for (ChannelContext channelContext : channelContextList) {
				Future future = channelContext.getSocketChannel().sendRequestMessage(new byte[0]);
				future.await();
				System.out.println(future.getResult());

				channelContext.getSocketChannel().sendRequestMessage(new byte[0]);
				channelContext.getSocketChannel().sendRequestMessage(new byte[0]);
				channelContext.getSocketChannel().sendRequestMessage(new byte[0]);
				channelContext.getSocketChannel().sendRequestMessage(new byte[0]);

				channelContext.getSocketChannel().sendMessage(new byte[0]);
			}

			Thread.sleep(1000);

			socket.close();
		} finally {
			pinpointSocketFactory.release();
			ss.close();
		}

	}

	private Map getParams() {
		Map properties = new HashMap();

		properties.put(AgentProperties.KEY_AGENTID, "agent");
		properties.put(AgentProperties.KEY_APPLICATION_NAME, "application");
		properties.put(AgentProperties.KEY_HOSTNAME, "hostname");
		properties.put(AgentProperties.KEY_IP, "ip");
		properties.put(AgentProperties.KEY_PID, 1111);
		properties.put(AgentProperties.KEY_SERVICE_TYPE, 10);
		properties.put(AgentProperties.KEY_START_TIME_MILLIS, System.currentTimeMillis());
		properties.put(AgentProperties.KEY_VERSION, "1.0");

		return properties;
	}

	class EchoMessageListener implements MessageListener {
		private final List<SendPacket> sendPacketRepository = new ArrayList<SendPacket>();
		private final List<RequestPacket> requestPacketRepository = new ArrayList<RequestPacket>();

		@Override
		public void handleSend(SendPacket sendPacket, Channel channel) {
			sendPacketRepository.add(sendPacket);
			
			byte[] payload = sendPacket.getPayload();
			System.out.println(new String(payload));
		}

		@Override
		public void handleRequest(RequestPacket requestPacket, Channel channel) {
			requestPacketRepository.add(requestPacket);
			
			byte[] payload = requestPacket.getPayload();
			System.out.println(new String(payload));

			channel.write(new ResponsePacket(requestPacket.getRequestId(), requestPacket.getPayload()));
		}

		public List<SendPacket> getSendPacketRepository() {
			return sendPacketRepository;
		}

		public List<RequestPacket> getRequestPacketRepository() {
			return requestPacketRepository;
		}

	}

}
