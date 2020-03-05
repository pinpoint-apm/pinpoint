/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author minwoo.jung
 */
@Configuration
public class FlinkConfiguration {

    private final Logger logger = LoggerFactory.getLogger(FlinkConfiguration.class);

    @Value("${flink.cluster.enable:false}")
    protected boolean flinkClusterEnable;

    @Value("${flink.cluster.zookeeper.address:}")
    protected String flinkClusterZookeeperAddress;

    @Value("${flink.cluster.zookeeper.sessiontimeout:-1}")
    protected int flinkClusterSessionTimeout;

    public FlinkConfiguration() {
    }

    public FlinkConfiguration(boolean flinkClusterEnable, String flinkClusterZookeeperAddress, int flinkClusterSessionTimeout) {
        this.flinkClusterEnable = flinkClusterEnable;
        this.flinkClusterZookeeperAddress = flinkClusterZookeeperAddress;
        this.flinkClusterSessionTimeout = flinkClusterSessionTimeout;
    }

    public boolean isFlinkClusterEnable() {
        return flinkClusterEnable;
    }

    public String getFlinkClusterZookeeperAddress() {
        return flinkClusterZookeeperAddress;
    }

    public int getFlinkClusterSessionTimeout() {
        return flinkClusterSessionTimeout;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        if (logger.isDebugEnabled()) {
            AnnotationVisitor visitor = new AnnotationVisitor(Value.class);
            visitor.visit(this, new LoggingEvent(logger));
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlinkConfiguration{");
        sb.append("flinkClusterEnable=").append(flinkClusterEnable);
        sb.append(", flinkClusterZookeeperAddress='").append(flinkClusterZookeeperAddress).append('\'');
        sb.append(", flinkClusterSessionTimeout=").append(flinkClusterSessionTimeout);
        sb.append('}');
        return sb.toString();
    }
}
