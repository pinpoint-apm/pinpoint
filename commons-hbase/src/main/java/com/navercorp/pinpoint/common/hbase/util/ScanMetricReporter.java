package com.navercorp.pinpoint.common.hbase.util;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface ScanMetricReporter {

    Reporter newReporter(TableName tableName, String comment, List<Scan> scans);

    Reporter newReporter(TableName tableName, String comment, Scan[] scans);


    interface Reporter {
        void report(ResultScanner[] scanners);

        void report(Supplier<List<ScanMetrics>> scanners);

        void report(ResultScanner scanner);

        void report(Collection<ScanMetrics> scanMetricsList);
    }
}
