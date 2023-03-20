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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterConfiguration;
import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CollectorClusterConfig {
    private final Logger logger = LogManager.getLogger(getClass());

    private final ZookeeperClusterConfiguration clusterConfiguration;

    private final String clusterListenIp;

    private final int clusterListenPort;


    public CollectorClusterConfig(
            @Qualifier("clusterConfiguration") ZookeeperClusterConfiguration clusterConfiguration,
            @Value("${cluster.listen.ip:}")    String clusterListenIp,
            @Value("${cluster.listen.port:-1}") int clusterListenPort) {
        this.clusterConfiguration = clusterConfiguration;
        this.clusterListenIp = clusterListenIp;
        this.clusterListenPort = clusterListenPort;
    }

    public boolean isClusterEnable() {
        return clusterConfiguration.isEnable();
    }

    public String getClusterAddress() {
        return clusterConfiguration.getAddress();
    }

    public String getWebZNodePath() {
        return clusterConfiguration.getWebZNodePath();
    }

    public String getCollectorZNodePath() {
        return clusterConfiguration.getCollectorZNodePath();
    }

    public int getClusterSessionTimeout() {
        return clusterConfiguration.getSessionTimeout();
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
        AnnotationVisitor<Value> visitor = new AnnotationVisitor<>(Value.class);
        visitor.visit(this, new LoggingEvent(logger));
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
