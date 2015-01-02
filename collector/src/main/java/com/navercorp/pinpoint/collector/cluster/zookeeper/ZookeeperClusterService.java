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

package com.navercorp.pinpoint.collector.cluster.zookeeper;

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

import com.navercorp.pinpoint.collector.cluster.AbstractClusterService;
import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.WebCluster;
import com.navercorp.pinpoint.collector.cluster.WorkerState;
import com.navercorp.pinpoint.collector.cluster.WorkerStateContext;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.SocketChannelStateChangeEventListener;

/**
 * @author koo.taejin
 */
public class ZookeeperClusterService extends AbstractClusterService {

    private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster    ;
	private static final String PINPOINT_WEB_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/    eb";
	private static final String PINPOINT_PROFILER_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/pro    iler";

	private final Logger logger = LoggerFactory.getLogger(this.ge    Class());

	// represented as pid@hostname (identifiers may overlap for services hosted on localhost if pids     re identical)
	// shouldn't be too big of a problem, but will change to MAC or IP if it bec    mes problematic.
	private final String serverIdentifier = CollectorUtils.get    erverIdentifier();

	private final       WebCluster webCluster;
	
	private final Work    rStateContext serviceState;

	    rivate ZookeeperClient client;

	// WebClusterManager checks Zookeeper for the Web data, and man    ges collector -> web connections.
	private Zookeep       rWebClusterManager webClusterManager;
	
	// ProfilerClusterManager detects/manages profiler -> collector connections, and saves their information in Zookeeper.
    private ZookeeperPro    ilerClusterManager profilerClusterManager;

	public ZookeeperClusterService(CollectorConfiguration c       nfig, ClusterPointRouter clust       rPointRouter) {
		super(config, clusterPo       ntRouter);
		this.serviceState = new WorkerStateContext();
		this.webCluster = new WebC        ster(serverI    entifie    , clusterPointRouter, clusterPointRouter);
	}

	@PostConstruct
	@Override
	pu       lic void setUp() throws Keep          rException, IOException, InterruptedException          {                   		if (!config.isClusterEnable()) {
		          log             er.info("pinpoint-collector cluster disabl                .");
			return;
		}
		
		switch (this.serviceState.getCurrentStat                   ()) {
			case NEW:
				if (this.serviceState.chan                eStateInitializing()) {
					logger.info("{} initialization started.", this.getClass().getSimple                               ame());
	
					ClusterManagerWatcher watcher = new ClusterManagerWatcher();
					this.client = new ZookeeperClient(config.getCl                sterAddress(), config.getC                               usterSessionTimeout(), watcher);
					
					this.profilerClusterManager = new ZookeeperProfilerCluster                anager(client, server                   dentifier, clusterPointRoute                .getTargetClusterPointRepository());
					this.profilerClusterManag                   r.start();
					                   					this.webClusterManager = new ZookeeperWebClusterManager(client, PINPOINT_WEB_CLUSTER_PATH, serverIdenti                   ier, webCluster);
					this.webClusterMa                      ager.start                                                             );
	
					this.serviceState.changeStateStarted();
					logger.i                      fo("{}             initialization completed.", this.getClass().getSimpleName())
	
					i              (client.isConnected()) {
						WatcherEvent wa          cherEve             t = new WatcherEvent(EventType.None.getIntVa          ue(), KeeperS             ate.SyncConnected.getIntValue(), "");
				             	Watched    vent ev    nt = new WatchedEvent(       atcherEvent);
	
						watche          .process(event);
					}
				}
				break;
			c          s              INITIALIZING:
				logger.info("{} already ini          ializing.", this.getClass().getSimpleName());
				                   reak;
			case STARTED:
				logger.info("{} already started.", this.get          l             ss().getSimpleName());
				break;
			case DESTROYING:
				throw new       IllegalStateException("Already destroy          ng.");
			case STOPPED:
             			throw new IllegalStateExcepti          n("Already stopped.                   );
			case I          LEGAL_STA             E:
				throw new Ill          galStateExcep                   ion("Invalid State.");
		}
	}
       	@PreDestroy
	@Override
	public void tearDown() {
		if (!config.isClu          terEna    le()) {
			logger.info("p       npoint-collector cluster dis          ble.");
			return;
		}

		if (!(this.serviceState.changeStateDestroying())) {
		       WorkerState state = this.s          rviceState.getCurrentState();
			
			logger.info("{} already {}."        this.getClass().getSimple          ame(), state.toString());
			return;
		}

		logger.info       "{} destroying starte        ", this.getClass().getSimpleName());

		if (this.profilerClus       erManager != null) {
			profilerClusterManager.stop();
		}

		       f (th       s.webClusterManager != null) {
			web          lusterManager.stop();
		}
		
		if (client !=                    ull) {
			client.close();
          	}

		if (webCluster != null) {
	          	webCluster.close();
		}
		
		this.serviceState.changeStateStopped();
		logger.info("{} dest          oying completed.", this.getClass().getSimpleName());
             }
	
	@Override
	public boolea                                isEnable() {
		retu          n config.isClusterEnable();
	}
	
	public SocketCha          nelStateChangeEventListener getChannelStateChangeEventListener() {
		return              rofilerClusterManager;
	}
	
	public ZookeeperPr                   filerClusterManager getProfilerClusterMana          er() {
		return profilerClusterManager;
	}
	
	public Zook             eperWebClusterManager getWebClusterManager() {
	                return webClusterManager;
	}

	class ClusterManagerWatcher implements ZookeeperEventWatcher {

	                private final AtomicBoolean connected = new AtomicBoole                   n(false);

		@Override
		public void process(WatchedEvent event) {
			logger.deb                               g("Process Zookeeper Event({})", event);
			
			Keepe             State state = event.getState();
			EventType eve                tType = event.getType                );

			// ephemeral node is removed                   on disconnect event (leave node manag                                  ment exclusively to zookeeper)
			if (Zo                                                       keeperUtils.is          isconnectedEvent(             tate, eventType)) {
				connected.compareAndSet(true, false);
				return;
			}

			// on connect/reconnect event
			if (ZookeeperUtils.isConnectedEvent(state, eventType)) {
			    // could already be connected (failure to compareAndSet doesn't really matter)
				boolean changed = connected.compareAndSet(false, true);
			}

			if (serviceState.isStarted() && connected.get()) {

			    // duplicate event possible - but the logic does not change
				if (ZookeeperUtils.isConnectedEvent(state, eventType)) {
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
