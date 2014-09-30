package com.nhn.pinpoint.web.cluster.zookeeper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.nhn.pinpoint.web.cluster.CollectorClusterInfoRepository;

/**
 * @author koo.taejin <kr14910>
 */
public class ZookeeperClusterManager implements ClusterManager, Watcher {

	private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
	private static final String PINPOINT_WEB_CLUSTER_PATh = PINPOINT_CLUSTER_PATH + "/web";
	private static final String PINPOINT_COLLECTOR_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/collector";

	private static final long SYNC_INTERVAL_TIME_MILLIS = 15 * 1000;

	private static final String PATH_SEPERATOR = "/";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final AtomicBoolean connected = new AtomicBoolean(false);
	private final ZookeeperClient client;

	private final int retryInterval;
	
	private final Timer timer;

	private final AtomicReference<PushWebClusterJob> job = new AtomicReference<ZookeeperClusterManager.PushWebClusterJob>();

	private final CollectorClusterInfoRepository collectorClusterInfo = new CollectorClusterInfoRepository();
	
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

		PushWebClusterJob job = new PushWebClusterJob(zNodePath, contents, retryInterval);
		if (!this.job.compareAndSet(null, job)) {
			logger.warn("Already Register Web Cluster Node.");
			return false;
		}

		// 스케쥴로 라도 등록하면 성공
		if (!isConnected()) {
			logger.info("Zookeeper is Disconnected.");
			return true;
		}
		
		if (!syncWebCluster(job)) {
			timer.newTimeout(job, job.getRetryInterval(), TimeUnit.MILLISECONDS);
		}

