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
import com.navercorp.pinpoint.collector.util.LoggerUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
@Component
public class CollectorMetric {

    public static final String REPORTER_LOGGER_NAME = "com.navercorp.pinpoint.collector.StateReport";

    private final Logger reporterLogger = LoggerFactory.getLogger(REPORTER_LOGGER_NAME);

    private final MetricRegistry metricRegistry;

    private final HBaseAsyncOperationMetrics hBaseAsyncOperationMetrics;
    private final BulkOperationMetrics bulkOperationMetrics;

    private List<Reporter> reporterList = new ArrayList<Reporter>(2);

    private final boolean isEnable = isEnable0(REPORTER_LOGGER_NAME);

    @Autowired
    private CollectorConfiguration collectorConfiguration;

    public CollectorMetric(MetricRegistry metricRegistry, Optional<HBaseAsyncOperationMetrics> hBaseAsyncOperationMetrics, Optional<BulkOperationMetrics> cachedStatisticsDaoMetrics) {
        this.metricRegistry = metricRegistry;
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
        final Logger logger = LoggerFactory.getLogger(loggerName);
        final int loggerLevel = LoggerUtils.getLoggerLevel(logger);
        if (loggerLevel >= LoggerUtils.WARN_LEVEL) {
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
            if (StringUtils.isEmpty(metricJmxDomainName)) {
                throw new IllegalArgumentException("metricJmxDomainName may not be empty");
            }

            final JmxReporter jmxReporter = createJmxReporter(metricJmxDomainName);
            jmxReporter.start();
            reporterList.add(jmxReporter);
        }
    }

    private Slf4jReporter createSlf4jReporter() {
        Slf4jReporter.Builder builder = Slf4jReporter.forRegistry(metricRegistry);
        builder.convertRatesTo(TimeUnit.SECONDS);
        builder.convertDurationsTo(TimeUnit.MILLISECONDS);

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
