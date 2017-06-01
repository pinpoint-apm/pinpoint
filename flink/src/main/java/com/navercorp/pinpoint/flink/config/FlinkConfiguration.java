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

import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author minwoo.jung
 */
public class FlinkConfiguration extends CollectorConfiguration {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean flinkClusterEnable;
    private String flinkClusterZookeeperAddress;
    private int flinkClusterSessionTimeout;
    private int flinkRetryInterval;
    private int flinkClusterTcpPort;
    private String flinkStreamExecutionEnvironment;

    private int flinkSourceFunctionParallel;

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

    public int getFlinkSourceFunctionParallel() {
        return flinkSourceFunctionParallel;
    }

    public boolean isLocalforFlinkStreamExecutionEnvironment() {
        return "local".equals(flinkStreamExecutionEnvironment) ? true : false;
    }


    @Override
    protected void readPropertyValues(Properties properties) {
        logger.info("pinpoint-flink.properties read.");
        super.readPropertyValues(properties);

        this.flinkClusterEnable = readBoolean(properties, "flink.cluster.enable");
        this.flinkClusterZookeeperAddress = readString(properties, "flink.cluster.zookeeper.address", "");
        this.flinkClusterSessionTimeout = readInt(properties, "flink.cluster.zookeeper.sessiontimeout", -1);
        this.flinkRetryInterval =  readInt(properties, "flink.cluster.zookeeper.retry.interval", 60000);
        this.flinkClusterTcpPort = readInt(properties,"flink.cluster.tcp.port", 19994);
        this.flinkStreamExecutionEnvironment = readString(properties, "flink.StreamExecutionEnvironment", "server");
        this.flinkSourceFunctionParallel = readInt(properties, "flink.sourceFunction.Parallel", 1);
    }
}
