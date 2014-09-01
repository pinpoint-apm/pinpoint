package com.nhn.pinpoint.web.cluster.zookeeper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.util.TimerFactory;
import com.nhn.pinpoint.web.cluster.ClusterManager;

/**
 * @author koo.taejin <kr14910>
 */
public class ZookeeperClusterManager implements ClusterManager, Watcher {

	private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
	private static final String PINPOINT_WEB_CLUSTER_PATh = PINPOINT_CLUSTER_PATH + "/web";

	private static final String PATH_SEPERATOR = "/";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final AtomicBoolean connected = new AtomicBoolean(false);
	private final ZookeeperClient client;

	private final int retryInterval;
	
	private final Timer timer;

	private final AtomicReference<RegisterJob> job = new AtomicReference<ZookeeperClusterManager.RegisterJob>();

	public ZookeeperClusterManager(String zookeeperAddress, int sessionTimeout, int retryInterval) throws KeeperException, IOException, InterruptedException {
		this.client = new ZookeeperClient(zookeeperAddress, sessionTimeout, this);
		this.retryInterval = retryInterval;
		// 등록이 실패하였을때 생성하게 하는게 나을수도 있음
		this.timer = createTimer();
	}

	// 등록이 실패해도 계속 시도 (주기는 기본 1분)
	// 크게 부하가 가는 작업이 아니며, 
	// 실패할 경우 계속 로그를 출력
	@Override
	public boolean registerWebCluster(String zNodeName, byte[] contents) {
		String zNodePath = bindingPathAndZnode(PINPOINT_WEB_CLUSTER_PATh, zNodeName);

		logger.info("Create Web Cluster Zookeeper UniqPath = {}", zNodePath);

		RegisterJob job = new RegisterJob(zNodePath, contents, retryInterval);
		if (!this.job.compareAndSet(null, job)) {
			logger.warn("Already Register Web Cluster Node.");
			return false;
		}

		// 스케쥴로 라도 등록하면 성공
		registerWebCluster(job);
		return true;
	}

	@Override
	public void process(WatchedEvent event) {
		KeeperState state = event.getState();
		EventType eventType = event.getType();

		// 상태가 되면 ephemeral 노드가 사라짐
		// 문서에 따라 자동으로 연결이 되고, 연결되는 이벤트는 process에서 감지가 됨
		if (state == KeeperState.Disconnected || state == KeeperState.Expired) {
			connected.compareAndSet(true, false);
			return;
		}

		if ((state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) && eventType == EventType.None) {
			// 이전상태가 RUN일수 있기 때문에 유지해도 됨
			boolean changed = connected.compareAndSet(false, true);
			if (changed) {
				RegisterJob job = this.job.get();
				if (job != null) {
					registerWebCluster(job);
				}
			}
			return;
		}
	}

	@Override
	public void close() {
		if (timer != null) {
			timer.stop();
		}

		if (client != null) {
			this.client.close();
		}
	}

	private Timer createTimer() {
		HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-Web-Cluster-Timer", 100, TimeUnit.MILLISECONDS, 512);
		timer.start();
		return timer;
	}

	private boolean registerWebCluster(RegisterJob job) {
		String zNodePath = job.getZnodePath();
		byte[] contents = job.getContents();

		if (!isConnected()) {
			logger.info("Web Cluster Zookeeper is Disconnected. This job retry when reconnected. Path={}", zNodePath);
			return false;
		}

		try {
			if (!client.exists(zNodePath)) {
				client.createPath(zNodePath);
			}

			// 쥬키퍼의 zNode는 ip:port 형태의 이름으로 만들수 있음
			String nodeName = client.createNode(zNodePath, contents, CreateMode.EPHEMERAL);
			logger.info("Register Web Cluster Zookeeper UniqPath = {}.", zNodePath);
			return true;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		reservationRegisterWebCluster(job);
		return false;
	}

	private void reservationRegisterWebCluster(RegisterJob job) {
		timer.newTimeout(job, job.getRetryInterval(), TimeUnit.MILLISECONDS);
	}

	public boolean isConnected() {
		return connected.get();
	}

	private String bindingPathAndZnode(String path, String znodeName) {
		StringBuilder fullPath = new StringBuilder();

		fullPath.append(path);
		if (!path.endsWith(PATH_SEPERATOR)) {
			fullPath.append(PATH_SEPERATOR);
		}
		fullPath.append(znodeName);

		return fullPath.toString();
	}

	class RegisterJob implements TimerTask {
		private final String znodeName;
		private final byte[] contents;
		private final int retryInterval;
		
		public RegisterJob(String znodeName, byte[] contents, int retryInterval) {
			this.znodeName = znodeName;
			this.contents = contents;
			this.retryInterval = retryInterval;
		}

		public String getZnodePath() {
			return znodeName;
		}

		public byte[] getContents() {
			return contents;
		}

		public int getRetryInterval() {
			return retryInterval;
		}
		
		@Override
		public String toString() {
			StringBuilder toString = new StringBuilder();
			toString.append(this.getClass().getSimpleName());
			toString.append(", Znode=" + getZnodePath());

			return toString.toString();
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			registerWebCluster(this);
		}

	}

}
