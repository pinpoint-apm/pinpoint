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

package com.navercorp.pinpoint.common.hbase.wd;

import com.navercorp.pinpoint.common.hbase.scan.ScanUtils;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.hbase.util.ScanMetricUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.ScanResultConsumer;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.io.IOException;
import java.util.Objects;

/**
 * Copy from sematext/HBaseWD
 * Interface for client-side scanning the data written with keys distribution
 *
 * @author Alex Baranau
 */
public class DistributedScanner implements ResultScanner {
    private final int saltKeySize;

    private final LocalScanner[] localScanners;
    private Result next = null;

    public DistributedScanner(int saltKeySize, ResultScanner[] scanners) {
        Objects.requireNonNull(scanners, "scanners");

        this.saltKeySize = saltKeySize;
        this.localScanners = wrapLocalScanners(scanners);
    }

    public DistributedScanner(Table table, DistributedScan scan) {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(scan, "scan");

        this.saltKeySize = scan.getSaltKeySize();

        ResultScanner[] resultScanners = ScanUtils.newScanners(table, scan.getScans());
        this.localScanners = wrapLocalScanners(resultScanners);
    }

    public DistributedScanner(AsyncTable<ScanResultConsumer> table, DistributedScan scan) {
        Objects.requireNonNull(scan, "scan");

        this.saltKeySize = scan.getSaltKeySize();

        ResultScanner[] resultScanners = ScanUtils.newScanners(table, scan.getScans());
        this.localScanners = wrapLocalScanners(resultScanners);
    }

    private LocalScanner[] wrapLocalScanners(ResultScanner[] scanners) {
        final LocalScanner[] localScanners = new LocalScanner[scanners.length];
        for (int i = 0; i < scanners.length; i++) {
            ResultScanner scanner = scanners[i];
            localScanners[i] = new LocalScannerImpl(scanner);
        }
        return localScanners;
    }

    private boolean hasNext() throws IOException {
        if (next != null) {
            return true;
        }

        next = nextInternal();

        return next != null;
    }

    @Override
    public Result next() throws IOException {
        if (hasNext()) {
            Result toReturn = next;
            next = null;
            return toReturn;
        }

        return null;
    }

    @Override
    public void close() {
        for (LocalScanner scanner : localScanners) {
            IOUtils.closeQuietly(scanner);
        }
    }


    @Override
    public boolean renewLease() {
        return false;
    }

    @Override
    public ScanMetrics getScanMetrics() {
        ScanMetrics merge = null;
        for (LocalScanner scanner : localScanners) {
            ScanMetrics scanMetrics = scanner.getScanMetrics();
            if (scanMetrics == null) {
                continue;
            }
            if (merge == null) {
                merge = new ScanMetrics();
            }
            ScanMetricUtils.sum(merge, scanMetrics);
        }
        return merge;
    }


    private Result nextInternal() throws IOException {
        Result result = null;
        LocalScanner fetchedScanner = null;
        for (LocalScanner localScanner : localScanners) {
            if (localScanner.isExhausted()) {
                // result scanner is exhausted, don't advance it any more
                continue;
            }

            Result localResult = localScanner.next();
            if (localResult == null) {
                continue;
            }

            // if result is null or next record has original key less than the candidate to be returned
            if (result == null || CellUtils.compareFirstRow(localResult, result, saltKeySize) < 0) {
                result = localResult;
                fetchedScanner = localScanner;
            }
        }

        if (fetchedScanner != null) {
            fetchedScanner.consume();
        }
        return result;
    }
}

