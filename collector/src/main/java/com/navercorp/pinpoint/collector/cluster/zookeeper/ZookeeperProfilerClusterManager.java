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

import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.cluster.ClusterPointRepository;
import com.navercorp.pinpoint.collector.cluster.ProfilerClusterManager;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonStateContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ZookeeperProfilerClusterManager implements ProfilerClusterManager {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ClusterJobWorker<ClusterKey> worker;

    private final CommonStateContext workerState = new CommonStateContext();

    private final ClusterPointRepository<ClusterPoint<?>> profileCluster;

    private final Object lock = new Object();

    // keep it simple - register on RUN, remove on FINISHED, skip otherwise
    // should only be instantiated when cluster is enabled.
    public ZookeeperProfilerClusterManager(ZookeeperClient client, String connectedAgentZNodePath, ClusterPointRepository<ClusterPoint<?>> profileCluster) {
        this.profileCluster = Objects.requireNonNull(profileCluster, "profileCluster");

        this.worker = new ZookeeperJobWorker<>(client, connectedAgentZNodePath);
    }

    @Override
    public void start() {
        switch (this.workerState.getCurrentState()) {
            case NEW:
                if (this.workerState.changeStateInitializing()) {
                    logger.info("start() started.");

                    worker.start();
                    workerState.changeStateStarted();

                    logger.info("start() completed.");

                    break;
                }
            case INITIALIZING:
                logger.info("start() failed. caused:already initializing.");
                break;
            case STARTED:
                logger.info("start() failed. caused:already started.");
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
    public void stop() {
        if (!(this.workerState.changeStateDestroying())) {
            logger.info("stop() failed. caused:unexpected state.");
            return;
        }

        logger.info("stop() started.");

        worker.stop();
        this.workerState.changeStateStopped();

        logger.info("stop() completed.");
    }

    @Override
    public boolean isRunning() {
        return workerState.isStarted();
    }

    @Override
    public void register(ClusterPoint<?> targetClusterPoint) {
        if (workerState.isStarted()) {
            synchronized (lock) {
                ClusterKey key = targetClusterPoint.getDestClusterKey();

                boolean added = profileCluster.addAndIsKeyCreated(targetClusterPoint);
                if (key != null && added) {
                    worker.addPinpointServer(key);
                    logger.info("registered {}", targetClusterPoint);
                } else {
                    logger.warn("register failed: {}", targetClusterPoint);
                }
            }
        } else {
            logger.warn("register() failed. caused:unexpected state.");
        }
    }

    @Override
    public void unregister(ClusterPoint<?> targetClusterPoint) {
        if (workerState.isStarted()) {
            synchronized (lock) {
                ClusterKey key = targetClusterPoint.getDestClusterKey();

                boolean removed = profileCluster.removeAndGetIsKeyRemoved(targetClusterPoint);
                if (key != null && removed) {
                    worker.removePinpointServer(key);
                    logger.info("unregistered {}", targetClusterPoint);
                } else {
                    logger.warn("unregister failed, element not found: {}", targetClusterPoint);
                }
            }
        } else {
            logger.warn("unregister() failed. caused:unexpected state.");
        }
    }

    @Override
    public void refresh() {
        worker.clear();

        synchronized (lock) {
            Set<ClusterKey> availableAgentKeyList = profileCluster.getAvailableAgentKeyList();
            for (ClusterKey availableAgentKey : availableAgentKeyList) {
                worker.addPinpointServer(availableAgentKey);
            }
        }
    }

    @Override
    public List<String> getClusterData() {
        return worker.getClusterList();
    }

}
