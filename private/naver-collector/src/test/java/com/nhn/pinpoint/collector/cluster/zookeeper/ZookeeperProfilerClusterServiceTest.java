package com.nhn.pinpoint.collector.cluster.zookeeper;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.test.TestingServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.collector.cluster.ClusterPointRouter;
import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.SocketChannel;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperProfilerClusterServiceTest {

	private static final int DEFAULT_ACCEPTOR_PORT = 22213;

	private static CollectorConfiguration collectorConfig = null;

	@Autowired
	ClusterPointRouter clusterPointRouter;
	
	@BeforeClass
	public static void setUp() {
		collectorConfig = new CollectorConfiguration();

		collectorConfig.setClusterEnable(true);
		collectorConfig.setClusterAddress("127.0.0.1:" + DEFAULT_ACCEPTOR_PORT);
		collectorConfig.setClusterSessionTimeout(3000);
	}

	@Test
	public void simpleTest1() throws Exception {
		TestingServer ts = null;
		try {
			ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ChannelContext channelContext = new ChannelContext(mock(SocketChannel.class), null, service.getChannelStateChangeEventListener());
			channelContext.setChannelProperties(getParams());

			ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

			channelContext.changeStateRun();
			Thread.sleep(1000);

			List<String> result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(1, result.size());

			
			
			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			service.tearDown();
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
			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ChannelContext channelContext = new ChannelContext(mock(SocketChannel.class), null, service.getChannelStateChangeEventListener());
			channelContext.setChannelProperties(getParams());

			ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

			channelContext.changeStateRun();
			Thread.sleep(1000);
			List<String> result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);
			Thread.sleep(10000);
			
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(1, result.size());

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			service.tearDown();
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
			ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ChannelContext channelContext = new ChannelContext(mock(SocketChannel.class), null, service.getChannelStateChangeEventListener());
			channelContext.setChannelProperties(getParams());

			ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

			channelContext.changeStateRun();
			Thread.sleep(1000);
			List<String> result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(1, result.size());

			ts.stop();
			Thread.sleep(1000);
			ts.restart();
			Thread.sleep(1000);

			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(1, result.size());

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			service.tearDown();
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
			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ChannelContext channelContext = new ChannelContext(mock(SocketChannel.class), null, service.getChannelStateChangeEventListener());
			channelContext.setChannelProperties(getParams());

			ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

			channelContext.changeStateRun();
			Thread.sleep(1000);
			List<String> result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			service.tearDown();
		} finally {
			closeZookeeperServer(ts);
		}
	}

	//
	// 심플 쥬키퍼 테스트
	// 쥬키퍼와 연결되었을때 이벤트가 등록되었는데
	// 쥬키퍼가 종료 되고 다시 연결될때 해당 이벤트가 상태를 유지 되는지
	@Test
	public void simpleTest5() throws Exception {
		TestingServer ts = null;
		try {
			ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ChannelContext channelContext = new ChannelContext(mock(SocketChannel.class), null, service.getChannelStateChangeEventListener());
			channelContext.setChannelProperties(getParams());

			ZookeeperProfilerClusterManager profilerClusterManager = service.getProfilerClusterManager();

			channelContext.changeStateRun();
			Thread.sleep(1000);
			List<String> result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			channelContext.changeStateRunDuplexCommunication();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(1, result.size());

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			ts.stop();
			Thread.sleep(1000);
			ts.restart();
			Thread.sleep(1000);

			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			service.tearDown();
		} finally {
			closeZookeeperServer(ts);
		}
	}

	private void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
		try {
			mockZookeeperServer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<Object, Object> getParams() {
		Map<Object, Object> properties = new HashMap<Object, Object>();

        properties.put(AgentHandshakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(AgentHandshakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(AgentHandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(AgentHandshakePropertyType.IP.getName(), "ip");
        properties.put(AgentHandshakePropertyType.PID.getName(), 1111);
        properties.put(AgentHandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(AgentHandshakePropertyType.START_TIMESTAMP.getName(), System.currentTimeMillis());
        properties.put(AgentHandshakePropertyType.VERSION.getName(), "1.0");

		return properties;
	}

}
