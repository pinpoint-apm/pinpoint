package com.nhn.pinpoint.collector.cluster;

import java.util.HashMap;
import java.util.Map;

import org.apache.curator.test.TestingServer;
import org.junit.Assert;
import org.junit.Test;

import com.nhn.pinpoint.collector.cluster.zookeeper.ZookeeperClusterManager;
import com.nhn.pinpoint.collector.receiver.tcp.AgentProperties;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocketStateCode;

public class ZookeeperClusterManagerTest {

	private static final int DEFAULT_ACCEPTOR_PORT = 22213;

	// 심플 쥬키퍼 테스트
	// 상태에 변경에 따라 상태 변경이 제대로 되어있는지 확인
	@Test
	public void simpleTest1() throws Exception {
		TestingServer ts = null;
		try {
			ts = createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT, 3000);

			ChannelContext channelContext = new ChannelContext(null, null, clusterManager);
			channelContext.setChannelProperties(getParams());

			channelContext.changeStateRun();
			Thread.sleep(1000);
			Map result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, getCode(result));

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			clusterManager.close();
		} finally {
			closeZookeeperServer(ts);
		}
	}

	// 심플 쥬키퍼 테스트
	// 쥬키퍼와 연결이 끊어져 있는경우 이벤트가 발생했을때 쥬키퍼와 연결이 될 경우 해당 이벤트가 처리되어 있는지
	@Test
	public void simpleTest2() throws Exception {
		TestingServer ts = null;
		try {

			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT, 3000);

			ChannelContext channelContext = new ChannelContext(null, null, clusterManager);
			channelContext.setChannelProperties(getParams());

			channelContext.changeStateRun();
			Thread.sleep(1000);
			Map result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			ts = createZookeeperServer(DEFAULT_ACCEPTOR_PORT);
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, getCode(result));

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			clusterManager.close();
		} finally {
			closeZookeeperServer(ts);
		}
	}

	// 심플 쥬키퍼 테스트
	// 쥬키퍼와 연결되었을때 이벤트가 등록되었는데
	// 쥬키퍼가 종료 되고 다시 연결될때 해당 이벤트가 상태를 유지 되는지
	@Test
	public void simpleTest3() throws Exception {
		TestingServer ts = null;
		try {
			ts = createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT, 3000);

			ChannelContext channelContext = new ChannelContext(null, null, clusterManager);
			channelContext.setChannelProperties(getParams());

			channelContext.changeStateRun();
			Thread.sleep(1000);
			Map result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, getCode(result));

			ts.stop();
			Thread.sleep(1000);
			ts.restart();
			Thread.sleep(1000);

			result = clusterManager.getData(channelContext);
			Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, getCode(result));

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			clusterManager.close();
		} finally {
			closeZookeeperServer(ts);
		}
	}

	// 심플 쥬키퍼 테스트
	// 쥬키퍼와 연결이 끊어져 있는경우 이벤트가 발생했을때 쥬키퍼와 연결이 될 경우 해당 이벤트가 처리되어 있는지
	@Test
	public void simpleTest4() throws Exception {
		TestingServer ts = null;
		try {
			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT, 3000);

			ChannelContext channelContext = new ChannelContext(null, null, clusterManager);
			channelContext.setChannelProperties(getParams());

			channelContext.changeStateRun();
			Thread.sleep(1000);
			Map result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			ts = createZookeeperServer(DEFAULT_ACCEPTOR_PORT);
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			clusterManager.close();
		} finally {
			closeZookeeperServer(ts);
		}
	}

	// 심플 쥬키퍼 테스트
	// 쥬키퍼와 연결되었을때 이벤트가 등록되었는데
	// 쥬키퍼가 종료 되고 다시 연결될때 해당 이벤트가 상태를 유지 되는지
	@Test
	public void simpleTest5() throws Exception {
		TestingServer ts = null;
		try {
			ts = createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT, 3000);

			ChannelContext channelContext = new ChannelContext(null, null, clusterManager);
			channelContext.setChannelProperties(getParams());

			channelContext.changeStateRun();
			Thread.sleep(1000);
			Map result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, getCode(result));

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			ts.stop();
			Thread.sleep(1000);
			ts.restart();
			Thread.sleep(1000);

			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			clusterManager.close();
		} finally {
			closeZookeeperServer(ts);
		}
	}

	private TestingServer createZookeeperServer(int port) throws Exception {
		TestingServer mockZookeeperServer = new TestingServer(port);
		mockZookeeperServer.start();

		return mockZookeeperServer;
	}

	private void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
		try {
			mockZookeeperServer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PinpointServerSocketStateCode getCode(Map channelContextData) {
		String state = (String) channelContextData.get("state");
		return PinpointServerSocketStateCode.getStateCode(state);
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
		properties.put(AgentProperties.KEY_VERSION, "1.0");

		return properties;
	}

}