		return true;
	}

	@Override
	public void process(WatchedEvent event) {
		logger.info("Zookeepr Event({}) ocurred.", event);
		
		KeeperState state = event.getState();
		EventType eventType = event.getType();
		String path = event.getPath();

		boolean result = false;
		
		// 상태가 되면 ephemeral 노드가 사라짐
		// 문서에 따라 자동으로 연결이 되고, 연결되는 이벤트는 process에서 감지가 됨
		if (state == KeeperState.Disconnected || state == KeeperState.Expired) {
			result = handleDisconnected();
		} else if ((state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) && eventType == EventType.None) {
			result = handleConnected();
		} else if ((state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) && eventType == EventType.NodeChildrenChanged) {
			result = handleNodeChildrenChanged(path);
		} else if ((state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) && eventType == EventType.NodeDeleted) {
			result = handleNodeDeleted(path);
		} else if ((state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) && eventType == EventType.NodeDataChanged) {
			result = handleNodeDataChanged(path);
		}
		
		if (result) {
			logger.info("Zookeeper Event({}) successed.", event); 
		} else {
			logger.info("Zookeeper Event({}) failed.", event); 
		}
	}
	
	private boolean handleDisconnected() {
		connected.compareAndSet(true, false);
		collectorClusterInfo.clear();
		return true;
	}
	
	private boolean handleConnected() {
		boolean result = true;
		
		// 이전상태가 RUN일수 있기 때문에 유지해도 됨
		boolean changed = connected.compareAndSet(false, true);
		if (changed) {
			PushWebClusterJob job = this.job.get();
			if (job != null) {
				if (!syncWebCluster(job)) {
					timer.newTimeout(job, job.getRetryInterval(), TimeUnit.MILLISECONDS);
					result = false;
				}
			}
			
			if (!syncCollectorCluster()) {
				timer.newTimeout(new FetchCollectorClusterJob(), SYNC_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
				result = false;
			}
		} else {
			result = false;
		}
		
		return result;
	}
	
	private boolean handleNodeChildrenChanged(String path) {
		if (PINPOINT_COLLECTOR_CLUSTER_PATH.equals(path)) {
			if (syncCollectorCluster()) {
				return true;
			}
			timer.newTimeout(new FetchCollectorClusterJob(), SYNC_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
		}
		
		return false;
	}
	
	private boolean handleNodeDeleted(String path) {
		if (path != null) {
			String id = extractCollectorClusterId(path);
			if (id != null) {
				collectorClusterInfo.remove(id);
				return true;
			}
		}
		return false;
	}
	
	private boolean handleNodeDataChanged(String path) {
		if (path != null) {
			String id = extractCollectorClusterId(path);
			if (id != null) {
				if (syncCollectorCluster(id)) {
					return true;
				} 
				timer.newTimeout(new FetchCollectorClusterJob(), SYNC_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
			}
		}
		
		return false;
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

	@Override
	public List<String> getRegisteredAgentList(String applicationName, String agentId, long startTimeStamp) {
		return collectorClusterInfo.get(applicationName, agentId, startTimeStamp);
	}
	
	private Timer createTimer() {
		HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-Web-Cluster-Timer", 100, TimeUnit.MILLISECONDS, 512);
		timer.start();
		return timer;
	}

	private boolean syncWebCluster(PushWebClusterJob job) {
		String zNodePath = job.getZnodePath();
		byte[] contents = job.getContents();

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
		return false;
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
	
	private String extractCollectorClusterId(String path) {
		int index  = path.indexOf(PINPOINT_COLLECTOR_CLUSTER_PATH);
		
		int startPosition = index + PINPOINT_COLLECTOR_CLUSTER_PATH.length() + 1;
		
		if (path.length() > startPosition) {
			String id = path.substring(startPosition);
			return id;
		}

		return null;
	}
	
	private boolean syncCollectorCluster() {
		synchronized (this) {
			Map<String, byte[]> map = getCollectorData();
			
			if (map == null) {
				return false;
			}

			for (Map.Entry<String, byte[]> entry : map.entrySet()) {
				String key = entry.getKey();
				byte[] value = entry.getValue();
				
				String id = extractCollectorClusterId(key);
				if (id == null) {
					logger.error("Illegal Collector Path({}) finded.", key);
					continue;
				}
				collectorClusterInfo.put(id, value);
			}
			
			return true;
		}
	}
	
	private boolean syncCollectorCluster(String id) {
		synchronized (this) {
			try {
				String key = bindingPathAndZnode(PINPOINT_COLLECTOR_CLUSTER_PATH, id);
				byte[] data = client.getData(key, true);
				
				collectorClusterInfo.put(id, data);
				return true;
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
			
			return false;
		}
	}

	private Map<String, byte[]> getCollectorData() {
		try {
			List<String> collectorList = client.getChildren(PINPOINT_COLLECTOR_CLUSTER_PATH, true);

			Map<String, byte[]> map = new HashMap<String, byte[]>();
			
			for (String collector : collectorList) {
				String node = bindingPathAndZnode(PINPOINT_COLLECTOR_CLUSTER_PATH, collector);
				
				byte[] data = client.getData(node, true);
				map.put(node, data);
			}
			
			return map;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		
		return null;
	}


	class PushWebClusterJob implements TimerTask {
		private final String znodeName;
		private final byte[] contents;
		private final int retryInterval;
		
		public PushWebClusterJob(String znodeName, byte[] contents, int retryInterval) {
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
			logger.info("Reservation Job({}) started.", this.getClass().getSimpleName());
			
			if (!isConnected()) {
				return;
			}
			
			if (!syncWebCluster(this)) {
				timer.newTimeout(this, getRetryInterval(), TimeUnit.MILLISECONDS);
			}
		}
	}
	
	class FetchCollectorClusterJob implements TimerTask {
		
		@Override
		public void run(Timeout timeout) throws Exception {
			logger.info("Reservation Job({}) started.", this.getClass().getSimpleName());

			if (!isConnected()) {
				return;
			}
			
			if (!syncCollectorCluster()) {
				timer.newTimeout(new FetchCollectorClusterJob(), SYNC_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
			}
		}
	}

}
