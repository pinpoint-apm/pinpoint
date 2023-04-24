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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CuratorZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.flink.config.FlinkProperties;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.web.cluster.zookeeper.PushZnodeJob;
import com.navercorp.pinpoint.web.cluster.zookeeper.ZookeeperClusterDataManagerHelper;
import org.apache.curator.utils.ZKPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author minwoo.jung
 */
public class FlinkServerRegister implements ZookeeperEventWatcher {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final String connectAddress;
    private final String zookeeperPath;
    private final int sessionTimeout;
    private final boolean clusterEnable;

    private final PushFlinkNodeJob pushFlinkNodeJob;

    private ZookeeperClient client;
    private ZookeeperClusterDataManagerHelper clusterDataManager;

    private Timer timer;

    public FlinkServerRegister(FlinkProperties flinkProperties) {
        Objects.requireNonNull(flinkProperties, "flinkConfiguration");
        this.clusterEnable = flinkProperties.isFlinkClusterEnable();
        this.connectAddress = flinkProperties.getFlinkClusterZookeeperAddress();
        this.sessionTimeout = flinkProperties.getFlinkClusterSessionTimeout();
        this.zookeeperPath = flinkProperties.getFlinkZNodePath();

        String zNodeName = getRepresentationLocalV4Ip() + ":" +  flinkProperties.getFlinkClusterTcpPort();
        String zNodeFullPath = ZKPaths.makePath(zookeeperPath, zNodeName);

        CreateNodeMessage createNodeMessage = new CreateNodeMessage(zNodeFullPath, new byte[0]);
        int retryInterval = flinkProperties.getFlinkRetryInterval();
        this.pushFlinkNodeJob = new PushFlinkNodeJob(createNodeMessage, retryInterval);
    }

    @PostConstruct
    public void start() throws Exception {
        if (clusterEnable == false) {
            logger.info("pinpoint flink cluster disable.");
            return;
        }

        this.timer = createTimer();
        this.client = new CuratorZookeeperClient(connectAddress, sessionTimeout, this);
        this.clusterDataManager = new ZookeeperClusterDataManagerHelper(client);
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
        logger.info("registerFlinkNode() started. create UniqPath={}.", pushFlinkNodeJob.createNodeMessage.getNodePath());

        // successful even for scheduler registration completion
        if (!isConnected()) {
            logger.info("Zookeeper is Disconnected.");
            return true;
        }

        if (!clusterDataManager.pushZnode(pushFlinkNodeJob.getCreateNodeMessage())) {
            timer.newTimeout(pushFlinkNodeJob, pushFlinkNodeJob.getRetryInterval(), TimeUnit.MILLISECONDS);
        }

        return true;
    }

    @Override
    public void process(WatchedEvent event) {
        logger.info("Handle Zookeeper Event({}) started.", event);

        KeeperState state = event.getState();
        EventType eventType = event.getType();

        if (state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) {
            // when this happens, ephemeral node disappears
            // reconnects automatically, and process gets notified for all events
            if (eventType == EventType.NodeChildrenChanged) {
                logger.info("zookeeper Event occurs : NodeChildrenChanged event");
            } else if (eventType == EventType.NodeDeleted) {
                logger.info("zookeeper Event occurs : NodeDeleted");
            } else if (eventType == EventType.NodeDataChanged) {
                logger.info("zookeeper Event occurs : NodeDataChanged");
            }
        }
        logger.info("Handle Zookeeper Event({}) completed.", event);
    }

    @Override
    public boolean handleDisconnected() {
        return true;
    }

    @Override
    public boolean handleConnected() {
        if (clusterDataManager.pushZnode(pushFlinkNodeJob.getCreateNodeMessage())) {
            return true;
        } else {
            timer.newTimeout(pushFlinkNodeJob, pushFlinkNodeJob.getRetryInterval(), TimeUnit.MILLISECONDS);
            return false;
        }
    }

    private Timer createTimer() {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-Flink-Cluster-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    class PushFlinkNodeJob implements PushZnodeJob {
        private final CreateNodeMessage createNodeMessage;
        private final int retryInterval;

        public PushFlinkNodeJob(CreateNodeMessage createNodeMessage, int retryInterval) {
            this.createNodeMessage = Objects.requireNonNull(createNodeMessage, "createNodeMessage");
            this.retryInterval = retryInterval;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            logger.info("Reserved {} started.", ClassUtils.simpleClassName(this));

            if (!isConnected()) {
                return;
            }

            if (!clusterDataManager.pushZnode(getCreateNodeMessage())) {
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

}
