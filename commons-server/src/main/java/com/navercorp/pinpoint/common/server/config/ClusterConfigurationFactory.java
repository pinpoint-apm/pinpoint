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

package com.navercorp.pinpoint.common.server.config;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterConfiguration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Taejin Koo
 */
@Configuration
public class ClusterConfigurationFactory {

    public static final String DEFAULT_CLUSTER = "cluster.zookeeper";

    public static final String FLINK_CLUSTER = "flink.cluster.zookeeper";

    public static final String FLINK_SPAN_STAT_CLUSTER = "span.stat.flink.cluster.zookeeper";

    @Bean(DEFAULT_CLUSTER)
    @ConfigurationProperties(DEFAULT_CLUSTER)
    public ZookeeperClusterConfiguration.Builder newDefaultConfigurationBuilder() {
        return ZookeeperClusterConfiguration.newBuilder();
    }

    @Bean(FLINK_CLUSTER)
    @ConfigurationProperties(FLINK_CLUSTER)
    public ZookeeperClusterConfiguration.Builder newFlinkConfigurationBuilder() {
        return ZookeeperClusterConfiguration.newBuilder();
    }

    @Bean(FLINK_SPAN_STAT_CLUSTER)
    @ConfigurationProperties(FLINK_SPAN_STAT_CLUSTER)
    public ZookeeperClusterConfiguration.Builder newFlinkSpanStatConfigurationBuilder() {
        return ZookeeperClusterConfiguration.newBuilder();
    }

    @Bean("clusterConfiguration")
    public ZookeeperClusterConfiguration newDefault(Environment environment) {
        boolean enable = environment.getProperty("cluster.enable", boolean.class, false);

        ZookeeperClusterConfiguration.Builder builder = newDefaultConfigurationBuilder();
        builder.setEnable(enable);

        ZookeeperClusterConfiguration configuration = builder.build();
        return configuration;
    }

    @Bean("flinkClusterConfiguration")
    public ZookeeperClusterConfiguration newFlink(Environment environment) {
        boolean enable = environment.getProperty("flink.cluster.enable", boolean.class, false);

        ZookeeperClusterConfiguration.Builder builder = newFlinkConfigurationBuilder();
        builder.setEnable(enable);

        ZookeeperClusterConfiguration configuration = builder.build();
        return configuration;
    }

    @Bean("flinkSpanStatClusterConfiguration")
    public ZookeeperClusterConfiguration newFlinkSpanStat(Environment environment) {
        boolean enable = environment.getProperty("span.stat.flink.cluster.enable", boolean.class, false);

        ZookeeperClusterConfiguration.Builder builder = newFlinkSpanStatConfigurationBuilder();
        builder.setEnable(enable);

        ZookeeperClusterConfiguration configuration = builder.build();
        return configuration;
    }

}
