package com.nhn.pinpoint.collector.cluster;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.collector.receiver.tcp.AgentProperties;
import com.nhn.pinpoint.rpc.packet.ControlEnableWorkerConfirmPacket;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamPacket;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.ServerStreamChannel;
import com.nhn.pinpoint.rpc.server.SocketChannel;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ClusterPointRouterTest {

	private static final int DEFAULT_ACCEPTOR_SOCKET_PORT = 22215;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ClusterPointRouter clusterPointRouter;

	@Test
	public void webClusterPointtest() {
		WebClusterPoint webClusterPoint = clusterPointRouter.getWebClusterPoint();

		PinpointServerSocket pinpointServerSocket = new PinpointServerSocket();
		pinpointServerSocket.setMessageListener(new PinpointSocketManagerHandler());
		pinpointServerSocket.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

		InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

		Assert.assertEquals(0, webClusterPoint.getWebClusterList().size());
		webClusterPoint.connectPointIfAbsent(address);
		Assert.assertEquals(1, webClusterPoint.getWebClusterList().size());
		webClusterPoint.connectPointIfAbsent(address);
		Assert.assertEquals(1, webClusterPoint.getWebClusterList().size());

		webClusterPoint.disconnectPoint(address);
		Assert.assertEquals(0, webClusterPoint.getWebClusterList().size());
		webClusterPoint.disconnectPoint(address);
		Assert.assertEquals(0, webClusterPoint.getWebClusterList().size());
	}

	@Test
	public void profilerClusterPointtest() {
		ProfilerClusterPoint profilerClusterPoint = clusterPointRouter.getProfilerClusterPoint();

		ChannelContext channelContext = new ChannelContext(null, null);
		channelContext.setChannelProperties(getParams());

		profilerClusterPoint.registerChannelContext(channelContext);
		Assert.assertEquals(1, profilerClusterPoint.getChannelContext().size());

		Assert.assertNull(profilerClusterPoint.getChannelContext("a", "a"));
		Assert.assertNull(profilerClusterPoint.getChannelContext("application", "a"));
		Assert.assertEquals(channelContext, profilerClusterPoint.getChannelContext("application", "agent"));
		
		profilerClusterPoint.unregisterChannelContext(channelContext);
		Assert.assertEquals(0, profilerClusterPoint.getChannelContext().size());
		Assert.assertNull(profilerClusterPoint.getChannelContext("application", "agent"));
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
		public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
			logger.warn("unsupported streamPacket received {}", streamPacket);
		}

		@Override
		public int handleEnableWorker(Map properties) {
			logger.warn("do handleEnableWorker {}", properties);
			return ControlEnableWorkerConfirmPacket.SUCCESS;
		}
	}

	private Map<Object, Object> getParams() {
		Map<Object, Object> properties = new HashMap<Object, Object>();

		properties.put(AgentProperties.KEY_AGENTID, "agent");
		properties.put(AgentProperties.KEY_APPLICATION_NAME, "application");
		properties.put(AgentProperties.KEY_HOSTNAME, "hostname");
		properties.put(AgentProperties.KEY_IP, "ip");
		properties.put(AgentProperties.KEY_PID, 1111);
		properties.put(AgentProperties.KEY_SERVICE_TYPE, 10);
		properties.put(AgentProperties.KEY_START_TIME_MILLIS, System.currentTimeMillis());
		properties.put(AgentProperties.KEY_VERSION, "1.0.3-SNAPSHOT");

		return properties;
	}

}
