package com.navercorp.pinpoint.common.hbase.scan;

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import org.apache.commons.collections4.ListUtils;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DefaultScanner<T> implements Scanner<T> {

    private final ResultScannerSupplier[] resultScanners;
    private final List<List<ResultScannerSupplier>> partition;

    public DefaultScanner(ResultScannerSupplier[] resultScanners, int partitionSize) {
        this.resultScanners = Objects.requireNonNull(resultScanners, "resultScanners");
        this.partition = ListUtils.partition(Arrays.asList(resultScanners), partitionSize);
    }

    @Override
    public List<T> extractData(final ResultsExtractor<T> action) {
        final List<T> results = new ArrayList<>();
        for (List<ResultScannerSupplier> resultScanners : partition) {
            preCreateScanner(resultScanners);

            for (ResultScannerSupplier resultScanner : resultScanners) {
                try (ResultScannerSupplier scanner = resultScanner) {
                    T t = action.extractData(scanner.getScanner());
                    results.add(t);
                } catch (Throwable th) {
                    ResultScannerFactory.closeScanner(resultScanners);
                    throw new HbaseSystemException(th);
                }
            }
        }
        return results;
    }

    private void preCreateScanner(List<ResultScannerSupplier> resultScanners) {
        for (ResultScannerSupplier resultScanner : resultScanners) {
            resultScanner.getScanner();
        }
    }


    @Override
    public List<ScanMetrics> getScanMetrics() {
        List<ScanMetrics> metrics = new ArrayList<>();
        for (ResultScannerSupplier resultScanner : resultScanners) {
            metrics.add(resultScanner.getScanMetrics());
        }
        return metrics;
    }
}
