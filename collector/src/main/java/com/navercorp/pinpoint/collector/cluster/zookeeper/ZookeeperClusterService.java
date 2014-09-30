package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.proto.WatcherEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.cluster.AbstractClusterService;
import com.nhn.pinpoint.collector.cluster.ClusterPointRouter;
import com.nhn.pinpoint.collector.cluster.WorkerState;
import com.nhn.pinpoint.collector.cluster.WorkerStateContext;
import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.collector.util.CollectorUtils;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.SocketChannelStateChangeEventListener;

public class ZookeeperClusterService extends AbstractClusterService {

	private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
	private static final String PINPOINT_WEB_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/web";
	private static final String PINPOINT_PROFILER_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/profiler";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 해당 값이 유일한 값이 아닌 경우 MAC주소나 IP주소 등으로 변경할 예정
	// 요렇게 하면 pid@hostname 으로 나옴 (localhost 요런놈은 겹칠 가능성이 존재함)
	private final String serverIdentifier = CollectorUtils.getServerIdentifier();

	private final WorkerStateContext serviceState;

	private ZookeeperClient client;


	// ProfilerClusterManager는 프로파일러 -> 콜렉터 연결을 감지 및 관리하고, 쥬키퍼에 등록한다.
	// private ZookeeperProfilerClusterManager profilerClusterManager;
	// WebClusterManager는 웹 정보가 쥬키퍼에 등록되어 있는지 체크하고, 콜렉터 -> 웹 연결을 관리한다.
	
	private ZookeeperWebClusterManager webClusterManager;
	private ZookeeperProfilerClusterManager profilerClusterManager;

	public ZookeeperClusterService(CollectorConfiguration config, ClusterPointRouter clusterPointRouter) {
		super(config, clusterPointRouter);
		this.serviceState = new WorkerStateContext();
	}

	@PostConstruct
	@Override
	public void setUp() throws KeeperException, IOException, InterruptedException {
		if (!config.isClusterEnable()) {
			logger.info("pinpoint-collector cluster disable.");
			return;
		}
		
		switch (this.serviceState.getCurrentState()) {
			case NEW:
				if (this.serviceState.changeStateInitializing()) {
					logger.info("{} initialization started.", this.getClass().getSimpleName());
	
					// 이 상태값은 반드시 필요한것들인데.
					ClusterManagerWatcher watcher = new ClusterManagerWatcher();
					this.client = new ZookeeperClient(config.getClusterAddress(), config.getClusterSessionTimeout(), watcher);
					
					this.profilerClusterManager = new ZookeeperProfilerClusterManager(client, serverIdentifier, clusterPointRouter.getProfilerClusterPoint());
					this.profilerClusterManager.start();
	
					this.webClusterManager = new ZookeeperWebClusterManager(client, PINPOINT_WEB_CLUSTER_PATH, serverIdentifier, clusterPointRouter.getWebClusterPoint());
					this.webClusterManager.start();
	
					this.serviceState.changeStateStarted();
					logger.info("{} initialization completed.", this.getClass().getSimpleName());
	
					if (client.isConnected()) {
						WatcherEvent watcherEvent = new WatcherEvent(EventType.None.getIntValue(), KeeperState.SyncConnected.getIntValue(), "");
						WatchedEvent event = new WatchedEvent(watcherEvent);
	
						watcher.process(event);
					}
				}
				break;
			case INITIALIZING:
				logger.info("{} already initializing.", this.getClass().getSimpleName());
				break;
			case STARTED:
				logger.info("{} already started.", this.getClass().getSimpleName());
				break;
			case DESTROYING:
				throw new IllegalStateException("Already destroying.");
			case STOPPED:
				throw new IllegalStateException("Already stopped.");
			case ILLEGAL_STATE:
				throw new IllegalStateException("Invalid State.");
		}
	}

	@PreDestroy
	@Override
	public void tearDown() {
		if (!config.isClusterEnable()) {
			logger.info("pinpoint-collector cluster disable.");
			return;
		}

		if (!(this.serviceState.changeStateDestroying())) {
			WorkerState state = this.serviceState.getCurrentState();
			
			logger.info("{} already {}.", this.getClass().getSimpleName(), state.toString());
			return;
		}

		logger.info("{} destroying started.", this.getClass().getSimpleName());

		if (this.profilerClusterManager != null) {
			profilerClusterManager.stop();
		}

		if (this.webClusterManager != null) {
			webClusterManager.stop();
		}
		
		if (client != null) {
			client.close();
		}

		this.serviceState.changeStateStoped();
		logger.info("{} destroying completed.", this.getClass().getSimpleName());

		return;
	}
	
	@Override
	public boolean isEnable() {
		return config.isClusterEnable();
	}
	
	public SocketChannelStateChangeEventListener getChannelStateChangeEventListener() {
		return profilerClusterManager;
	}
	
	public ZookeeperProfilerClusterManager getProfilerClusterManager() {
		return profilerClusterManager;
	}
	
	public ZookeeperWebClusterManager getWebClusterManager() {
		return webClusterManager;
	}

	class ClusterManagerWatcher implements ZookeeperEventWatcher {

		private final AtomicBoolean connected = new AtomicBoolean(false);

		@Override
		public void process(WatchedEvent event) {
			logger.debug("Process Zookeeper Event({})", event);
			
			KeeperState state = event.getState();
			EventType eventType = event.getType();

			// 상태가 되면 ephemeral 노드가 사라짐
			// 문서에 따라 자동으로 연결이 되고, 연결되는 이벤트는 process에서 감지가 됨
			if (ZookeeperUtils.isDisconnectedEvent(state, eventType)) {
				connected.compareAndSet(true, false);
				return;
			}

			if (ZookeeperUtils.isConnectedEvent(state, eventType)) {
				// 이전상태가 RUN일수 있기 때문에 유지해도 됨
				boolean changed = connected.compareAndSet(false, true);
			}

			if (serviceState.isStarted() && connected.get()) {

				// 중복 요청이 있을수 있음 일단은 중복 로직 감안함
				if (ZookeeperUtils.isConnectedEvent(state, eventType)) {
					// 이전상태가 RUN일수 있기 때문에 유지해도 됨
					// 여기서 데이터가 있으면 다 넣어줘야함
					List<ChannelContext> currentChannelContextList = profilerClusterManager.getRegisteredChannelContextList();
					for (ChannelContext channelContext : currentChannelContextList) {
						profilerClusterManager.eventPerformed(channelContext, channelContext.getCurrentStateCode());
					}

					webClusterManager.handleAndRegisterWatcher(PINPOINT_WEB_CLUSTER_PATH);
				} else if (eventType == EventType.NodeChildrenChanged) {
					String path = event.getPath();

					if (PINPOINT_WEB_CLUSTER_PATH.equals(path)) {
						webClusterManager.handleAndRegisterWatcher(path);
					} else {
						logger.warn("Unknown Path ChildrenChanged {}.", path);
					}

				}
			}
		}

		@Override
		public boolean isConnected() {
			return connected.get();
		}

	}
	
}
