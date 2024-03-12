package com.navercorp.pinpoint.common.hbase.util;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class EmptyScanMetricReporter implements ScanMetricReporter {

    private static final Reporter REPORTER = new EmptyReporter();

    public EmptyScanMetricReporter() {
    }

    @Override
    public Reporter newReporter(TableName tableName, String comment, List<Scan> scans) {
        return REPORTER;
    }

    @Override
    public Reporter newReporter(TableName tableName, String comment, Scan[] scans) {
        return REPORTER;
    }

    public static class EmptyReporter implements Reporter {
        @Override
        public void report(ResultScanner[] scanners) {
        }

        @Override
        public void report(Supplier<List<ScanMetrics>> scanners) {

        }

        @Override
        public void report(ResultScanner scanner) {
        }

        @Override
        public void report(Collection<ScanMetrics> scanMetricsList) {

        }
    }
}
