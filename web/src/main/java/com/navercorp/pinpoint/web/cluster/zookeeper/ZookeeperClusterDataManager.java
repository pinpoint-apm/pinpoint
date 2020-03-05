/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.cluster.zookeeper;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CuratorZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.NoNodeException;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.MapUtils;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.web.cluster.ClusterDataManager;
import com.navercorp.pinpoint.web.cluster.CollectorClusterInfoRepository;
import com.navercorp.pinpoint.web.config.WebConfig;
import com.navercorp.pinpoint.web.vo.AgentInfo;

import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author koo.taejin
 */
public class ZookeeperClusterDataManager implements ClusterDataManager, ZookeeperEventWatcher {

    private static final long PULL_RETRY_INTERVAL_TIME_MILLIS = 15 * 1000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String connectAddress;
    private final int sessionTimeout;
    private final int retryInterval;

    private ZookeeperClient client;
    private final ZookeeperClusterDataManagerHelper clusterDataManagerHelper = new ZookeeperClusterDataManagerHelper();

    private Timer timer;

    private final AtomicReference<PushWebClusterJob> job = new AtomicReference<>();

    private final CollectorClusterInfoRepository collectorClusterInfo = new CollectorClusterInfoRepository();

    private final PeriodicSyncTask periodicSyncTask;

    public ZookeeperClusterDataManager(WebConfig config) {
        this.connectAddress = config.getClusterZookeeperAddress();
        this.sessionTimeout = config.getClusterZookeeperSessionTimeout();
        this.retryInterval = config.getClusterZookeeperRetryInterval();

        if (config.isClusterZookeeperPeriodicSyncEnable()) {
            this.periodicSyncTask = new PeriodicSyncTask(config.getClusterZookeeperPeriodicSyncInterval());
        } else {
            this.periodicSyncTask = null;
        }
    }

