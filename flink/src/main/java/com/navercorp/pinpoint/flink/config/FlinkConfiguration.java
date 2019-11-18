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

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Configuration
public class FlinkConfiguration {
    private final Logger logger = LoggerFactory.getLogger(FlinkConfiguration.class);

    @Value("${flink.cluster.enable:false}")
    private boolean flinkClusterEnable;

    @Value("${flink.cluster.zookeeper.address:}")
    private String flinkClusterZookeeperAddress;

    @Value("${flink.cluster.zookeeper.sessiontimeout:-1}")
    private int flinkClusterSessionTimeout;

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
        return flinkClusterEnable;
    }

    public String getFlinkClusterZookeeperAddress() {
        return flinkClusterZookeeperAddress;
    }

    public int getFlinkClusterTcpPort() {
        return flinkClusterTcpPort;
    }

    public int getFlinkClusterSessionTimeout() {
        return flinkClusterSessionTimeout;
    }

    public int getFlinkRetryInterval() {
        return flinkRetryInterval;
    }

    public boolean isLocalforFlinkStreamExecutionEnvironment() {
        return "local".equals(flinkStreamExecutionEnvironment) ? true : false;
    }

    public List<String> getL4IpList() {
        return Arrays.asList(l4IpList);
    }

    @PostConstruct
    public void log() {
        this.logger.info("{}", logger);
        AnnotationVisitor annotationVisitor = new AnnotationVisitor(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlinkConfiguration{");
        sb.append("flinkClusterEnable=").append(flinkClusterEnable);
        sb.append(", flinkClusterZookeeperAddress='").append(flinkClusterZookeeperAddress).append('\'');
        sb.append(", flinkClusterSessionTimeout=").append(flinkClusterSessionTimeout);
        sb.append(", flinkRetryInterval=").append(flinkRetryInterval);
        sb.append(", flinkClusterTcpPort=").append(flinkClusterTcpPort);
        sb.append(", flinkStreamExecutionEnvironment='").append(flinkStreamExecutionEnvironment).append('\'');
        sb.append(", l4IpList=").append(Arrays.toString(l4IpList));
        sb.append('}');
        return sb.toString();
    }
}
