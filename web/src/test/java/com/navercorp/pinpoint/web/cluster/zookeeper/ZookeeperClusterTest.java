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

package com.navercorp.pinpoint.web.cluster.zookeeper;

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

import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.web.cluster.zookeeper.ZookeeperClusterManager;

public class ZookeeperClusterTest {

    private static final int DEFAULT_ACCEPTOR_PORT = 999    ;
	private static final int DEFAULT_ZOOKEEPER_PORT = 22    13;

	private static final String DEFAULT_IP = NetUtils.getLocal    4Ip();

	private static final String COLLECTOR_NODE_PATH = "/pinpoint-cluster/    ollector";
	private static final String COLLECTOR_TEST_NODE_PATH = "/pinpoint-cluster/co       lector/test";
	
	private static final String CLUSTER_NODE_PATH = "/pinpoint-cluster/web/" + DEFAULT_IP + ":" + DEF    ULT_ACCEPTOR_PORT;

	private final Logger logger = LoggerFactory.getL       gger(this.getClass());
	
	private sta    ic Testing    erver ts = null;

	@BeforeClass
	public sta       ic void setUp() throws Exception {
		ts = creat        ookeeperS    rver(DEFAULT_ZOOKEEPER_PORT);
	}

	@AfterClass       	public static void t        rDown    ) throws Exception {
		closeZookeeperSe       ver(ts
	}

	@Before
	public void before() throws IOException {
		ts.stop();
	}

	// te    t f    r zookeeper agents to be registered correct       y at the c       uster as expected
	@Tes
	public void clusterTest1() throws       E          ception {
		ts.restart();

		ZooKeeper zookeeper = null;
		ZookeeperCluster          anager manager = null;
		try {
			zookeeper = ne           ZooKeeper(DEFAULT_IP + ":" + DEFAULT_ZOOKEEPER_PORT, 5000, nu                   l);
			createPath(zookeeper, COLLECTOR_TEST_NODE_PATH, true);
			zookeeper.setData(          OLLECTOR_TEST_          ODE_PATH, "a:b:1".getBytes(), -1);
			
			manager = new Zookeepe          ClusterManager(DEFAULT_IP + ":" + D          FAULT_ZOOKEEPER_PORT, 5000, 60000);
			Th          ead.sleep(3000);

			List<String> agentList = manag          r.getRegisteredAgentList("a", "b",                    L);
			Assert.assertEquals(1, agentList.size());
			          ssert.assertEq          als("test", agentList.get(0));

			agentList = mana          er.getRegisteredAgentList("b", "c",       1L);
		          Assert.assertEqual             (0, agentL                            st.size             ));
			
                            		zookeeper.setData(COLLECTOR_TEST_NODE_       ATH, "".ge       Bytes(), -1);
			Thread       sleep(3000);

			agentList = manage       .          etRegisteredAgentList("a", "b", 1L);
			Assert.assertEquals(0, agentList.si          e());
		} finally {
			if (zookeeper != null) {
          			zookeeper.close();
			}
			
			if (manager != null) {
				m                   nager.close();
			}
		}
	}
	
	@Test
	public void clusterTest2() throws Exception {
          	ts.restart();
		ZooKeeper zookeeper = null;
		ZookeeperClusterManager manager          = null;
		try {
			zookeeper = new           ooKeeper(DEFAULT_IP + ":" + DEFAULT_ZOOKE          PER_PORT, 5000, null);
			createPath(zookeeper, COLLECTOR_TEST_NODE_PAT          , true);
			zoo          eeper.setData(COLLECTOR_TEST_NODE_PATH, "a:b:1".get          ytes(), -1);
			
			manager = new Z          okeeperClusterManager(DEFAULT_IP + ":" +           EFAULT_ZOOKEEPER_PORT, 5000, 60000);
			Thread.slee          (3000);

			List<String> agentList            manager.getRegisteredAgentList("a", "b",          1L);
			Assert.assertEquals(1, agentList.          ize());
			As                   ert.assertEquals("test", agentList.get(0));

	          	zookeeper.setData(COLLECTOR_TEST_N                   DE_PATH, "a:b:1\r\nc:d:2".getBytes(), -1);
			          hread.sleep(3000);


			agentList =       manager          getRegisteredAgent             ist("a", "                            ", 1L);             			Asser                      .assertEquals(1, agentList.size());
			Assert.assertEquals("test", agent       ist.get(0));

			agentList = manager.getRegisteredAgentL       st("c", "d", 2L);
			Asse       t.assertEquals(1, agent        st.size());
			Assert.assertEquals("test", agentList.get(0));

			zookeeper.delete(COLLECTOR       T          ST_NODE_PATH, -1);
			Thread             sleep(3000);
			
			                gentList = mana          er.getRegister             dAgentList("a", "b", 1L);
			Assert.assertEquals(0, agentList.size());
			
			agentList = manager.getRe       isteredAgentList("c", "d", 2L);
			Assert.assertEquals(0, agentLi       t.size());
		} finally {
			if (zookeeper != null) {
				zook       eper.close();
			}
			
			if (manager != null)
				manager.close();
			}
		}
	}

	private static Testin       Server createZookeeperServer(int          port) throws Exception {
		Testin             Server mockZookeeperServer = new TestingServer(port);
		mockZookeeperServer.start();
       		return mockZook          eperServe             ;
	}

	private st          tic void clo             eZookeeperServer(TestingServer mockZookeeperSe       ver)        hrows Exception {
		try {
			if (mockZookeeperServer != null)             {
		       	mockZookeeperServer.close();
			}
		} catch (Exception e) {
			e.pri          tStackTrace();
		}
	}

	priva             e void getNodeAndCompareContents(ZooKeeper zookeeper) throws KeeperException, InterruptedException {
		byte[] conetents = zookeeper.getData(CLUSTER_NODE       PATH, nu                 , null);

		String[] registe          edIplist =             new String(c                   netents).split("\r             n");

		List<                                              tring> ipList = NetUtil          .getLocalV4IpList();

		Assert.assertEqua             s                   registeredIplist.length, ipList.size());

		for (String ip : registeredIplist) {
			Assert          assertTrue(ipList.contains(ip));
		}
	}

       private void closePinpoint    ocket(PinpointSocketFactory factory, PinpointSocket socket) {
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
