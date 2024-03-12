package com.navercorp.pinpoint.common.hbase.scan;

import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.util.Objects;
import java.util.function.Function;

public class DefaultResultScannerSupplier implements ResultScannerSupplier {

    private final Function<Scan, ResultScanner> table;
    private final Scan scan;
    private boolean closed;

    private ResultScanner resultScanner;

    public DefaultResultScannerSupplier(Function<Scan, ResultScanner> table, Scan scan) {
        this.table = Objects.requireNonNull(table, "table");
        this.scan = Objects.requireNonNull(scan, "scan");
    }

    @Override
    public ResultScanner getScanner() {
        if (resultScanner != null) {
            return resultScanner;
        }
        resultScanner = table.apply(scan);
        return resultScanner;
    }


    @Override
    public ScanMetrics getScanMetrics() {
        if (resultScanner != null) {
            return resultScanner.getScanMetrics();
        }
        return null;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        this.closed = true;
        if (resultScanner != null) {
            resultScanner.close();
        }
    }
}
