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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class FlinkConfiguration implements BeanNameAware {

    private final Logger logger = LogManager.getLogger(getClass());

    private String name;
    private final ZookeeperClusterConfiguration clusterConfiguration;

    public FlinkConfiguration(ZookeeperClusterConfiguration clusterConfiguration) {
        this.clusterConfiguration = Objects.requireNonNull(clusterConfiguration, "clusterConfiguration");
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

    public int getFlinkClusterSessionTimeout() {
        return clusterConfiguration.getSessionTimeout();
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
    }

    @Override
    public String toString() {
        return "FlinkConfiguration{" +
                "name=" + name +
                ", flinkClusterEnable=" + isFlinkClusterEnable() +
                ", flinkClusterZookeeperAddress='" + getFlinkClusterZookeeperAddress() + '\'' +
                ", flinkZNodePath='" + getFlinkZNodePath() + '\'' +
                ", flinkClusterSessionTimeout=" + getFlinkClusterSessionTimeout() +
                '}';
    }

}
