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

import org.apache.hadoop.hbase.client.AsyncConnectionImpl;
import org.apache.hadoop.hbase.client.ClusterConnection;
import org.apache.hadoop.hbase.client.MetricsConnection;
import org.apache.hadoop.hbase.shaded.com.codahale.metrics.MetricRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * @author intr3p1d
 */
public class HbaseConnectionReflects {

    private static final Logger logger = LogManager.getLogger(HbaseConnectionReflects.class);


    static List<MetricRegistry> getRegistriesFromConnections(ClusterConnection conn, AsyncConnectionImpl asyncConn) {
        List<MetricRegistry> registries = new ArrayList<>();

        addMetricRegistryFromConnection(registries, conn);
        addMetricRegistryFromAsyncConnection(registries, asyncConn);
        return registries;
    }

    static void addMetricRegistryFromConnection(List<MetricRegistry> registries, ClusterConnection conn) {
        MetricRegistry metricRegistry = getMetricRegistry(getMetricsConnection(conn));
        if (metricRegistry != null) {
            registries.add(metricRegistry);
        }
    }

    static void addMetricRegistryFromAsyncConnection(List<MetricRegistry> registries, AsyncConnectionImpl asyncConn) {
        MetricsConnection metricsConnection = getMetricsConnection(asyncConn)
                .orElseThrow(() -> new NoSuchElementException("MetricsConnection not present"));
        MetricRegistry metricRegistry = getMetricRegistry(metricsConnection);
        if (metricRegistry != null) {
            registries.add(metricRegistry);
        }
    }

    @SuppressWarnings("unchecked")
    static Optional<MetricsConnection> getMetricsConnection(AsyncConnectionImpl asyncConnection) {
        try {
            Method method = asyncConnection.getClass().getDeclaredMethod("getConnectionMetrics");
            method.setAccessible(true);
            return (Optional<MetricsConnection>) method.invoke(asyncConnection);
        } catch (Exception e) {
            logger.warn(e);
            return Optional.empty();
        }
    }

    static MetricsConnection getMetricsConnection(ClusterConnection connectionImplementation) {
        try {
            Method method = connectionImplementation.getClass().getDeclaredMethod("getConnectionMetrics");
            method.setAccessible(true);
            return (MetricsConnection) method.invoke(connectionImplementation);
        } catch (Exception e) {
            logger.warn(e);
            return null;
        }
    }

    static MetricRegistry getMetricRegistry(MetricsConnection metricsConnection) {
        try {
            Method method = metricsConnection.getClass().getDeclaredMethod("getMetricRegistry");
            method.setAccessible(true);
            return (MetricRegistry) method.invoke(metricsConnection);
        } catch (Exception e) {
            logger.warn(e);
            return null;
        }
    }
}
