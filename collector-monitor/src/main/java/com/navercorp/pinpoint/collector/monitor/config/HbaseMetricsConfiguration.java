/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.monitor.config;

import com.navercorp.pinpoint.collector.monitor.dao.hbase.HBaseMetricsAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.AsyncConnectionImpl;
import org.apache.hadoop.hbase.client.ClusterConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.shaded.com.codahale.metrics.MetricRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.navercorp.pinpoint.collector.monitor.config.HbaseConnectionReflects.getRegistriesFromConnections;

/**
 * @author intr3p1d
 */
@Configuration
@ConditionalOnProperty(value = "pinpoint.modules.collector.hbase-client-metric.enabled", havingValue = "true")
public class HbaseMetricsConfiguration {

    private final Logger logger = LogManager.getLogger(HbaseMetricsConfiguration.class);

    public HbaseMetricsConfiguration() {
        logger.info("Install {}", HbaseMetricsConfiguration.class.getSimpleName());
    }

    @Bean
    public HBaseMetricsAdapter collectHBaseMetrics(
            MeterRegistry meterRegistry,
            @Qualifier("hbaseConnection")
            FactoryBean<Connection> connectionFactoryBean,
            @Qualifier("hbaseAsyncConnection")
            FactoryBean<AsyncConnection> asyncConnectionFactoryBean
    ) {
        try {
            ClusterConnection conn = (ClusterConnection) connectionFactoryBean.getObject();
            AsyncConnectionImpl asyncConn = (AsyncConnectionImpl) asyncConnectionFactoryBean.getObject();
            List<MetricRegistry> registries = getRegistriesFromConnections(conn, asyncConn);

            return new HBaseMetricsAdapter(
                    meterRegistry, registries
            );
        } catch (Exception e) {
            logger.error("HbaseMetricsConfiguration Error: ", e);
        }
        return null;
    }

}
