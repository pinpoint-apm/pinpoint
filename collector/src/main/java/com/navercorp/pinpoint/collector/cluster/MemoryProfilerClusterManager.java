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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonStateContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class MemoryProfilerClusterManager implements ProfilerClusterManager {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final CommonStateContext workerState = new CommonStateContext();
    private final ClusterPointRepository<ClusterPoint<?>> profileCluster;
    private final Object lock = new Object();

    // keep it simple - register on RUN, remove on FINISHED, skip otherwise
    // should only be instantiated when cluster is enabled.
    public MemoryProfilerClusterManager(ClusterPointRepository<ClusterPoint<?>> profileCluster) {
        this.profileCluster = Objects.requireNonNull(profileCluster, "profileCluster");
    }

    @Override
    public void start() {
        switch (this.workerState.getCurrentState()) {
            case NEW:
                if (this.workerState.changeStateInitializing()) {
                    logger.info("Starting profiler cluster manager");
                    workerState.changeStateStarted();
                    logger.info("Started profiler cluster manager");
                    break;
                }
            case INITIALIZING:
                logger.info("Failed to start: already initializing");
                break;
            case STARTED:
                logger.info("Failed to start: already started");
                break;
            case DESTROYING:
                throw new IllegalStateException("Failed to start: already destroying");
            case STOPPED:
                throw new IllegalStateException("Failed to start: already stopped");
            case ILLEGAL_STATE:
                throw new IllegalStateException("Failed to start: invalid state");
        }
    }

    @Override
    public void stop() {
        if (!this.workerState.changeStateDestroying()) {
            logger.info("Failed to stop profiler cluster manager: unexpected state");
            return;
        }

        logger.info("Stopping profiler cluster manager");
        this.workerState.changeStateStopped();
        logger.info("Stopped profiler cluster manager");
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
                    logger.info("Registered {}", targetClusterPoint);
                } else {
                    logger.warn("Failed to register {}: already exists", targetClusterPoint);
                }
            }
        } else {
            logger.warn("Failed to register {}: unexpected state", targetClusterPoint);
        }
    }

    @Override
    public void unregister(ClusterPoint<?> targetClusterPoint) {
        if (workerState.isStarted()) {
            synchronized (lock) {
                ClusterKey key = targetClusterPoint.getDestClusterKey();

                boolean removed = profileCluster.removeAndGetIsKeyRemoved(targetClusterPoint);
                if (key != null && removed) {
                    logger.info("Unregistered {}", targetClusterPoint);
                } else {
                    logger.warn("Failed to unregister {}: element not found", targetClusterPoint);
                }
            }
        } else {
            logger.warn("Failed to unregister {}: unexpected state", targetClusterPoint);
        }
    }

    @Override
    public List<String> getClusterData() {
        return profileCluster.getClusterPointList().stream()
                .map(el -> el.getDestClusterKey().toString())
                .collect(Collectors.toList());
    }

}
