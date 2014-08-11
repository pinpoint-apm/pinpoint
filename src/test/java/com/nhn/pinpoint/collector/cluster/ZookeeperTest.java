package com.nhn.pinpoint.collector.cluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.zookeeper.KeeperException;
import org.junit.Assert;
import org.junit.Test;

import com.nhn.pinpoint.collector.cluster.zookeeper.ZookeeperClusterManager;
import com.nhn.pinpoint.rpc.server.AgentProperties;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocketStateCode;


public class ZookeeperTest {
	
	// 테스트만들기가 어려움 일단 가장 단순한 테스트는 로그보고 확인
	// 이후 추가예정
	
	@Test
	public void simpleTest1() throws KeeperException, IOException, InterruptedException {
		try {
			ChannelContext channelContext = new ChannelContext(null, null);
			channelContext.setAgentProperties(getParams());
			
			
			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager("dev.zk.pinpoint.navercorp.com", 3000);

			clusterManager.eventPerformed(channelContext, PinpointServerSocketStateCode.RUN_WITHOUT_REGISTER);
			Thread.sleep(1000);
			Map result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));

			clusterManager.eventPerformed(channelContext, PinpointServerSocketStateCode.RUN);
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertEquals(PinpointServerSocketStateCode.RUN, getCode(result));

			clusterManager.eventPerformed(channelContext, PinpointServerSocketStateCode.SHUTDOWN);
			Thread.sleep(1000);
			result = clusterManager.getData(channelContext);
			Assert.assertNull(getCode(result));
			
			clusterManager.close();
			
//			Thread.sleep(100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
//	@Test
	public void simpleTest2() throws KeeperException, IOException, InterruptedException {
		try {
			ChannelContext channelContext = new ChannelContext(null, null);
			channelContext.setAgentProperties(getParams());
			
			
			ZookeeperClusterManager clusterManager = new ZookeeperClusterManager("dev.zk.pinpoint.navercorp.com", 3000);
			clusterManager.eventPerformed(channelContext, PinpointServerSocketStateCode.SHUTDOWN);
			
			Thread.sleep(5000);
			
			clusterManager.close();
			
//			clusterManager.eventPerformed(channelContext, PinpointServerSocketStateCode.RUN);
			Thread.sleep(100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private PinpointServerSocketStateCode getCode(Map channelContextData) {
		String state = (String) channelContextData.get("state");
		return PinpointServerSocketStateCode.getStateCode(state);
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


}
