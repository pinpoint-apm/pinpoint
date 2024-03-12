package com.navercorp.pinpoint.common.hbase.util;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class DefaultScanMetricReporter implements ScanMetricReporter {

    public DefaultScanMetricReporter() {
    }

    @Override
    public Reporter newReporter(TableName tableName, String comment, List<Scan> scans) {
        for (Scan scan : scans) {
            scan.setScanMetricsEnabled(true);
        }
        return new MetricReporter(tableName, comment);
    }

    @Override
    public Reporter newReporter(TableName tableName, String comment, Scan[] scans) {
        for (Scan scan : scans) {
            scan.setScanMetricsEnabled(true);
        }
        return new MetricReporter(tableName, comment);
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
            // simple metric
            ScanMetrics summary = new ScanMetrics();
            int actual = 0;
            for (ResultScanner scanner : scanners) {
                if (scanner == null) {
                    continue;
                }
                ScanMetrics scanMetrics = scanner.getScanMetrics();
                if (scanMetrics != null) {
                    actual++;
                    sum(summary, scanMetrics);
                }
            }
            if (actual != 0) {
                logger.info("ScanMetric {}/{} {} {}:{}", actual, scanners.length, name, comment, summary.getMetricsMap());
            }
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

}
