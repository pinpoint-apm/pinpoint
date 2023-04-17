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

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.ClusterService;
import com.navercorp.pinpoint.collector.cluster.ProfilerClusterManager;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterAcceptor;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionFactory;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionManager;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionRepository;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnector;
import com.navercorp.pinpoint.collector.config.CollectorClusterProperties;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CuratorZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonState;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonStateContext;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author koo.taejin
 */
public class ZookeeperClusterService implements ClusterService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final CollectorClusterProperties properties;
    private final String webZNodePath;

    private final ClusterPointRouter clusterPointRouter;

    private final String serverIdentifier = CollectorUtils.getHumanFriendlyServerIdentifier();

    private final CommonStateContext serviceState = new CommonStateContext();

    private CollectorClusterConnectionManager clusterConnectionManager;

    private ZookeeperClient client;

    // WebClusterManager checks Zookeeper for the Web data, and manages collector -> web connections.
    private ZookeeperClusterManager webClusterManager;

    // ProfilerClusterManager detects/manages profiler -> collector connections, and saves their information in Zookeeper.
    private ProfilerClusterManager profilerClusterManager;

    public ZookeeperClusterService(CollectorClusterProperties properties, ClusterPointRouter clusterPointRouter) {
        this.properties = Objects.requireNonNull(properties, "properties");
        Assert.isTrue(properties.isClusterEnable(), "clusterEnable is false");

        this.webZNodePath = Objects.requireNonNull(properties.getWebZNodePath(), "webZNodePath");

        this.clusterPointRouter = Objects.requireNonNull(clusterPointRouter, "clusterPointRouter");

        CollectorClusterConnectionRepository clusterRepository = new CollectorClusterConnectionRepository();
        CollectorClusterConnectionFactory clusterConnectionFactory = new CollectorClusterConnectionFactory(serverIdentifier, clusterPointRouter, clusterPointRouter);
        CollectorClusterConnector clusterConnector = clusterConnectionFactory.createConnector();

        CollectorClusterAcceptor clusterAcceptor = newCollectorClusterAcceptor(properties, clusterRepository, clusterConnectionFactory);

        this.clusterConnectionManager = new CollectorClusterConnectionManager(serverIdentifier, clusterRepository, clusterConnector, clusterAcceptor);

    }

    private CollectorClusterAcceptor newCollectorClusterAcceptor(CollectorClusterProperties properties,
                                                                 CollectorClusterConnectionRepository clusterRepository,
                                                                 CollectorClusterConnectionFactory clusterConnectionFactory) {
        if (StringUtils.isNotEmpty(properties.getClusterListenIp()) && properties.getClusterListenPort() > 0) {
            InetSocketAddress bindAddress = new InetSocketAddress(properties.getClusterListenIp(), properties.getClusterListenPort());
            return clusterConnectionFactory.createAcceptor(bindAddress, clusterRepository);
        }
        return null;
    }


    @Override
    public void setUp() {
        logger.info("pinpoint-collector cluster setUp");

        switch (this.serviceState.getCurrentState()) {
            case NEW:
                if (this.serviceState.changeStateInitializing()) {
                    logger.info("{} initialization started.", this.getClass().getSimpleName());

                    ClusterManagerWatcher watcher = new ClusterManagerWatcher();
                    this.client = new CuratorZookeeperClient(properties.getClusterAddress(), properties.getClusterSessionTimeout(), watcher);
                    try {
                        this.client.connect();
                    } catch (PinpointZookeeperException e) {
                        throw new RuntimeException("ZookeeperClient connect failed", e);
                    }

                    final String connectedAgentZNodePath = ZKPaths.makePath(properties.getCollectorZNodePath(), serverIdentifier);

                    this.profilerClusterManager = new ZookeeperProfilerClusterManager(client, connectedAgentZNodePath, clusterPointRouter.getTargetClusterPointRepository());
                    this.profilerClusterManager.start();

                    this.webClusterManager = new ZookeeperClusterManager(client, webZNodePath, clusterConnectionManager);
                    this.webClusterManager.start();

                    this.serviceState.changeStateStarted();
                    logger.info("{} initialization completed.", this.getClass().getSimpleName());

                    if (client.isConnected()) {
                        watcher.handleConnected();
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


    @Override
    public void tearDown() {
        logger.info("pinpoint-collector cluster tearDown");

        if (!(this.serviceState.changeStateDestroying())) {
            CommonState state = this.serviceState.getCurrentState();

            logger.info("{} already {}.", this.getClass().getSimpleName(), state);
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

        if (clusterConnectionManager != null) {
            clusterConnectionManager.stop();
        }

        this.serviceState.changeStateStopped();
        logger.info("{} destroying completed.", this.getClass().getSimpleName());
    }

    @Override
    public boolean isEnable() {
        return properties.isClusterEnable();
    }

    @Override
    public ProfilerClusterManager getProfilerClusterManager() {
        return profilerClusterManager;
    }

    class ClusterManagerWatcher implements ZookeeperEventWatcher {

        @Override
        public void process(WatchedEvent event) {
            logger.debug("Process Zookeeper Event({})", event);

            EventType eventType = event.getType();

            if (serviceState.isStarted() && client.isConnected()) {
                // duplicate event possible - but the logic does not change
                if (eventType == EventType.NodeChildrenChanged) {
                    String path = event.getPath();

                    if (webZNodePath.equals(path)) {
                        webClusterManager.handleAndRegisterWatcher(path);
                    } else {
                        logger.warn("Unknown Path ChildrenChanged {}.", path);
                    }
                }
            }
        }

        @Override
        public boolean handleConnected() {
            logger.info("Connected to zookeeper");
            if (serviceState.isStarted()) {
                profilerClusterManager.refresh();
                logger.info("Refreshed profiler cluster manager");

                webClusterManager.handleAndRegisterWatcher(webZNodePath);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean handleDisconnected() {
            logger.info("Disconnected from zookeeper");
            return true;
        }

    }

}
