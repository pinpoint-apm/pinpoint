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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterConfiguration;
import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author minwoo.jung
 */
@Component
public class FlinkConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Qualifier("flinkClusterConfiguration")
    @Autowired
    private ZookeeperClusterConfiguration clusterConfiguration;

    public boolean isFlinkClusterEnable() {
        return clusterConfiguration.isEnable();
    }

    public String getFlinkClusterZookeeperAddress() {
        return clusterConfiguration.getAddress();
    }

    public String getFlinkZNodePath() {
        return clusterConfiguration.getFlinkZNodePath();
    }

    public int getFlinkClusterSessionTimeout() {
        return clusterConfiguration.getSessionTimeout();
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        if (logger.isDebugEnabled()) {
            AnnotationVisitor<Value> visitor = new AnnotationVisitor<>(Value.class);
            visitor.visit(this, new LoggingEvent(logger));
        }
    }


    @Override
    public String toString() {
        return "FlinkConfiguration{" +
                "flinkClusterEnable=" + isFlinkClusterEnable() +
                ", flinkClusterZookeeperAddress='" + getFlinkClusterZookeeperAddress() + '\'' +
                ", flinkZNodePath='" + getFlinkZNodePath() + '\'' +
                ", flinkClusterSessionTimeout=" + getFlinkClusterSessionTimeout() +
                '}';
    }
}
