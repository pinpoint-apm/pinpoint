/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor;

import com.navercorp.pinpoint.collector.config.CollectorConfiguration;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
@Component
public class CollectorMetric {

    public static final String REPORTER_LOGGER_NAME = "com.navercorp.pinpoint.collector.StateReport";

    private final CollectorConfiguration collectorConfiguration;

    private final MetricRegistry metricRegistry;

    private final HBaseAsyncOperationMetrics hBaseAsyncOperationMetrics;
    private final BulkOperationMetrics bulkOperationMetrics;

    private List<Reporter> reporterList = new ArrayList<>(2);

    private final boolean isEnable = isEnable0(REPORTER_LOGGER_NAME);

    public CollectorMetric(CollectorConfiguration collectorConfiguration,
                           MetricRegistry metricRegistry,
                           Optional<HBaseAsyncOperationMetrics> hBaseAsyncOperationMetrics,
                           Optional<BulkOperationMetrics> cachedStatisticsDaoMetrics) {
        this.collectorConfiguration = Objects.requireNonNull(collectorConfiguration, "collectorConfiguration");
        this.metricRegistry = Objects.requireNonNull(metricRegistry, "metricRegistry");
        this.hBaseAsyncOperationMetrics = hBaseAsyncOperationMetrics.orElse(null);
        this.bulkOperationMetrics = cachedStatisticsDaoMetrics.orElse(null);
    }

    @PostConstruct
    public void start() {
        initRegistry();
        initReporters();
    }

    public boolean isEnable() {
        return isEnable;
    }

    private boolean isEnable0(String loggerName) {
        final Logger logger = LogManager.getLogger(loggerName);
        final Level level = logger.getLevel();
        if (level.isLessSpecificThan(Level.WARN)) {
            return false;
        }
        return true;
    }

    private void initRegistry() {
        // add JVM statistics
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.vm", new JvmAttributeGaugeSet());
        metricRegistry.register("jvm.garbage-collectors", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet());

        if (hBaseAsyncOperationMetrics != null) {
            Map<String, Metric> metrics = hBaseAsyncOperationMetrics.getMetrics();
            for (Map.Entry<String, Metric> metric : metrics.entrySet()) {
                metricRegistry.register(metric.getKey(), metric.getValue());
            }
        }

        if (bulkOperationMetrics != null) {
            Map<String, Metric> metrics = bulkOperationMetrics.getMetrics();
            for (Map.Entry<String, Metric> metric : metrics.entrySet()) {
                metricRegistry.register(metric.getKey(), metric.getValue());
            }
        }
    }

    private void initReporters() {
        Slf4jReporter slf4jReporter = createSlf4jReporter();
        slf4jReporter.start(60, TimeUnit.SECONDS); // print every 1 min.

        reporterList.add(slf4jReporter);

        if (collectorConfiguration.isMetricJmxEnable()) {

            final String metricJmxDomainName = collectorConfiguration.getMetricJmxDomainName();
            Assert.hasLength(metricJmxDomainName, "metricJmxDomainName must not be empty");

            final JmxReporter jmxReporter = createJmxReporter(metricJmxDomainName);
            jmxReporter.start();
            reporterList.add(jmxReporter);
        }
    }

    private Slf4jReporter createSlf4jReporter() {
        Slf4jReporter.Builder builder = Slf4jReporter.forRegistry(metricRegistry);
        builder.convertRatesTo(TimeUnit.SECONDS);
        builder.convertDurationsTo(TimeUnit.MILLISECONDS);

        Log4jLoggerFactory log4jLoggerFactory = new Log4jLoggerFactory();

        final org.slf4j.Logger reporterLogger = log4jLoggerFactory.getLogger(REPORTER_LOGGER_NAME);
        builder.outputTo(reporterLogger);
        return builder.build();
    }

    private JmxReporter createJmxReporter(String metricJmxDomainName) {
        final JmxReporter.Builder builder = JmxReporter.forRegistry(metricRegistry);
        builder.convertRatesTo(TimeUnit.SECONDS);
        builder.convertDurationsTo(TimeUnit.MILLISECONDS);
        builder.inDomain(metricJmxDomainName);

        return builder.build();
    }

    @PreDestroy
    private void shutdown() {
        for (Reporter reporter : reporterList) {
            if (reporter instanceof Closeable) {
                try {
                    ((Closeable) reporter).close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        reporterList = null;
    }

}
