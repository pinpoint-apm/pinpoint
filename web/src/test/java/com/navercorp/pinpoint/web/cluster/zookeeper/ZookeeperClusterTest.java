package com.nhn.pinpoint.web.cluster.zookeeper;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.jboss.netty.channel.Channel;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;
import com.nhn.pinpoint.common.util.NetUtils;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.web.cluster.zookeeper.ZookeeperClusterManager;

public class ZookeeperClusterTest {

	private static final int DEFAULT_ACCEPTOR_PORT = 9995;
	private static final int DEFAULT_ZOOKEEPER_PORT = 22213;

	private static final String DEFAULT_IP = NetUtils.getLocalV4Ip();

	private static final String COLLECTOR_NODE_PATH = "/pinpoint-cluster/collector";
	private static final String COLLECTOR_TEST_NODE_PATH = "/pinpoint-cluster/collector/test";
	
	private static final String CLUSTER_NODE_PATH = "/pinpoint-cluster/web/" + DEFAULT_IP + ":" + DEFAULT_ACCEPTOR_PORT;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static TestingServer ts = null;

	@BeforeClass
	public static void setUp() throws Exception {
		ts = createZookeeperServer(DEFAULT_ZOOKEEPER_PORT);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		closeZookeeperServer(ts);
	}

	@Before
	public void before() throws IOException {
		ts.stop();
	}

	// ApplicationContext 설정에 맞게 등록이 되는지
	@Test
	public void clusterTest1() throws Exception {
		ts.restart();

		ZooKeeper zookeeper = null;
		ZookeeperClusterManager manager = null;
		try {
			zookeeper = new ZooKeeper(DEFAULT_IP + ":" + DEFAULT_ZOOKEEPER_PORT, 5000, null);
			createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
			zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".getBytes(), -1);
			
			manager = new ZookeeperClusterManager(DEFAULT_IP + ":" + DEFAULT_ZOOKEEPER_PORT, 5000, 60000);
			Thread.sleep(3000);

			List<String> agentList = manager.getRegisteredAgentList("a", "b", 1L);
			Assert.assertEquals(1, agentList.size());
			Assert.assertEquals("test", agentList.get(0));

			agentList = manager.getRegisteredAgentList("b", "c", 1L);
			Assert.assertEquals(0, agentList.size());
			
			zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "".getBytes(), -1);
			Thread.sleep(3000);

			agentList = manager.getRegisteredAgentList("a", "b", 1L);
			Assert.assertEquals(0, agentList.size());
		} finally {
			if (zookeeper != null) {
				zookeeper.close();
			}
			
			if (manager != null) {
				manager.close();
			}
		}
	}
	
	@Test
	public void clusterTest2() throws Exception {
		ts.restart();

		ZooKeeper zookeeper = null;
		ZookeeperClusterManager manager = null;
		try {
			zookeeper = new ZooKeeper(DEFAULT_IP + ":" + DEFAULT_ZOOKEEPER_PORT, 5000, null);
			createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
			zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".getBytes(), -1);
			
			manager = new ZookeeperClusterManager(DEFAULT_IP + ":" + DEFAULT_ZOOKEEPER_PORT, 5000, 60000);
			Thread.sleep(3000);

			List<String> agentList = manager.getRegisteredAgentList("a", "b", 1L);
			Assert.assertEquals(1, agentList.size());
			Assert.assertEquals("test", agentList.get(0));

			zookeeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1\r\nc:d:2".getBytes(), -1);
			Thread.sleep(3000);


			agentList = manager.getRegisteredAgentList("a", "b", 1L);
			Assert.assertEquals(1, agentList.size());
			Assert.assertEquals("test", agentList.get(0));

			agentList = manager.getRegisteredAgentList("c", "d", 2L);
			Assert.assertEquals(1, agentList.size());
			Assert.assertEquals("test", agentList.get(0));

			zookeeper.delete(COLLECTOR_TEST_NODE_PATH, -1);
			Thread.sleep(3000);
			
			agentList = manager.getRegisteredAgentList("a", "b", 1L);
			Assert.assertEquals(0, agentList.size());
			
			agentList = manager.getRegisteredAgentList("c", "d", 2L);
			Assert.assertEquals(0, agentList.size());
		} finally {
			if (zookeeper != null) {
				zookeeper.close();
			}
			
			if (manager != null) {
				manager.close();
			}
		}
	}

	private static TestingServer createZookeeperServer(int port) throws Exception {
		TestingServer mockZookeeperServer = new TestingServer(port);
		mockZookeeperServer.start();

		return mockZookeeperServer;
	}

	private static void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
		try {
			if (mockZookeeperServer != null) {
				mockZookeeperServer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getNodeAndCompareContents(ZooKeeper zookeeper) throws KeeperException, InterruptedException {
		byte[] conetents = zookeeper.getData(CLUSTER_NODE_PATH, null, null);

		String[] registeredIplist = new String(conetents).split("\r\n");

		List<String> ipList = NetUtils.getLocalV4IpList();

		Assert.assertEquals(registeredIplist.length, ipList.size());

		for (String ip : registeredIplist) {
			Assert.assertTrue(ipList.contains(ip));
		}
	}

	private void closePinpointSocket(PinpointSocketFactory factory, PinpointSocket socket) {
		if (socket != null) {
			socket.close();
		}

		if (factory != null) {
			factory.release();
		}
	}

	class SimpleListener implements MessageListener {
		@Override
		public void handleSend(SendPacket sendPacket, Channel channel) {

		}

		@Override
		public void handleRequest(RequestPacket requestPacket, Channel channel) {
			// TODO Auto-generated method stub

		}
	}

	public void createPath(ZooKeeper zookeeper, String path, boolean createEndNode) throws PinpointZookeeperException, InterruptedException, KeeperException {

		int pos = 1;
		do {
			pos = path.indexOf('/', pos + 1);

			if (pos == -1) {
				pos = path.length();
			}

			if (pos == path.length()) {
				if (!createEndNode) {
					return;
				}
			}

			String subPath = path.substring(0, pos);
			if (zookeeper.exists(subPath, false) != null) {
				continue;
			}

			String result = zookeeper.create(subPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			logger.info("Create path {} success.", result);
		} while (pos < path.length());
	}

}
