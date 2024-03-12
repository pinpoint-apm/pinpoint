package com.navercorp.pinpoint.common.hbase.scan;

import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

public interface ResultScannerSupplier extends AutoCloseable {

    ResultScanner getScanner();


    ScanMetrics getScanMetrics();
}