    @Override
    public void start() throws Exception {
        this.timer = createTimer();
        this.client = new CuratorZookeeperClient(connectAddress, sessionTimeout, this);
        this.client.connect();
        if (periodicSyncTask != null) {
            this.timer.newTimeout(periodicSyncTask, periodicSyncTask.getIntervalMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void stop() {
        if (periodicSyncTask != null) {
            periodicSyncTask.stop();
        }

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
        String zNodeFullPath = ZKPaths.makePath(ZookeeperConstants.PINPOINT_WEB_CLUSTER_PATH, zNodeName);

        logger.info("registerWebCluster() started. create UniqPath={}.", zNodeFullPath);
        CreateNodeMessage createNodeMessage = new CreateNodeMessage(zNodeFullPath, contents);
        PushWebClusterJob job = new PushWebClusterJob(createNodeMessage, retryInterval);
        if (!this.job.compareAndSet(null, job)) {
            logger.warn("Already Register Web Cluster Node.");
            return false;
        }

        // successful even for scheduler registration completion
        if (!isConnected()) {
            logger.info("Zookeeper is Disconnected.");
            return true;
        }

        if (!clusterDataManagerHelper.pushZnode(client, job.getCreateNodeMessage())) {
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
        if (state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) {
            if (eventType == EventType.NodeChildrenChanged) {
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

    @Override
    public boolean handleConnected() {
        PushWebClusterJob job = this.job.get();
        if (job != null) {
            if (!clusterDataManagerHelper.pushZnode(client, job.getCreateNodeMessage())) {
                timer.newTimeout(job, job.getRetryInterval(), TimeUnit.MILLISECONDS);
                return false;
            }
        }

        if (!syncPullCollectorCluster()) {
            timer.newTimeout(new PullCollectorClusterJob(), PULL_RETRY_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleDisconnected() {
        collectorClusterInfo.clear();
        return true;
    }

    private boolean handleNodeChildrenChanged(String path) {
        if (ZookeeperConstants.PINPOINT_COLLECTOR_CLUSTER_PATH.equals(path)) {
            if (syncPullCollectorCluster()) {
                return true;
            }
            timer.newTimeout(new PullCollectorClusterJob(), PULL_RETRY_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
        }

        return false;
    }

    private boolean handleNodeDeleted(String path) {
        if (path != null) {
            String id = clusterDataManagerHelper.extractCollectorClusterId(path, ZookeeperConstants.PINPOINT_COLLECTOR_CLUSTER_PATH);
            if (id != null) {
                collectorClusterInfo.remove(id);
                return true;
            }
        }
        return false;
    }

    private boolean handleNodeDataChanged(String path) {
        if (path != null) {
            String id = clusterDataManagerHelper.extractCollectorClusterId(path, ZookeeperConstants.PINPOINT_COLLECTOR_CLUSTER_PATH);
            if (id != null) {
                if (pushCollectorClusterData(id)) {
                    return true;
                }
                timer.newTimeout(new PullCollectorClusterJob(), PULL_RETRY_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
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

    public boolean isConnected() {
        return client.isConnected();
    }

    private boolean syncPullCollectorCluster() {
        logger.info("syncPullCollectorCluster() started.");
        synchronized (this) {
            Map<String, byte[]> map = clusterDataManagerHelper.syncPullCollectorCluster(client, ZookeeperConstants.PINPOINT_COLLECTOR_CLUSTER_PATH);
            if (MapUtils.isEmpty(map)) {
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
        String path = ZKPaths.makePath(ZookeeperConstants.PINPOINT_COLLECTOR_CLUSTER_PATH, id);
        synchronized (this) {
            try {
                byte[] data = client.getData(path, true);

                collectorClusterInfo.put(id, data);
                logger.info("pushCollectorClusterData() completed.");
                return true;
            } catch (NoNodeException e) {
                logger.warn("No node path({}).", path);
                collectorClusterInfo.remove(id);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }

            return false;
        }
    }

    class PushWebClusterJob implements PushZnodeJob {
        private final CreateNodeMessage createNodeMessage;
        private final int retryInterval;

        public PushWebClusterJob(CreateNodeMessage createNodeMessage, int retryInterval) {
            this.createNodeMessage = Objects.requireNonNull(createNodeMessage, "createNodeMessage");
            this.retryInterval = retryInterval;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            logger.info("Reserved {} started.", ClassUtils.simpleClassName(this));

            if (!isConnected()) {
                return;
            }

            if (!clusterDataManagerHelper.pushZnode(client, getCreateNodeMessage())) {
                timer.newTimeout(this, getRetryInterval(), TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public CreateNodeMessage getCreateNodeMessage() {
            return createNodeMessage;
        }

        @Override
        public int getRetryInterval() {
            return retryInterval;
        }

        @Override
        public String toString() {
            return ClassUtils.simpleClassName(this) + ", createNodeMessage=" + getCreateNodeMessage();
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
                timer.newTimeout(new PullCollectorClusterJob(), PULL_RETRY_INTERVAL_TIME_MILLIS, TimeUnit.MILLISECONDS);
            }
        }
    }

    class PeriodicSyncTask implements TimerTask {

        private final long intervalMillis;
        private volatile boolean isStopped;

        public PeriodicSyncTask(long intervalMillis) {
            Assert.isTrue(intervalMillis > 0, "must be `intervalMillis > 0`)");
            this.intervalMillis = intervalMillis;
        }

        long getIntervalMillis() {
            return intervalMillis;
        }

        void stop() {
            isStopped = true;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (isStopped) {
                logger.info("PeriodicSyncTask will be discarded. message:already stopped");
                return;
            }

            logger.info("PeriodicSyncTask started");
            try {
                syncPullCollectorCluster();
            } catch (Exception e) {
                logger.warn("Failed to sync data. message:{}", e.getMessage(), e);
            }
            timer.newTimeout(this, intervalMillis, TimeUnit.MILLISECONDS);
        }

    }

}
