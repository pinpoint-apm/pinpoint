package com.nhn.pinpoint.web.cluster;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.jboss.netty.channel.Channel;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.common.util.NetUtils;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.web.server.PinpointSocketManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-web.xml")
public class ClusterTest {

	// 해당 테스트를 로컬에서 실행할떄 resource profile 문제로 실패할수 있음
	// 실패할 경우 resource-test의 pinpoint-web.properties파일을 resource-local의 pinpoint-web.properties에 복사하여 테스트하면 성공한다.
	
	private static final int DEFAULT_ACCEPTOR_PORT = 9995;
	private static final int DEFAULT_ZOOKEEPER_PORT = 22213;

	private static final String DEFAULT_IP = NetUtils.getLocalV4Ip();

	private static final String CLUSTER_NODE_PATH = "/pinpoint-cluster/web/" + DEFAULT_IP + ":" + DEFAULT_ACCEPTOR_PORT;

	private static TestingServer ts = null;

	@Autowired
	PinpointSocketManager socketManager;

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
		Thread.sleep(5000);

		ZooKeeper zookeeper = new ZooKeeper("127.0.0.1:22213", 5000, null);
		getNodeAndCompareContents(zookeeper);

	}

	// ApplicationContext 설정에 맞게 등록이 되는지
	@Test
	public void clusterTest2() throws Exception {
		ts.restart();
		Thread.sleep(5000);

		ZooKeeper zookeeper = new ZooKeeper("127.0.0.1:22213", 5000, null);
		getNodeAndCompareContents(zookeeper);

		ts.stop();

		Thread.sleep(5000);
		try {
			zookeeper.getData(CLUSTER_NODE_PATH, null, null);
			Assert.fail();
		} catch (KeeperException e) {
			Assert.assertEquals(KeeperException.Code.CONNECTIONLOSS, e.code());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ts.restart();

		getNodeAndCompareContents(zookeeper);

	}

	// ApplicationContext 설정에 맞게 등록이 되는지
	@Test
	public void clusterTest3() throws Exception {
		ts.restart();

		PinpointSocketFactory factory = null;
		PinpointSocket socket = null;
		try {
			Thread.sleep(5000);

			ZooKeeper zookeeper = new ZooKeeper("127.0.0.1:22213", 5000, null);
			getNodeAndCompareContents(zookeeper);

			Assert.assertEquals(0, socketManager.getCollectorChannelContext().size());

			factory = new PinpointSocketFactory();
			socket = factory.connect(DEFAULT_IP, DEFAULT_ACCEPTOR_PORT, new SimpleListener());

			Thread.sleep(1000);

			Assert.assertEquals(1, socketManager.getCollectorChannelContext().size());

		} finally {
			closePinpointSocket(factory, socket);

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

}
