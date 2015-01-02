/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.cluster;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.web.config.WebConfig;
import com.navercorp.pinpoint.web.server.PinpointSocketManager;

public class ClusterTest {

    // some tests may fail when executed in local environme    t
	// when failures happen, you have to copy pinpoint-web.properties of resource-test to resource-local. Tests will suc       eed.
	
	private static final int DEFAULT_ACCEPTOR_P    RT = 9996;
	private static final int DEFAULT_ZOOKEEPER_    ORT = 22213;

	private static final String DEFAULT_IP = NetUtils    getLocalV4Ip();

	private static final String CLUSTER_NODE_PATH = "/pinpoint-cluster/web/" + DEFAULT_IP + ":" + DEF    ULT_ACCEPTOR_PORT;

	private static Te    tingServer ts = null;

	static PinpointSo       ketManage     socketManager;
	
	@BeforeClass
	public sta       ic void setUp() throws Exception {
		             ebConfig config = mock(WebConfig.class);
		when(config.isClusterEnable()).thenReturn(true);		
		wh       n(config.getClusterTcpPort()).thenReturn(DEFAULT_ACCEPTOR_PORT);
		w       en(config.getClusterZookeeperAddress()).thenReturn("127.0.0.1:       2213");
		when(config.getClusterZookeeperRetryInterval()).thenR       turn(60000);
		when(config.getClusterZookeeper       essionTimeout()).t             enReturn(3000);

		socketManager = new Pinpo        tSocketMa    ager(config);
		socketManager.start();
		
		ts       = createZookeeperServ       r(DEFAULT_ZOOKEEP        _PORT    ;
	}

	@AfterClass
	public static void        earDow        ) t    rows Exception {
		closeZookeeperServer(ts)
		socket       anager.stop();


	@Before
	public void before() throws IOException {
		ts.stop       );
	}

	@Test
	public void cluste             Test1() throws Ex       eption {
		ts.rest             rt    );
		Thread.sleep(5000);

		ZooKeeper zooke       per = new       ZooKeeper("127.0       0.1:22213", 5000, null);
		getNodeAndCompareContents(zookeeper)
		
		if (zookeeper != null) {
		          zooke       per.close();

          }

	@Test
	public void clusterTest2() throw           Excepti       n {
		ts.restart();
		Thr          ad.sleep(5000);

		ZooKeeper zookeeper = new ZooKeeper("127.0          0.1:22213", 5000, null);
		g          tNodeAndCompar             Contents(z       okeeper);

		ts.stop();

		Thread.       leep(5000);
		try {
       		zookeeper.getDat             (C    USTER_NODE_PATH, null, null);
			Assert.fai       ();
		} ca       ch (KeeperException e) {
			Asser       .assertEquals(KeeperExcep             ion.Code.CONNECTIONL       S          , e.code());
	          	// TODO Auto-generated catch block
			e.printStack          race();
		} 

		ts.restart();

	          getNodeAndCompareContents(zookeeper);

		if (zookeeper != null) {
		             zookeeper.close();
		}
	}

	@T          st
	public void clusterTest3() throws Excep                   ion {
		ts.restart();

		PinpointSocketFactory fac          ory = null;
		          inpointSocket socket = null;
		
		ZooKeeper zookeeper = null;
		try {       			Thre          d.sleep(5000);

			zookeeper = n          w ZooKeeper("127.0          0.1:22213", 5000                       null);
			getNodeAndCompareContents(zookeeper);

			Assert.assertEquals       0, socketManager.getCollectorChannelContext().size());

       		factory = new PinpointS       cketFactory();
			facto        .setMessageListener(new SimpleListener());
			
			socket = factory.connect(DEFAULT_IP, DEFAU       T          ACCEPTOR_PORT);

			Thread.s             eep(1000);

			Asser                .assertEquals(1           socketManager             getCollectorChannelContext().size());

		} finally {
			closePinpointSocket(factory, socket);

			if (z       okeeper != null) {
			    zookeeper.close();
			}
		}
	}

	privat        static TestingServer createZookeeperServer(int port) throws        xception {
		TestingServer mockZookeeperServer         new TestingServer(port);
		mockZookeeperServer.start();
       		return mockZookeeperServer;
	}
	private static void closeZookee             erServer(TestingServer mockZookeeperServer) throws Exception {
		try {
			if (mockZoo       eeperServer != nu          l) {
				             ockZookeeperServe          .close();
		             }
		} catch (Exception e) {
			e.printStackTra       e();
       	}
	}

	private void getNodeAndCompareContents(ZooKeeper zook             eper        throws KeeperException, InterruptedException {
		byte[] conetents =           ookeeper.getData(CLUSTER_NODE          PATH, null, null);

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
