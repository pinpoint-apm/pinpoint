package com.nhn.pinpoint.collector.cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingZooKeeperServer;
import org.junit.Assert;
import org.junit.Test;

import com.nhn.pinpoint.collector.cluster.zookeeper.ZookeeperClusterManager;
import com.nhn.pinpoint.collector.receiver.tcp.AgentProperties;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocketStateCode;

public class ZookeeperClusterManagerTest2 {

	@Test
	public void simpleTest1() throws Exception {
		TestingCluster tcluster = null;
		try {
			tcluster = createZookeeperCluster(3);
			
			String connectString = getConnectString(tcluster);
			
			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager(connectString, 3000);

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
			
			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager(connectString, 3000);

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

			restart(tcluster);
			
			result = clusterManager.getData(channelContext);
			Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, getCode(result));

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			clusterManager.close();
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

			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager(connectString, 3000);

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

			stop(tcluster);

			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			restart(tcluster);
			
			result = clusterManager.getData(channelContext);
			Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, getCode(result));

			channelContext.changeStateShutdown();
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			clusterManager.close();
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
