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
package com.navercorp.pinpoint.collector.flink;

import com.navercorp.pinpoint.collector.config.FlinkProperties;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CuratorZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonState;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonStateContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class FlinkClusterService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final CommonStateContext serviceState;
    private final FlinkProperties properties;
    private final FlinkClusterConnectionManager clusterConnectionManager;
    private final String pinpointFlinkClusterPath;

    private ZookeeperClient client;
    private ZookeeperClusterManager zookeeperClusterManager;

    public FlinkClusterService(FlinkProperties properties, FlinkClusterConnectionManager clusterConnectionManager) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.serviceState = new CommonStateContext();
        this.clusterConnectionManager = Objects.requireNonNull(clusterConnectionManager, "clusterConnectionManager");
        this.pinpointFlinkClusterPath = properties.getFlinkZNodePath();
    }

    @PostConstruct
    public void setup() {
        if (!properties.isFlinkClusterEnable()) {
            logger.info("flink cluster disable.");
            return;
        }

        switch (this.serviceState.getCurrentState()) {
            case NEW -> {
                if (this.serviceState.changeStateInitializing()) {
                    logger.info("{} initialization started.", this.getClass().getSimpleName());

                    ClusterManagerWatcher watcher = new ClusterManagerWatcher(pinpointFlinkClusterPath);
                    this.client = new CuratorZookeeperClient(properties.getFlinkClusterZookeeperAddress(), properties.getFlinkClusterSessionTimeout(), watcher);
                    try {
                        this.client.connect();
                    } catch (PinpointZookeeperException e) {
                        throw new RuntimeException("ZookeeperClient connect failed", e);
                    }

                    this.zookeeperClusterManager = new ZookeeperClusterManager(client, pinpointFlinkClusterPath, clusterConnectionManager);
                    this.zookeeperClusterManager.start();

                    this.serviceState.changeStateStarted();
                    logger.info("{} initialization completed.", this.getClass().getSimpleName());

                    if (client.isConnected()) {
                        watcher.handleConnected();
                    }
                }
            }
            case INITIALIZING -> logger.info("{} already initializing.", this.getClass().getSimpleName());
            case STARTED -> logger.info("{} already started.", this.getClass().getSimpleName());
            case DESTROYING -> throw new IllegalStateException("Already destroying.");
            case STOPPED -> throw new IllegalStateException("Already stopped.");
            case ILLEGAL_STATE -> throw new IllegalStateException("Invalid State.");
        }
    }

    @PreDestroy
    public void tearDown() {
        if (!properties.isFlinkClusterEnable()) {
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

    private class ClusterManagerWatcher implements ZookeeperEventWatcher {

        private final String pinpointFlinkClusterPath;

        public ClusterManagerWatcher(String pinpointFlinkClusterPath) {
            this.pinpointFlinkClusterPath = pinpointFlinkClusterPath;
        }

        @Override
        public void process(WatchedEvent event) {
            logger.debug("Process Zookeeper Event({})", event);

            EventType eventType = event.getType();

            if (serviceState.isStarted() && client.isConnected()) {
                // duplicate event possible - but the logic does not change
                if (eventType == EventType.NodeChildrenChanged) {
                    String eventPath = event.getPath();

                    if (pinpointFlinkClusterPath.equals(eventPath)) {
                        zookeeperClusterManager.handleAndRegisterWatcher(eventPath);
                    } else {
                        logger.warn("Unknown Path ChildrenChanged {}.", eventPath);
                    }
                }
            }
        }

        @Override
        public boolean handleConnected() {
            if (serviceState.isStarted()) {
                zookeeperClusterManager.handleAndRegisterWatcher(pinpointFlinkClusterPath);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean handleDisconnected() {
            return true;
        }

    }

}
