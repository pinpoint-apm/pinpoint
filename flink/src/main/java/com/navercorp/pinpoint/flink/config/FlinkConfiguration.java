/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.flink.config;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterConfiguration;
import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class FlinkConfiguration {
    private final Logger logger = LogManager.getLogger(FlinkConfiguration.class);

    @Qualifier("flinkClusterConfiguration")
    @Autowired
    private ZookeeperClusterConfiguration clusterConfiguration;

    @Value("${flink.cluster.zookeeper.retry.interval:60000}")
    private int flinkRetryInterval;

    @Value("${flink.cluster.tcp.port:19994}")
    private int flinkClusterTcpPort;

    @Value("${flink.StreamExecutionEnvironment:server}")
    private String flinkStreamExecutionEnvironment;

    @Value("${collector.l4.ip:}")
    private String[] l4IpList = new String[0];

    public FlinkConfiguration() {
    }

    public boolean isFlinkClusterEnable() {
        return clusterConfiguration.isEnable();
    }

    public String getFlinkClusterZookeeperAddress() {
        return clusterConfiguration.getAddress();
    }

    public String getFlinkZNodePath() {
        return clusterConfiguration.getFlinkZNodePath();
    }

    public int getFlinkClusterTcpPort() {
        return flinkClusterTcpPort;
    }

    public int getFlinkClusterSessionTimeout() {
        return clusterConfiguration.getSessionTimeout();
    }

    public int getFlinkRetryInterval() {
        return flinkRetryInterval;
    }

    public boolean isLocalforFlinkStreamExecutionEnvironment() {
        return "local".equals(flinkStreamExecutionEnvironment);
    }

    public List<String> getL4IpList() {
        return Arrays.asList(l4IpList);
    }

    @PostConstruct
    public void log() {
        this.logger.info("{}", logger);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }


    @Override
    public String toString() {
        return "FlinkConfiguration{" +
                "flinkClusterEnable=" + isFlinkClusterEnable() +
                ", flinkClusterZookeeperAddress='" + getFlinkClusterZookeeperAddress() + '\'' +
                ", flinkZNodePath='" + getFlinkZNodePath() + '\'' +
                ", flinkClusterSessionTimeout=" + getFlinkClusterSessionTimeout() +
                ", flinkRetryInterval=" + flinkRetryInterval +
                ", flinkClusterTcpPort=" + flinkClusterTcpPort +
                ", flinkStreamExecutionEnvironment='" + flinkStreamExecutionEnvironment + '\'' +
                ", l4IpList=" + Arrays.toString(l4IpList) +
                '}';
    }
}
