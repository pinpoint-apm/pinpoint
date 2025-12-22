package com.navercorp.pinpoint.common.hbase.util;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

public class ScanMetricUtils {

    public static ScanMetrics merge(Collection<ScanMetrics> metricsList) {
        if (CollectionUtils.isEmpty(metricsList)) {
            return new ScanMetrics();
        }

        ScanMetrics sum = new ScanMetrics();
        for (ScanMetrics target : metricsList) {
            sum(sum, target);
        }

        return sum;
    }

    public static void sum(ScanMetrics source, ScanMetrics target) {
        if (target == null) {
            return;
        }
        add(source.countOfRPCcalls, target.countOfRPCcalls);
        add(source.countOfRemoteRPCcalls, target.countOfRemoteRPCcalls);
        add(source.sumOfMillisSecBetweenNexts, target.sumOfMillisSecBetweenNexts);
        add(source.countOfNSRE, target.countOfNSRE);
        add(source.countOfBytesInResults, target.countOfBytesInResults);
        add(source.countOfBytesInRemoteResults, target.countOfBytesInRemoteResults);
        add(source.countOfRegions, target.countOfRegions);
        add(source.countOfRPCRetries, target.countOfRPCRetries);
        add(source.countOfRemoteRPCRetries, target.countOfRemoteRPCRetries);

        add(source.countOfRowsFiltered, target.countOfRowsFiltered);
        add(source.countOfRowsScanned, target.countOfRowsScanned);
        add(source.bytesReadFromFs, target.bytesReadFromFs);
        add(source.bytesReadFromBlockCache, target.bytesReadFromBlockCache);
        add(source.bytesReadFromMemstore, target.bytesReadFromMemstore);
        add(source.blockReadOpsCount, target.blockReadOpsCount);
    }

    private static void add(AtomicLong source, AtomicLong target) {
        source.addAndGet(target.get());
    }
}
