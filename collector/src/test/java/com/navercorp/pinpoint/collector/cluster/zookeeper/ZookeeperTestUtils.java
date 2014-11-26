package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.util.HashMap;
import java.util.Map;

import org.apache.curator.test.TestingServer;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.receiver.tcp.AgentHandShakePropertyType;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.packet.HandShakeResponseCode;
import com.nhn.pinpoint.rpc.packet.HandShakeResponseType;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.SocketChannel;

final class ZookeeperTestUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperTestUtils.class);
	
	private ZookeeperTestUtils() {
	}

	static MessageListener getMessageListener()  {
		return new SimpleMessageListener();
	}
	
	static ServerMessageListener getServerMessageListener() {
		return new SimpleServerMessageListner();
	}
	
	static Map<String, Object> getParams() {
		return getParams("application", "agent", System.currentTimeMillis());
	}

	static Map<String, Object> getParams(String applicationName, String agentId, long startTimeMillis) {
		Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(AgentHandShakePropertyType.AGENT_ID.getName(), agentId);
        properties.put(AgentHandShakePropertyType.APPLICATION_NAME.getName(), applicationName);
        properties.put(AgentHandShakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(AgentHandShakePropertyType.IP.getName(), "ip");
        properties.put(AgentHandShakePropertyType.PID.getName(), 1111);
        properties.put(AgentHandShakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(AgentHandShakePropertyType.START_TIMESTAMP.getName(), startTimeMillis);
        properties.put(AgentHandShakePropertyType.VERSION.getName(), "1.0");

		return properties;
	}

	static TestingServer createZookeeperServer(int port) throws Exception {
		TestingServer mockZookeeperServer = new TestingServer(port);
		mockZookeeperServer.start();

		return mockZookeeperServer;
	}

	private static class SimpleMessageListener implements MessageListener {

		public SimpleMessageListener() {
		}

		@Override
		public void handleSend(SendPacket sendPacket, Channel channel) {
			LOGGER.info("Received SendPacket{} {}", sendPacket, channel);
		}

		@Override
		public void handleRequest(RequestPacket requestPacket, Channel channel) {
			LOGGER.info("Received RequestPacket{} {}", requestPacket, channel);
		}
	}
	
	private static class SimpleServerMessageListner implements ServerMessageListener {
		@Override
		public void handleSend(SendPacket sendPacket, SocketChannel channel) {
			LOGGER.warn("Unsupport send received {} {}", sendPacket, channel);
		}

		@Override
		public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
			LOGGER.warn("Unsupport request received {} {}", requestPacket, channel);
		}

		@Override
		public HandShakeResponseCode handleHandShake(Map properties) {
			LOGGER.warn("do handleEnableWorker {}", properties);
			return HandShakeResponseType.Success.DUPLEX_COMMUNICATION;
		}
	}

}
