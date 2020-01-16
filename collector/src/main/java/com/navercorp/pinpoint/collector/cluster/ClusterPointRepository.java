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
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.PinpointServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterPointRepository<T extends ClusterPoint> implements ClusterPointLocator<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Set<T>> clusterPointRepository = new HashMap<>();

    public boolean addAndIsKeyCreated(T clusterPoint) {
        AgentInfo destAgentInfo = clusterPoint.getDestAgentInfo();
        String key = destAgentInfo.getAgentKey();
        synchronized (this) {
            final Set<T> clusterPointSet = clusterPointRepository.get(key);
            if (clusterPointSet != null) {
                clusterPointSet.add(clusterPoint);

                return false;
            } else {
                Set<T> newSet = new HashSet<>();
                newSet.add(clusterPoint);

                clusterPointRepository.put(key, newSet);
                return true;
            }
        }
    }

    public boolean removeAndGetIsKeyRemoved(T clusterPoint) {
        AgentInfo destAgentInfo = clusterPoint.getDestAgentInfo();
        String key = destAgentInfo.getAgentKey();
        synchronized (this) {
            final Set<T> clusterPointSet = clusterPointRepository.get(key);
            if (clusterPointSet != null) {
                clusterPointSet.remove(clusterPoint);

                if (clusterPointSet.isEmpty()) {
                    clusterPointRepository.remove(key);
                    return true;
                }
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

    public Set<String> getAvailableAgentKeyList() {
        synchronized (this) {
            Set<String> availableAgentKeySet = new HashSet<>(clusterPointRepository.size());

            for (Map.Entry<String, Set<T>> entry : clusterPointRepository.entrySet()) {
                final String key = entry.getKey();
                final Set<T> clusterPointSet = entry.getValue();
                for (T clusterPoint : clusterPointSet) {
                    if (clusterPoint instanceof ThriftAgentConnection) {
                        PinpointServer pinpointServer = ((ThriftAgentConnection) clusterPoint).getPinpointServer();
                        if (SocketStateCode.isRunDuplex(pinpointServer.getCurrentStateCode())) {
                            availableAgentKeySet.add(key);
                        }
                    } else if (clusterPoint instanceof GrpcAgentConnection) {
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
