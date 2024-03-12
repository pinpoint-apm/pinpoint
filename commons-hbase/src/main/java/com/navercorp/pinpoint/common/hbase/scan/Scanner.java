package com.navercorp.pinpoint.common.hbase.scan;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.util.List;

public interface Scanner<T> {
    List<T> extractData(ResultsExtractor<T> action);

    List<ScanMetrics> getScanMetrics();
}
