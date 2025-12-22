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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Objects;

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
        return new DefaultReportCollector(reporter);
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
        public void report(ScanMetrics scanMetrics) {
            if (scanMetrics == null) {
                return;
            }
            if (logger.isInfoEnabled()) {
                logger.info("ScanMetric {} {}:{}", name, comment, scanMetrics);
            }
        }

        @Override
        public void report(ResultScanner[] scanners) {
            if (!logger.isInfoEnabled()) {
                return;
            }
            ScanMetrics scanMetrics = scanMetric(scanners);

            report(scanMetrics);
        }

        private ScanMetrics scanMetric(ResultScanner[] scanners) {
            ScanMetrics result = null;
            for (ResultScanner scanner : scanners) {
                ScanMetrics scanMetrics = scanner.getScanMetrics();
                if (scanMetrics == null) {
                    continue;
                }
                if (result == null) {
                    result = new ScanMetrics();
                }
                ScanMetricUtils.sum(result, scanMetrics);
            }
            return result;
        }

        @Override
        public void report(Collection<ScanMetrics> scanMetricsList) {
            if (!logger.isInfoEnabled()) {
                return;
            }
            // simple metric
            ScanMetrics summary = ScanMetricUtils.merge(scanMetricsList);
            if (CollectionUtils.hasLength(scanMetricsList)) {
                logger.info("ScanMetric {} {} {}:{}", name, scanMetricsList.size(), comment, summary);
            }
        }

    }

    static class DefaultReportCollector implements ReportCollector {

        private final MetricReporter reporter;
        private volatile ScanMetrics metrics;

        public DefaultReportCollector(MetricReporter reporter) {
            this.reporter = Objects.requireNonNull(reporter, "reporter");
        }

        @Override
        public void collect(ScanMetrics scanMetrics) {
            if (scanMetrics == null) {
                return;
            }
            ScanMetrics metrics = getScanMetric();
            ScanMetricUtils.sum(metrics, scanMetrics);
        }

        private ScanMetrics getScanMetric() {
            ScanMetrics copy = this.metrics;
            if (copy != null) {
                return copy;
            }
            synchronized (this) {
                ScanMetrics sc = this.metrics;
                if (sc != null) {
                    return sc;
                }
                sc = new ScanMetrics();
                this.metrics = sc;
                return sc;
            }
        }

        @Override
        public void report() {
            ScanMetrics metrics = this.metrics;
            if (metrics != null) {
                this.reporter.report(metrics);
            }

        }
    }


}
