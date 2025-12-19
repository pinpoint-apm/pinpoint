/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase.util;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class DefaultScanMetricReporter implements ScanMetricReporter {

    public DefaultScanMetricReporter() {
    }

    @Override
    public Reporter newReporter(TableName tableName, String comment, Scan[] scans) {
        applyMetricsEnabled(scans);
        return new MetricReporter(tableName, comment);
    }

    private void applyMetricsEnabled(Scan[] scans) {
        for (Scan scan : scans) {
            scan.setScanMetricsEnabled(true);
        }
    }

    @Override
    public ReportCollector collect(TableName tableName, String comment, Scan[] scans) {
        applyMetricsEnabled(scans);
        MetricReporter reporter = new MetricReporter(tableName, comment);
        return new DefaultReportCollector(reporter, scans.length);
    }

    public static class MetricReporter implements Reporter {
        private final Logger logger = LogManager.getLogger(this.getClass());

        private final TableName name;
        private final String comment;

        public MetricReporter(TableName name, String comment) {
            this.name = Objects.requireNonNull(name, "name");
            this.comment = Objects.requireNonNull(comment, "comment");
        }

        @Override
        public void report(ResultScanner[] scanners) {
            if (!logger.isInfoEnabled()) {
                return;
            }
            List<ScanMetrics> scanMetrics = scanMetric(scanners);

            report(scanMetrics);
        }

        private List<ScanMetrics> scanMetric(ResultScanner[] scanners) {
            List<ScanMetrics> scanMetricsList = new ArrayList<>(scanners.length);
            for (ResultScanner scanner : scanners) {
                ScanMetrics scanMetrics = scanner.getScanMetrics();
                if (scanMetrics != null) {
                    scanMetricsList.add(scanMetrics);
                }
            }
            return scanMetricsList;
        }

        @Override
        public void report(Supplier<List<ScanMetrics>> scanners) {
            List<ScanMetrics> scanMetrics = scanners.get();
            this.report(scanMetrics);
        }

        @Override
        public void report(Collection<ScanMetrics> scanMetricsList) {
            if (!logger.isInfoEnabled()) {
                return;
            }
            // simple metric
            ScanMetrics summary = new ScanMetrics();
            int actual = 0;
            for (ScanMetrics scanMetrics : scanMetricsList) {
                if (scanMetrics == null) {
                    continue;
                }
                actual++;
                sum(summary, scanMetrics);
            }
            if (actual != 0) {
                logger.info("ScanMetric {}/{} {} {}:{}", actual, scanMetricsList.size(), name, comment, summary.getMetricsMap());
            }
        }

        public void sum(ScanMetrics source, ScanMetrics target) {
            add(source.countOfRPCcalls, target.countOfRPCcalls);
            add(source.countOfRemoteRPCcalls, target.countOfRemoteRPCcalls);
            add(source.sumOfMillisSecBetweenNexts, target.sumOfMillisSecBetweenNexts);
            add(source.countOfNSRE, target.countOfNSRE);
            add(source.countOfBytesInResults, target.countOfBytesInResults);
            add(source.countOfBytesInRemoteResults, target.countOfBytesInRemoteResults);
            add(source.countOfRegions, target.countOfRegions);
            add(source.countOfRPCRetries, target.countOfRPCRetries);
            add(source.countOfRemoteRPCRetries, target.countOfRemoteRPCRetries);
        }

        private void add(AtomicLong source, AtomicLong target) {
            source.addAndGet(target.get());
        }

        @Override
        public void report(ResultScanner scanner) {
            if (!logger.isInfoEnabled()) {
                return;
            }
            if (scanner == null) {
                return;
            }
            ScanMetrics scanMetrics = scanner.getScanMetrics();
            if (scanMetrics != null) {
                logger.info("ScanMetric {} {}:{}", name, comment, scanMetrics.getMetricsMap());
            } else {
                logger.info("ScanMetric is null {} {}", name, comment);
            }
        }
    }

    static class DefaultReportCollector implements ReportCollector {

        private final MetricReporter reporter;
        private final List<ScanMetrics> scanMetricsList;

        public DefaultReportCollector(MetricReporter reporter, int capacity) {
            this.reporter = Objects.requireNonNull(reporter, "reporter");
            this.scanMetricsList = new ArrayList<>(capacity);
        }

        @Override
        public void collect(ScanMetrics scanMetrics) {
            if (scanMetrics == null) {
                return;
            }
            synchronized (scanMetricsList) {
                scanMetricsList.add(scanMetrics);
            }
        }

        @Override
        public void report() {
            List<ScanMetrics> copy;
            synchronized (scanMetricsList) {
                copy = new ArrayList<>(scanMetricsList);
            }
            this.reporter.report(copy);
        }
    }


}
