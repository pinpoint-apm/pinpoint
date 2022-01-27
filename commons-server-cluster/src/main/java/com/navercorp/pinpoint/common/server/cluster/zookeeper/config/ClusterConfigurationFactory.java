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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

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
    public ClusterEnable newFlinkConfigurationEnable() {
        return new ClusterEnable();
    }

    @Bean(FLINK_SPAN_STAT_CLUSTER)
    @ConfigurationProperties(prefix = FLINK_SPAN_STAT_CLUSTER)
    public ClusterEnable newFlinkSpanStatConfigurationEnable() {
        return new ClusterEnable();
    }


    @Bean(DEFAULT_CLUSTER_ZOOKEEPER)
    @ConfigurationProperties(prefix = DEFAULT_CLUSTER_ZOOKEEPER)
    public ZookeeperClusterConfiguration.Builder newDefaultConfigurationBuilder() {
        return ZookeeperClusterConfiguration.newBuilder();
    }

    @Bean(FLINK_CLUSTER_ZOOKEEPER)
    @ConfigurationProperties(prefix = FLINK_CLUSTER_ZOOKEEPER)
    public ZookeeperClusterConfiguration.Builder newFlinkConfigurationBuilder() {
        return ZookeeperClusterConfiguration.newBuilder();
    }

    @Bean(SPAN_STAT_FLINK_CLUSTER_ZOOKEEPER)
    @ConfigurationProperties(prefix = SPAN_STAT_FLINK_CLUSTER_ZOOKEEPER)
    public ZookeeperClusterConfiguration.Builder newFlinkSpanStatConfigurationBuilder() {
        return ZookeeperClusterConfiguration.newBuilder();
    }

    @Bean("clusterConfiguration")
    public ZookeeperClusterConfiguration newDefault(Environment environment) {
        ClusterEnable clusterEnable = newDefaultConfigurationEnable();
        ZookeeperClusterConfiguration.Builder builder = newDefaultConfigurationBuilder();
        return createConfiguration(clusterEnable, builder);
    }

    @Bean("flinkClusterConfiguration")
    public ZookeeperClusterConfiguration newFlink(Environment environment) {
        ClusterEnable clusterEnable = newFlinkConfigurationEnable();
        ZookeeperClusterConfiguration.Builder builder = newFlinkConfigurationBuilder();
        return createConfiguration(clusterEnable, builder);
    }

    @Bean("flinkSpanStatClusterConfiguration")
    public ZookeeperClusterConfiguration newFlinkSpanStat(Environment environment) {
        ClusterEnable clusterEnable = newFlinkSpanStatConfigurationEnable();
        ZookeeperClusterConfiguration.Builder builder = newFlinkSpanStatConfigurationBuilder();
        return createConfiguration(clusterEnable, builder);
    }

    private ZookeeperClusterConfiguration createConfiguration(ClusterEnable clusterEnable, ZookeeperClusterConfiguration.Builder builder) {
        builder.setEnable(clusterEnable.isEnable());

        ZookeeperClusterConfiguration configuration = builder.build();
        return configuration;
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
