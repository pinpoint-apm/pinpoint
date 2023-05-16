/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.cluster.zookeeper.config;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Taejin Koo
 */
@Configuration
@EnableConfigurationProperties
public class ClusterConfigurationFactory {

    private static final String DEFAULT_CLUSTER = "cluster";
    private static final String DEFAULT_CLUSTER_ZOOKEEPER = "cluster.zookeeper";

    private static final String FLINK_CLUSTER = "flink.cluster";
    private static final String FLINK_CLUSTER_ZOOKEEPER = "flink.cluster.zookeeper";

    private static final String FLINK_SPAN_STAT_CLUSTER = "span.stat.flink.cluster";
    private static final String SPAN_STAT_FLINK_CLUSTER_ZOOKEEPER = "span.stat.flink.cluster.zookeeper";

    @Bean(DEFAULT_CLUSTER)
    @ConfigurationProperties(prefix = DEFAULT_CLUSTER)
    public ClusterEnable newDefaultConfigurationEnable() {
        return new ClusterEnable();
    }

    @Bean(FLINK_CLUSTER)
    @ConfigurationProperties(prefix = FLINK_CLUSTER)
    public ClusterEnable newFlinkClusterEnable() {
        return new ClusterEnable();
    }

    @Bean(FLINK_SPAN_STAT_CLUSTER)
    @ConfigurationProperties(prefix = FLINK_SPAN_STAT_CLUSTER)
    public ClusterEnable newFlinkSpanStatClusterEnable() {
        return new ClusterEnable();
    }


    @Bean(DEFAULT_CLUSTER_ZOOKEEPER)
    @ConfigurationProperties(prefix = DEFAULT_CLUSTER_ZOOKEEPER)
    public ZookeeperClusterProperties.Builder newDefaultPropertiesBuilder() {
        return ZookeeperClusterProperties.newBuilder();
    }

    @Bean(FLINK_CLUSTER_ZOOKEEPER)
    @ConfigurationProperties(prefix = FLINK_CLUSTER_ZOOKEEPER)
    public ZookeeperClusterProperties.Builder newFlinkPropertiesBuilder() {
        return ZookeeperClusterProperties.newBuilder();
    }

    @Bean(SPAN_STAT_FLINK_CLUSTER_ZOOKEEPER)
    @ConfigurationProperties(prefix = SPAN_STAT_FLINK_CLUSTER_ZOOKEEPER)
    public ZookeeperClusterProperties.Builder newFlinkSpanStatPropertiesBuilder() {
        return ZookeeperClusterProperties.newBuilder();
    }

    @Bean
    public ZookeeperClusterProperties clusterProperties() {
        ClusterEnable clusterEnable = newDefaultConfigurationEnable();
        ZookeeperClusterProperties.Builder builder = newDefaultPropertiesBuilder();
        return createProperties(clusterEnable, builder);
    }

    @Bean
    public ZookeeperClusterProperties flinkClusterProperties() {
        ClusterEnable clusterEnable = newFlinkClusterEnable();
        ZookeeperClusterProperties.Builder builder = newFlinkPropertiesBuilder();
        return createProperties(clusterEnable, builder);
    }

    @Bean
    public ZookeeperClusterProperties flinkSpanStatClusterProperties() {
        ClusterEnable clusterEnable = newFlinkSpanStatClusterEnable();
        ZookeeperClusterProperties.Builder builder = newFlinkSpanStatPropertiesBuilder();
        return createProperties(clusterEnable, builder);
    }

    private ZookeeperClusterProperties createProperties(ClusterEnable clusterEnable, ZookeeperClusterProperties.Builder builder) {
        builder.setEnable(clusterEnable.isEnable());

        return builder.build();
    }

    private static class ClusterEnable {

        private boolean enable;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
    }

}
