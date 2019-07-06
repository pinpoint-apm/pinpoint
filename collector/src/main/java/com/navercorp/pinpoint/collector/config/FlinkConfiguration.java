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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;
import java.util.Properties;

/**
 * @author minwoo.jung
 */
public class FlinkConfiguration implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkConfiguration.class);
    private Properties properties;

    protected boolean flinkClusterEnable;
    protected String flinkClusterZookeeperAddress;
    protected int flinkClusterSessionTimeout;

    public FlinkConfiguration() {
    }

    public FlinkConfiguration(boolean flinkClusterEnable, String flinkClusterZookeeperAddress, int flinkClusterSessionTimeout) {
        this.flinkClusterEnable = flinkClusterEnable;
        this.flinkClusterZookeeperAddress = flinkClusterZookeeperAddress;
        this.flinkClusterSessionTimeout = flinkClusterSessionTimeout;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        final Properties properties = Objects.requireNonNull(this.properties, "properties must not be null");
        readPropertyValues(properties);
    }

    protected void readPropertyValues(Properties properties) {
        LOGGER.info("pinpoint-collector.properties read.");

        this.flinkClusterEnable = CollectorConfiguration.readBoolean(properties, "flink.cluster.enable");
        this.flinkClusterZookeeperAddress = CollectorConfiguration.readString(properties, "flink.cluster.zookeeper.address", "");
        this.flinkClusterSessionTimeout = CollectorConfiguration.readInt(properties, "flink.cluster.zookeeper.sessiontimeout", -1);
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
