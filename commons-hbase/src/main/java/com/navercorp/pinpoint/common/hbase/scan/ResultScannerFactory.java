package com.navercorp.pinpoint.common.hbase.scan;

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class ResultScannerFactory {
    private final int partitionSize;

    public ResultScannerFactory(int partitionSize) {
        this.partitionSize = partitionSize;
    }

    public <T> Scanner<T> newScanner(Table table, Scan[] scans) {
        return newScanner0(scan -> {
            try {
                return table.getScanner(scan);
            } catch (IOException e) {
                throw new HbaseSystemException(e);
            }
        }, scans);
    }

    public <T> Scanner<T> newScanner(AsyncTable<?> table, Scan[] scans) {
        return newScanner0(table::getScanner, scans);
    }

    private <T> Scanner<T> newScanner0(Function<Scan, ResultScanner> table, Scan[] scans) {
        final int scansLength = scans.length;
        ResultScannerSupplier[] scanners = new ResultScannerSupplier[scansLength];
        boolean success = false;
        try {
            for (int i = 0; i < scansLength; i++) {
                scanners[i] = new DefaultResultScannerSupplier(table, scans[i]);
            }
            success = true;
        } finally {
            if (!success) {
                closeScanner(scanners);
            }
        }
        return new DefaultScanner<>(scanners, partitionSize);
    }


    public static void closeScanner(ResultScannerSupplier[] scannerList) {
        for (ResultScannerSupplier scanner : scannerList) {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Throwable ignore) {
                    // ignore
                }
            }
        }
    }

    public static void closeScanner(List<ResultScannerSupplier> scannerList) {
        for (ResultScannerSupplier scanner : scannerList) {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Throwable ignore) {
                    // ignore
                }
            }
        }
    }
}
