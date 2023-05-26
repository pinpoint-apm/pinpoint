/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServer;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterPointRepository<T extends ClusterPoint<?>> implements ClusterPointLocator<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @GuardedBy("this")
    private final Map<ClusterKey, Set<T>> clusterPointRepository = new HashMap<>();

    public boolean addAndIsKeyCreated(T clusterPoint) {
        ClusterKey destClusterKey = clusterPoint.getDestClusterKey();
        synchronized (this) {
            final Set<T> clusterPointSet = clusterPointRepository.get(destClusterKey);
            if (clusterPointSet != null) {
                clusterPointSet.add(clusterPoint);

                return false;
            } else {
                Set<T> newSet = new HashSet<>();
                newSet.add(clusterPoint);

                clusterPointRepository.put(destClusterKey, newSet);
                return true;
            }
        }
    }

    public boolean removeAndGetIsKeyRemoved(T clusterPoint) {
        ClusterKey destClusterKey = clusterPoint.getDestClusterKey();
        synchronized (this) {
            final Set<T> clusterPointSet = clusterPointRepository.get(destClusterKey);
            if (clusterPointSet != null) {
                clusterPointSet.remove(clusterPoint);

                if (clusterPointSet.isEmpty()) {
                    clusterPointRepository.remove(destClusterKey);
                    return true;
                }
                logger.info("clusterPointSet was not empty: {}", clusterPoint);
            } else {
                logger.info("clusterPointSet not found: {}", clusterPoint);
            }
            return false;
        }
    }

    public List<T> getClusterPointList() {
        synchronized (this) {
            List<T> clusterPointList = new ArrayList<>(clusterPointRepository.size());

            for (Set<T> eachKeysValue : clusterPointRepository.values()) {
                clusterPointList.addAll(eachKeysValue);
            }

            return clusterPointList;
        }
    }

    public Set<ClusterKey> getAvailableAgentKeyList() {
        synchronized (this) {
            Set<ClusterKey> availableAgentKeySet = new HashSet<>(clusterPointRepository.size());

            for (Map.Entry<ClusterKey, Set<T>> entry : clusterPointRepository.entrySet()) {
                final ClusterKey key = entry.getKey();
                final Set<T> clusterPointSet = entry.getValue();
                for (T clusterPoint : clusterPointSet) {
                    if (clusterPoint instanceof GrpcAgentConnection) {
                        PinpointGrpcServer pinpointGrpcServer = ((GrpcAgentConnection) clusterPoint).getPinpointGrpcServer();
                        if (SocketStateCode.isRunDuplex(pinpointGrpcServer.getState())) {
                            availableAgentKeySet.add(key);
                        }
                    }
                }
            }
            return availableAgentKeySet;
        }
    }

    public void clear() {
        synchronized (this) {
            clusterPointRepository.clear();
        }
    }

}
