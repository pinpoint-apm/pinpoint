package com.nhn.pinpoint.collector.cluster;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.collector.receiver.tcp.AgentHandShakePropertyType;
import com.nhn.pinpoint.collector.util.CollectorUtils;
import com.nhn.pinpoint.rpc.DefaultFuture;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.packet.HandShakeResponseCode;
import com.nhn.pinpoint.rpc.packet.HandShakeResponseType;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.SocketChannel;
import com.nhn.pinpoint.thrift.dto.command.TCommandEcho;
import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.util.SerializationUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ClusterPointRouterTest2 {

	private static final int DEFAULT_ACCEPTOR_SOCKET_PORT = 22215;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final long currentTime = System.currentTimeMillis();

	@Autowired
	ClusterPointRouter clusterPointRouter;

	@Autowired
	private SerializerFactory commandSerializerFactory;

	@Autowired
	private DeserializerFactory commandDeserializerFactory;

	@Test
	public void profilerClusterPointtest() throws TException, InterruptedException {
		WebCluster webCluster = null;
		try {
			webCluster = new WebCluster(CollectorUtils.getServerIdentifier(), clusterPointRouter);
			
			PinpointServerSocket pinpointServerSocket = createServerSocket("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

			InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);
			webCluster.connectPointIfAbsent(address);

			// profiler쪽 clusterPoint 생성
			SocketChannel socketChannel = mock(SocketChannel.class);
			ClusterPoint clusterPoint = new ChannelContextClusterPoint(createChannelContext(socketChannel));

			ClusterPointRepository clusterPointRepository = clusterPointRouter.getTargetClusterPointRepository();
			clusterPointRepository.addClusterPoint(clusterPoint);

			byte[] echoPayload = createEchoPayload("hello");
			when(socketChannel.sendRequestMessage(echoPayload)).thenReturn(createExpectedFuture(echoPayload));

			byte[] commandDeliveryPayload = createDeliveryCommandPayload("application", "agent", currentTime, echoPayload);

			List<ChannelContext> contextList = pinpointServerSocket.getDuplexCommunicationChannelContext();
			ChannelContext context = contextList.get(0);
			Future<ResponseMessage> future = context.getSocketChannel().sendRequestMessage(commandDeliveryPayload);
			future.await();

			TCommandEcho base = (TCommandEcho) SerializationUtils.deserialize(future.getResult().getMessage(), commandDeserializerFactory);
			
			Assert.assertEquals(base.getMessage(), "hello");
		} finally {
			if (webCluster != null) {
				webCluster.close();
			}
		}
	}
	
	private PinpointServerSocket createServerSocket(String host, int port) {
		PinpointServerSocket pinpointServerSocket = new PinpointServerSocket();
		pinpointServerSocket.setMessageListener(new PinpointSocketManagerHandler());
		pinpointServerSocket.bind(host, port);

		
		return pinpointServerSocket;
	}
	
	private ChannelContext createChannelContext(SocketChannel socketChannel) {
		ChannelContext channelContext = new ChannelContext(socketChannel, null);
		channelContext.setChannelProperties(getParams());

		return channelContext;
	}
	
	private DefaultFuture createExpectedFuture(byte[] payload) {
		ResponseMessage responseMessage = new ResponseMessage();
		responseMessage.setMessage(payload);

		DefaultFuture future = new DefaultFuture();
		future.setResult(responseMessage);

		return future;
	}

	private byte[] createEchoPayload(String message) throws TException {
		TCommandEcho echo = new TCommandEcho();
		echo.setMessage("hello");

		byte[] payload = SerializationUtils.serialize(echo, commandSerializerFactory);
		return payload;
	}
	
	private byte[] createDeliveryCommandPayload(String application, String agent, long currentTime, byte[] echoPayload) throws TException {
		TCommandTransfer commandTransfer = new TCommandTransfer();
		commandTransfer.setApplicationName("application");
		commandTransfer.setAgentId("agent");
		commandTransfer.setStartTime(currentTime);
		commandTransfer.setPayload(echoPayload);

		byte[] payload = SerializationUtils.serialize(commandTransfer, commandSerializerFactory);
		return payload;
	}

	private class PinpointSocketManagerHandler implements ServerMessageListener {
		@Override
		public void handleSend(SendPacket sendPacket, SocketChannel channel) {
			logger.warn("Unsupport send received {} {}", sendPacket, channel);
		}

		@Override
		public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
			logger.warn("Unsupport request received {} {}", requestPacket, channel);
		}

		@Override
		public HandShakeResponseCode handleHandShake(Map properties) {
			logger.warn("do handleEnableWorker {}", properties);
			return HandShakeResponseType.Success.DUPLEX_COMMUNICATION;
		}
	}

	private Map<Object, Object> getParams() {
		Map<Object, Object> properties = new HashMap<Object, Object>();
		
		properties.put(AgentHandShakePropertyType.AGENT_ID.getName(), "agent");
		properties.put(AgentHandShakePropertyType.APPLICATION_NAME.getName(), "application");
		properties.put(AgentHandShakePropertyType.HOSTNAME.getName(), "hostname");
		properties.put(AgentHandShakePropertyType.IP.getName(), "ip");
		properties.put(AgentHandShakePropertyType.PID.getName(), 1111);
		properties.put(AgentHandShakePropertyType.SERVICE_TYPE.getName(), 10);
		properties.put(AgentHandShakePropertyType.START_TIMESTAMP.getName(), currentTime);
		properties.put(AgentHandShakePropertyType.VERSION.getName(), "1.0.3-SNAPSHOT");

		return properties;
	}

	private TargetClusterPoint findClusterPoint(String applicationName, String agentId, long startTimeStamp, List<TargetClusterPoint> targetClusterPointList) {

		List<TargetClusterPoint> result = new ArrayList<TargetClusterPoint>();

		for (TargetClusterPoint targetClusterPoint : targetClusterPointList) {
			if (!targetClusterPoint.getApplicationName().equals(applicationName)) {
				continue;
			}

			if (!targetClusterPoint.getAgentId().equals(agentId)) {
				continue;
			}

			if (!(targetClusterPoint.getStartTimeStamp() == startTimeStamp)) {
				continue;
			}

			result.add(targetClusterPoint);
		}

		if (result.size() == 1) {
			return result.get(0);
		}

		if (result.size() > 1) {
			logger.warn("Ambiguous ClusterPoint {}, {}, {} (Valid Agent list={}).", applicationName, agentId, startTimeStamp, result);
			return null;
		}

		return null;
	}

}
