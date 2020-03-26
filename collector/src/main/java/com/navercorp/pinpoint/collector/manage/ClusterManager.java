/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.manage;

import com.navercorp.pinpoint.collector.cluster.AgentInfo;
import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ClusterManager extends AbstractCollectorManager implements ClusterManagerMBean {

    private final boolean enableCluster;
    private final ClusterPointLocator clusterPointLocator;

    public ClusterManager(CollectorConfiguration configuration, ClusterPointLocator clusterPointLocator) {
        Objects.requireNonNull(configuration, "configuration");
        this.enableCluster = configuration.isClusterEnable();
        this.clusterPointLocator = Objects.requireNonNull(clusterPointLocator, "clusterPointLocator");
    }

    @Override
    public boolean isEnable() {
        return enableCluster;
    }

    @Override
    public List<String> getConnectedAgentList() {
        List<String> result = new ArrayList<>();

        List<ClusterPoint> clusterPointList = clusterPointLocator.getClusterPointList();
        for (ClusterPoint clusterPoint : clusterPointList) {
            AgentInfo destAgentInfo = clusterPoint.getDestAgentInfo();
            result.add(destAgentInfo.getAgentKey());
        }

        return result;
    }

}
