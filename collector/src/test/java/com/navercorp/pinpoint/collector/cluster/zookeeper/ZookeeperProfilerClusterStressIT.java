package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.apache.curator.test.TestingServer;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.collector.cluster.ClusterPointRouter;
import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
@Ignore
public class ZookeeperProfilerClusterStressIT {

	private static final Logger logger = LoggerFactory.getLogger(ZookeeperProfilerClusterStressIT.class);

	private static final int DEFAULT_ACCEPTOR_PORT = 22313;
	private static final int DEFAULT_ACCEPTOR_SOCKET_PORT = 22315;

	private static final MessageListener messageListener = ZookeeperTestUtils.getMessageListener();

	private static CollectorConfiguration collectorConfig = null;

	private final int doCount = 10;
	
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
		List<TestSocket> socketList = new ArrayList<ZookeeperProfilerClusterStressIT.TestSocket>();

		PinpointServerSocket pinpointServerSocket = null;
		
		TestingServer ts = null;
		try {
			ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ZookeeperProfilerClusterManager profiler = service.getProfilerClusterManager();

			pinpointServerSocket = new PinpointServerSocket(service.getChannelStateChangeEventListener());
			pinpointServerSocket.setMessageListener(ZookeeperTestUtils.getServerMessageListener());
			pinpointServerSocket.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

			InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

			socketList = connectPoint(socketList, address, doCount);
			Thread.sleep(1000);
			Assert.assertEquals(socketList.size(), profiler.getClusterData().size());
			
			for (int i=0; i < doCount; i++) {
				socketList = randomJob(socketList, address);
				Thread.sleep(1000);
				Assert.assertEquals(socketList.size(), profiler.getClusterData().size());
			}
			
			disconnectPoint(socketList, socketList.size());
			Thread.sleep(1000);
			Assert.assertEquals(0, profiler.getClusterData().size());

			service.tearDown();
		} finally {
			closeZookeeperServer(ts);
			pinpointServerSocket.close();
		}
	}
	
	@Test
	public void simpleTest2() throws Exception {

		PinpointServerSocket pinpointServerSocket = null;

		TestingServer ts = null;
		try {
			ts = ZookeeperTestUtils.createZookeeperServer(DEFAULT_ACCEPTOR_PORT);

			ZookeeperClusterService service = new ZookeeperClusterService(collectorConfig, clusterPointRouter);
			service.setUp();

			ZookeeperProfilerClusterManager profiler = service.getProfilerClusterManager();

			pinpointServerSocket = new PinpointServerSocket(service.getChannelStateChangeEventListener());
			pinpointServerSocket.setMessageListener(ZookeeperTestUtils.getServerMessageListener());
			pinpointServerSocket.bind("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

			InetSocketAddress address = new InetSocketAddress("127.0.0.1", DEFAULT_ACCEPTOR_SOCKET_PORT);

			CountDownLatch latch = new CountDownLatch(2);

			
			List<TestSocket> socketList1 = connectPoint(new ArrayList<TestSocket>(), address, doCount);
			List<TestSocket> socketList2 = connectPoint(new ArrayList<TestSocket>(), address, doCount);
			
			
			WorkerJob job1 = new WorkerJob(latch, socketList1, address, 5);
			WorkerJob job2 = new WorkerJob(latch, socketList2, address, 5);
			
			Thread worker1 = new Thread(job1);
			worker1.setDaemon(false);
			worker1.start();

			Thread worker2 = new Thread(job2);
			worker2.setDaemon(false);
			worker2.start();
			
			
			latch.await();

			List<TestSocket> socketList = new ArrayList<ZookeeperProfilerClusterStressIT.TestSocket>();
			socketList.addAll(job1.getSocketList());
			socketList.addAll(job2.getSocketList());
			
			
			logger.info(profiler.getClusterData().toString());
			Assert.assertEquals(socketList.size(), profiler.getClusterData().size());
			
			
			disconnectPoint(socketList, socketList.size());
			Thread.sleep(1000);

			service.tearDown();
		} finally {
			closeZookeeperServer(ts);
			pinpointServerSocket.close();
		}
	}
	
	class WorkerJob implements Runnable {

		private final CountDownLatch latch;
		
		private final InetSocketAddress address;
		
		private List<TestSocket> socketList;
		private int workCount;
		
		public WorkerJob(CountDownLatch latch, List<TestSocket> socketList, InetSocketAddress address ,int workCount) {
			this.latch = latch;
			this.address = address;
			
			this.socketList = socketList;
			this.workCount = workCount;
		}
		
		@Override
		public void run() {
			try {
				for (int i=0; i < workCount; i++) {
					socketList = randomJob(socketList, address);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			} finally {
				latch.countDown();
			}
		}

		public List<TestSocket> getSocketList() {
			return socketList;
		}

	}

	private void closeZookeeperServer(TestingServer mockZookeeperServer) throws Exception {
		try {
			mockZookeeperServer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class TestSocket {

		private final PinpointSocketFactory factory;
		private final Map<String, Object> properties;
		
		private PinpointSocket socket;

		
		public TestSocket() {
			this.properties = ZookeeperTestUtils.getParams(Thread.currentThread().getName(), "agent", System.currentTimeMillis());

			this.factory = new PinpointSocketFactory();
			this.factory.setProperties(properties);
			this.factory.setMessageListener(messageListener);
		}

		private void connect(InetSocketAddress address) {
			if (socket == null) {
				socket = createPinpointSocket(factory, address);
			}
		}

		private void stop() {
			if (socket != null) {
				socket.close();
				socket = null;
			}

			if (factory != null) {
				factory.release();
			}
		}
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "(" + properties + ")";
		}
		
	}

	private PinpointSocket createPinpointSocket(PinpointSocketFactory factory, InetSocketAddress address) {
		String host = address.getHostName();
		int port = address.getPort();

		PinpointSocket socket = null;
		for (int i = 0; i < 3; i++) {
			try {
				socket = factory.connect(host, port);
				logger.info("tcp connect success:{}/{}", host, port);
				return socket;
			} catch (PinpointSocketException e) {
				logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
			}
		}
		logger.warn("change background tcp connect mode  {}/{} ", host, port);
		socket = factory.scheduledConnect(host, port);

		return socket;
	}

	private List<TestSocket> randomJob(List<TestSocket> socketList, InetSocketAddress address) {
		Random random = new Random(System.currentTimeMillis());
		int randomNumber = Math.abs(random.nextInt());
		
		if (randomNumber % 2 == 0) {
			return connectPoint(socketList, address, 1);
		} else {
			return disconnectPoint(socketList, 1);
		}
	}
	
	private List<TestSocket> connectPoint(List<TestSocket> socketList, InetSocketAddress address, int count) {
//		logger.info("connect list=({}), address={}, count={}.", socketList, address, count);

		for (int i = 0; i < count; i++) {

			// startTimeStamp 혹시나 안겹치게
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			TestSocket socket = new TestSocket();
			logger.info("connect ({}), .", socket);
			socket.connect(address);

			socketList.add(socket);
		}

		return socketList;
	}

	private List<TestSocket> disconnectPoint(List<TestSocket> socketList, int count) {
//		logger.info("disconnect list=({}), count={}.", socketList, count);

		int index = 1;

		Iterator<TestSocket> iterator = socketList.iterator();
		while (iterator.hasNext()) {
			TestSocket socket = iterator.next();

			logger.info("disconnect ({}), .", socket);
			socket.stop();

			iterator.remove();

			if (index++ >= count) {
				break;
			}
		}

		return socketList;
	}

}
