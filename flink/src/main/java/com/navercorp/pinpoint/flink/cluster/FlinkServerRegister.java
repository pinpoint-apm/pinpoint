/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.flink.cluster;

import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.flink.config.FlinkConfiguration;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.web.cluster.zookeeper.*;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author minwoo.jung
 */
public class FlinkServerRegister implements ZookeeperEventWatcher {

    private static final String PINPOINT_FLINK_CLUSTER_PATh = "/pinpoint-cluster/flink";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicReference<PushFlinkNodeJob> job = new AtomicReference<>();
    private final ZookeeperClusterDataManagerHelper clusterDataManagerHelper = new ZookeeperClusterDataManagerHelper();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final String connectAddress;
    private final int sessionTimeout;
    private final int retryInterval;
    private final boolean clusterEnable;
    private final String zNodeName;

    private ZookeeperClient client;
    private Timer timer;

    public FlinkServerRegister(FlinkConfiguration flinkConfiguration) {
        this.clusterEnable = flinkConfiguration.isFlinkClusterEnable();
        this.connectAddress = flinkConfiguration.getFlinkClusterZookeeperAddress();
        this.sessionTimeout = flinkConfiguration.getFlinkClusterSessionTimeout();
        this.retryInterval = flinkConfiguration.getFlinkRetryInterval();
        this.zNodeName = getRepresentationLocalV4Ip() +":" + flinkConfiguration.getFlinkClusterTcpPort();
    }

    @PostConstruct
    public void start() throws Exception {
        if (clusterEnable == false) {
            logger.info("pinpoint flink cluster disable.");
            return;
        }

        this.timer = createTimer();
        this.client = new ZookeeperClient(connectAddress, sessionTimeout, this, ZookeeperClient.DEFAULT_RECONNECT_DELAY_WHEN_SESSION_EXPIRED);
        this.client.connect();

        registerFlinkNode();
    }

    private String getRepresentationLocalV4Ip() {
        String ip = NetUtils.getLocalV4Ip();

        if (!ip.equals(NetUtils.LOOPBACK_ADDRESS_V4)) {
            return ip;
        }

        // local ip addresses with all LOOPBACK addresses removed
        List<String> ipList = NetUtils.getLocalV4IpList();
        if (!ipList.isEmpty()) {
            return ipList.get(0);
        }

        return NetUtils.LOOPBACK_ADDRESS_V4;
    }

    @PreDestroy
    public void stop() {
        if (clusterEnable == false) {
            logger.info("pinpoint flink cluster disable.");
            return;
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
    public boolean registerFlinkNode() {
        String zNodePath = clusterDataManagerHelper.bindingPathAndZNode(PINPOINT_FLINK_CLUSTER_PATh, zNodeName);

        logger.info("registerFlinkNode() started. create UniqPath={}.", zNodePath);

        PushFlinkNodeJob job = new PushFlinkNodeJob(zNodePath, new byte[0], retryInterval);
        if (!this.job.compareAndSet(null, job)) {
            logger.warn("Already Register flink server Node.");
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
                logger.info("zookeeper Event occurs : NodeChildrenChanged event");
            } else if (eventType == EventType.NodeDeleted) {
                logger.info("zookeeper Event occurs : NodeDeleted");
            } else if (eventType == EventType.NodeDataChanged) {
                logger.info("zookeeper Event occurs : NodeDataChanged");
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
        return true;
    }

    private boolean handleConnected() {
        boolean result = true;

        // is it ok to keep this since previous condition was possibly RUN
        boolean changed = connected.compareAndSet(false, true);
        if (changed) {
            PushFlinkNodeJob job = this.job.get();
            if (job != null) {
                if (!clusterDataManagerHelper.pushZnode(client, job)) {
                    timer.newTimeout(job, job.getRetryInterval(), TimeUnit.MILLISECONDS);
                    result = false;
                }
            }
        } else {
            result = false;
        }

        return result;
    }


    private Timer createTimer() {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-Flink-Cluster-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }


    class PushFlinkNodeJob implements PushZnodeJob {
        private final String zNodeName;
        private final byte[] contents;
        private final int retryInterval;

        public PushFlinkNodeJob(String zNodeName, byte[] contents, int retryInterval) {
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
}
