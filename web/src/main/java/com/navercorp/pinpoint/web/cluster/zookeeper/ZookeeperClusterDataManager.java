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

import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.web.cluster.ClusterDataManager;
import com.navercorp.pinpoint.web.cluster.CollectorClusterInfoRepository;
import com.navercorp.pinpoint.web.cluster.zookeeper.exception.NoNodeException;
import com.navercorp.pinpoint.web.config.WebConfig;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author koo.taejin
 */
public class ZookeeperClusterDataManager implements ClusterDataManager, ZookeeperEventWatcher {

    private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
    private static final String PINPOINT_WEB_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/web";
    private static final String PINPOINT_COLLECTOR_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/collector";
    private static final long SYNC_INTERVAL_TIME_MILLIS = 15 * 1000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String connectAddress;
    private final int sessionTimeout;
    private final int retryInterval;

    private final AtomicBoolean connected = new AtomicBoolean(false);
    private ZookeeperClient client;
    private final ZookeeperClusterDataManagerHelper clusterDataManagerHelper = new ZookeeperClusterDataManagerHelper();

    private Timer timer;

    private final AtomicReference<PushWebClusterJob> job = new AtomicReference<>();

    private final CollectorClusterInfoRepository collectorClusterInfo = new CollectorClusterInfoRepository();

    public ZookeeperClusterDataManager(WebConfig config) {
        this(config.getClusterZookeeperAddress(), config.getClusterZookeeperSessionTimeout(), config.getClusterZookeeperRetryInterval());
    }

    public ZookeeperClusterDataManager(String connectAddress, int sessionTimeout, int retryInterval) {
        this.connectAddress = connectAddress;
        this.sessionTimeout = sessionTimeout;
        this.retryInterval = retryInterval;
    }

    @Override
    public void start() throws Exception {
        this.timer = createTimer();
        this.client = new ZookeeperClient(connectAddress, sessionTimeout, this, ZookeeperClient.DEFAULT_RECONNECT_DELAY_WHEN_SESSION_EXPIRED);
        this.client.connect();
    }

    @Override
    public void stop() {
        if (timer != null) {
            timer.stop();
        }

        if (client != null) {
            this.client.close();
        }
    }

