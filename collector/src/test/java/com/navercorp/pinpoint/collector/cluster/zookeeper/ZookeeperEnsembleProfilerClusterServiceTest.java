package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingZooKeeperServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.collector.cluster.ClusterPointRouter;
import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.collector.receiver.tcp.AgentProperties;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocketStateCode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZookeeperEnsembleProfilerClusterServiceTest {

	@Autowired
	ClusterPointRouter clusterPointRouter;
	
	@Test
	public void simpleTest1() throws Exception {
		TestingCluster tcluster = null;
		try {
			tcluster = createZookeeperCluster(3);

			String connectString = getConnectString(tcluster);

			CollectorConfiguration collectorConfig = createConfig(connectString);
			
			
			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ChannelContext channelContext = new ChannelContext(null, null, service.getChannelStateChangeEventListener());
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
			closeZookeeperCluster(tcluster);
		}
	}

	// 연결되어 있는 쥬키퍼 클러스터가 끊어졌을때 해당 이벤트가 유지되는지
	// 테스트 코드만으로는 정확한 확인은 힘들다. 로그를 봐야함
	@Test
	public void simpleTest2() throws Exception {
		TestingCluster tcluster = null;
		try {
			tcluster = createZookeeperCluster(3);

			String connectString = getConnectString(tcluster);

			CollectorConfiguration collectorConfig = createConfig(connectString);

			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ChannelContext channelContext = new ChannelContext(null, null, service.getChannelStateChangeEventListener());
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

			restart(tcluster);

			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(1, result.size());

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			service.tearDown();
		} finally {
			closeZookeeperCluster(tcluster);
		}
	}

	
	// 연결되어 있는 쥬키퍼 클러스터가 모두 죽었을 경우
	// 그 이후 해당 이벤트가 유지되는지
	// 테스트 코드만으로는 정확한 확인은 힘들다. 로그를 봐야함
	@Test
	public void simpleTest3() throws Exception {
		TestingCluster tcluster = null;
		try {
			tcluster = createZookeeperCluster(3);

			String connectString = getConnectString(tcluster);

			CollectorConfiguration collectorConfig = createConfig(connectString);

			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ChannelContext channelContext = new ChannelContext(null, null, service.getChannelStateChangeEventListener());
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

			stop(tcluster);

			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			restart(tcluster);

			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(1, result.size());

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = profilerClusterManager.getClusterData();
			Assert.assertEquals(0, result.size());

			service.tearDown();
		} finally {
			closeZookeeperCluster(tcluster);
		}
	}

	private TestingCluster createZookeeperCluster(int size) throws Exception {
		return createZookeeperCluster(size, true);
	}

	private TestingCluster createZookeeperCluster(int size, boolean start) throws Exception {
		TestingCluster zookeeperCluster = new TestingCluster(size);

		// 주의 cluster 초기화에 시간이 좀 걸림 그래서 테스트에 sleep을 좀 길게 둠
		// 다 된걸 받는 이벤트도 없음
		if (start) {
			zookeeperCluster.start();
			Thread.sleep(5000);
		}

		return zookeeperCluster;
	}

	private void startZookeeperCluster(TestingCluster zookeeperCluster) throws Exception {
		zookeeperCluster.start();
		Thread.sleep(5000);
	}

	private void restart(TestingCluster zookeeperCluster) throws Exception {
		for (TestingZooKeeperServer zookeeperServer : zookeeperCluster.getServers()) {
			zookeeperServer.restart();
		}
		Thread.sleep(5000);
	}

	private void stop(TestingCluster zookeeperCluster) throws Exception {
		zookeeperCluster.stop();
		Thread.sleep(5000);
	}

	private void closeZookeeperCluster(TestingCluster zookeeperCluster) throws Exception {
		try {
			if (zookeeperCluster != null) {
				zookeeperCluster.close();
			}
		} catch (Exception e) {
		}
	}

	private String getConnectString(TestingZooKeeperServer testingZooKeeperServer) {
		return testingZooKeeperServer.getInstanceSpec().getConnectString();
	}

	private String getConnectString(TestingCluster zookeeperCluster) {
		StringBuilder connectString = new StringBuilder();

		Iterator<InstanceSpec> instanceSpecIterator = zookeeperCluster.getInstances().iterator();
		while (instanceSpecIterator.hasNext()) {
			InstanceSpec instanceSpec = instanceSpecIterator.next();
			connectString.append(instanceSpec.getConnectString());

			if (instanceSpecIterator.hasNext()) {
				connectString.append(",");
			}
		}

		return connectString.toString();
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

	private CollectorConfiguration createConfig(String connectString) {
		CollectorConfiguration collectorConfig = new CollectorConfiguration();

		collectorConfig.setClusterEnable(true);
		collectorConfig.setClusterAddress(connectString);
		collectorConfig.setClusterSessionTimeout(3000);

		return collectorConfig;
	}

}
