/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.util.Objects;

public class CollectorClusterProperties {
    private final Logger logger = LogManager.getLogger(getClass());

    private final ZookeeperClusterProperties clusterProperties;

    private final String clusterListenIp;

    private final int clusterListenPort;


    public CollectorClusterProperties(ZookeeperClusterProperties clusterProperties,
                                      String clusterListenIp,
                                      int clusterListenPort) {
        this.clusterProperties = Objects.requireNonNull(clusterProperties, "clusterProperties");
        this.clusterListenIp = Objects.requireNonNull(clusterListenIp, "clusterListenIp");
        this.clusterListenPort = clusterListenPort;
    }

    public boolean isClusterEnable() {
        return clusterProperties.isEnable();
    }

    public String getClusterAddress() {
        return clusterProperties.getAddress();
    }

    public String getWebZNodePath() {
        return clusterProperties.getWebZNodePath();
    }

    public String getCollectorZNodePath() {
        return clusterProperties.getCollectorZNodePath();
    }

    public int getClusterSessionTimeout() {
        return clusterProperties.getSessionTimeout();
    }

    public String getClusterListenIp() {
        return clusterListenIp;
    }


    public int getClusterListenPort() {
        return clusterListenPort;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CollectorClusterConfig{");
        sb.append("clusterEnable=").append(isClusterEnable());
        sb.append(", clusterAddress='").append(getClusterAddress()).append('\'');
        sb.append(", webZNodePath='").append(getCollectorZNodePath()).append('\'');
        sb.append(", collectorZNodePath='").append(getWebZNodePath()).append('\'');
        sb.append(", clusterSessionTimeout=").append(getClusterSessionTimeout());
        sb.append(", clusterListenPort=").append(clusterListenPort);
        sb.append('}');
        return sb.toString();
    }

}