    // Retry upon failure (1 min retry period)
    // not too much overhead, just logging
    @Override
    public boolean registerWebCluster(String zNodeName, byte[] contents) {
        String zNodePath = clusterDataManagerHelper.bindingPathAndZNode(PINPOINT_WEB_CLUSTER_PATH, zNodeName);

        logger.info("registerWebCluster() started. create UniqPath={}.", zNodePath);

        PushWebClusterJob job = new PushWebClusterJob(zNodePath, contents, retryInterval);
        if (!this.job.compareAndSet(null, job)) {
            logger.warn("Already Register Web Cluster Node.");
            return false;
        }

        // successful even for scheduler registration completion
        if (!isConnected()) {
            logger.info("Zookeeper is Disconnected.");
            return true;
        }

        if (!clusterDataManagerHelper.pushZnode(client, job)) {
            timer.newTimeout(job, job.getRetryInterval(), TimeUnit.MILLISECONDS);
        }

        return true;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void process(WatchedEvent event) {
        logger.info("Handle Zookeeper Event({}) started.", event);
        
        KeeperState state = event.getState();
        EventType eventType = event.getType();
        String path = event.getPath();

        // when this happens, ephemeral node disappears
        // reconnects automatically, and process gets notified for all events
        boolean result = false;
        if (ZookeeperUtils.isDisconnectedEvent(event)) {
            result = handleDisconnected();
            if (state == KeeperState.Expired) {
                client.reconnectWhenSessionExpired();
            }
        } else if (state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) {
            if (eventType == EventType.None) {
                result = handleConnected();
            } else if (eventType == EventType.NodeChildrenChanged) {
                result = handleNodeChildrenChanged(path);
            } else if (eventType == EventType.NodeDeleted) {
                result = handleNodeDeleted(path);
            } else if (eventType == EventType.NodeDataChanged) {
                result = handleNodeDataChanged(path);
            }
        }

        if (result) {
            logger.info("Handle Zookeeper Event({}) completed.", event);
        } else {
            logger.info("Handle Zookeeper Event({}) failed.", event);
        }
    }

    private boolean handleDisconnected() {
        connected.compareAndSet(true, false);
        collectorClusterInfo.clear();
        return true;
    }

    private boolean handleConnected() {
        boolean result = true;

        // is it ok to keep this since previous condition was possibly RUN
        boolean changed = connected.compareAndSet(false, true);
        if (changed) {
            PushWebClusterJob job = this.job.get();
            if (job != null) {
                if (!clusterDataManagerHelper.pushZnode(client, job)) {
                    timer.newTimeout(job, job.getRetryInterval(), TimeUnit.MILLISECONDS);
                    result = false;
                }
            }

            if (!syncPullCollectorCluster()) {
                timer.newTimeout(new PullCollectorClusterJob(), SYNC_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
                result = false;
            }
        } else {
            result = false;
        }

        return result;
    }

    private boolean handleNodeChildrenChanged(String path) {
        if (PINPOINT_COLLECTOR_CLUSTER_PATH.equals(path)) {
            if (syncPullCollectorCluster()) {
                return true;
            }
            timer.newTimeout(new PullCollectorClusterJob(), SYNC_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
        }

        return false;
    }

    private boolean handleNodeDeleted(String path) {
        if (path != null) {
            String id = clusterDataManagerHelper.extractCollectorClusterId(path, PINPOINT_COLLECTOR_CLUSTER_PATH);
            if (id != null) {
                collectorClusterInfo.remove(id);
                return true;
            }
        }
        return false;
    }

    private boolean handleNodeDataChanged(String path) {
        if (path != null) {
            String id = clusterDataManagerHelper.extractCollectorClusterId(path, PINPOINT_COLLECTOR_CLUSTER_PATH);
            if (id != null) {
                if (pushCollectorClusterData(id)) {
                    return true;
                }
                timer.newTimeout(new PullCollectorClusterJob(), SYNC_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
            }
        }

        return false;
    }

    @Override
    public List<String> getRegisteredAgentList(AgentInfo agentInfo) {
        return getRegisteredAgentList(agentInfo.getApplicationName(), agentInfo.getAgentId(), agentInfo.getStartTimestamp());
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

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    private boolean syncPullCollectorCluster() {
        logger.info("syncPullCollectorCluster() started.");
        synchronized (this) {
            Map<String, byte[]> map = clusterDataManagerHelper.syncPullCollectorCluster(client, PINPOINT_COLLECTOR_CLUSTER_PATH);
            if (Collections.EMPTY_MAP == map) {
                return false;
            }

            logger.info("Get collector({}) info.", map.keySet());
            for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                collectorClusterInfo.put(entry.getKey(), entry.getValue());
            }

            logger.info("syncPullCollectorCluster() completed.");
            return true;
        }
    }

    private boolean pushCollectorClusterData(String id) {
        logger.info("pushCollectorClusterData() started.");
        String path = clusterDataManagerHelper.bindingPathAndZNode(PINPOINT_COLLECTOR_CLUSTER_PATH, id);
        synchronized (this) {
            try {
                byte[] data = client.getData(path, true);

                collectorClusterInfo.put(id, data);
                logger.info("pushCollectorClusterData() completed.");
                return true;
            } catch(NoNodeException e) {
                logger.warn("No node path({}).", path);
                collectorClusterInfo.remove(id);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }

            return false;
        }
    }

    class PushWebClusterJob implements PushZnodeJob {
        private final String zNodeName;
        private final byte[] contents;
        private final int retryInterval;

        public PushWebClusterJob(String zNodeName, byte[] contents, int retryInterval) {
            this.zNodeName = zNodeName;
            this.contents = contents;
            this.retryInterval = retryInterval;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            logger.info("Reserved {} started.", ClassUtils.simpleClassName(this));

            if (!isConnected()) {
                return;
            }

            if (!clusterDataManagerHelper.pushZnode(client, this)) {
                timer.newTimeout(this, getRetryInterval(), TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public String getZNodePath() {
            return zNodeName;
        }

        @Override
        public byte[] getContents() {
            return contents;
        }

        @Override
        public int getRetryInterval() {
            return retryInterval;
        }

        @Override
        public String toString() {
            return ClassUtils.simpleClassName(this) + ", ZNode=" + getZNodePath();
        }

    }

    class PullCollectorClusterJob implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            logger.info("Reserved {} started.", ClassUtils.simpleClassName(this));

            if (!isConnected()) {
                return;
            }

            if (!syncPullCollectorCluster()) {
                timer.newTimeout(new PullCollectorClusterJob(), SYNC_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
            }
        }
    }

}
