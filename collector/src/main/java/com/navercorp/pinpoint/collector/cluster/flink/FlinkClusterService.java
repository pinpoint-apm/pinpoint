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
package com.navercorp.pinpoint.collector.cluster.flink;

import com.navercorp.pinpoint.collector.cluster.zookeeper.*;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonState;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonStateContext;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.proto.WatcherEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minwoo.jung
 */
public class FlinkClusterService {

    private static final String PINPOINT_FLINK_CLUSTER_PATH = "/pinpoint-cluster/flink";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CommonStateContext serviceState;
    private final CollectorConfiguration config;
    private final FlinkClusterConnectionManager clusterConnectionManager;

    private ZookeeperClient client;
    private ZookeeperClusterManager zookeeperClusterManager;

    public FlinkClusterService(CollectorConfiguration config, FlinkClusterConnectionManager clusterConnectionManager) {
        this.config = config;
        this.serviceState = new CommonStateContext();
        this.clusterConnectionManager = clusterConnectionManager;
    }

    @PostConstruct
    public void setUp() throws KeeperException, IOException, InterruptedException {
        if (!config.isFlinkClusterEnable()) {
            logger.info("flink cluster disable.");
            return;
        }

        switch (this.serviceState.getCurrentState()) {
            case NEW:
                if (this.serviceState.changeStateInitializing()) {
                    logger.info("{} initialization started.", this.getClass().getSimpleName());

                    ClusterManagerWatcher watcher = new ClusterManagerWatcher();
                    this.client = new DefaultZookeeperClient(config.getFlinkClusterZookeeperAddress(), config.getFlinkClusterSessionTimeout(), watcher);
                    this.client.connect();

                    this.zookeeperClusterManager = new ZookeeperClusterManager(client, PINPOINT_FLINK_CLUSTER_PATH, clusterConnectionManager);
                    this.zookeeperClusterManager.start();

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
    public void tearDown() {
        if (!config.isFlinkClusterEnable()) {
            logger.info("flink cluster disable.");
            return;
        }
        if (!(this.serviceState.changeStateDestroying())) {
            CommonState state = this.serviceState.getCurrentState();
            logger.info("{} already {}.", this.getClass().getSimpleName(), state.toString());
            return;
        }

        logger.info("{} destroying started.", this.getClass().getSimpleName());

        if (this.zookeeperClusterManager != null) {
            zookeeperClusterManager.stop();
        }
        if (client != null) {
            client.close();
        }

        this.serviceState.changeStateStopped();
        logger.info("{} destroying completed.", this.getClass().getSimpleName());
    }

    public ZookeeperClusterManager getZookeeperClusterManager() {
        return zookeeperClusterManager;
    }

    class ClusterManagerWatcher implements ZookeeperEventWatcher {

        private final AtomicBoolean connected = new AtomicBoolean(false);

        @Override
        public void process(WatchedEvent event) {
            logger.debug("Process Zookeeper Event({})", event);

            KeeperState state = event.getState();
            EventType eventType = event.getType();

            // ephemeral node is removed on disconnect event (leave node management exclusively to zookeeper)
            if (ZookeeperUtils.isDisconnectedEvent(state, eventType)) {
                connected.compareAndSet(true, false);
                if (state == KeeperState.Expired) {
                    if (client != null) {
                        client.reconnectWhenSessionExpired();
                    }
                }
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
//                    profilerClusterManager.initZookeeperClusterData();
                    zookeeperClusterManager.handleAndRegisterWatcher(PINPOINT_FLINK_CLUSTER_PATH);
                } else if (eventType == EventType.NodeChildrenChanged) {
                    String path = event.getPath();

                    if (PINPOINT_FLINK_CLUSTER_PATH.equals(path)) {
                        zookeeperClusterManager.handleAndRegisterWatcher(path);
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
