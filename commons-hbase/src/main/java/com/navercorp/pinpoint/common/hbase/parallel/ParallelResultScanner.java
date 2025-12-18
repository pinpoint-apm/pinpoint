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

package com.navercorp.pinpoint.common.hbase.parallel;

import com.navercorp.pinpoint.common.hbase.HbaseAccessor;
import com.navercorp.pinpoint.common.hbase.scan.ScanUtils;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.hbase.wd.LocalScanner;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author HyunGil Jeong
 */
public class ParallelResultScanner implements ResultScanner {

    private final int saltKeySize;
    private final ScanTask[] scanTasks;

    private Result next = null;

    public ParallelResultScanner(TableName tableName, HbaseAccessor hbaseAccessor, ExecutorService executor, Scan originalScan,
                                 RowKeyDistributor keyDistributor, int numParallelThreads) throws IOException {
        Objects.requireNonNull(hbaseAccessor, "hbaseAccessor");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(originalScan, "originalScan");

        Objects.requireNonNull(keyDistributor, "keyDistributor");
        this.saltKeySize = keyDistributor.getSaltKeySize();

        final ScanTaskConfig scanTaskConfig = ScanTaskConfig.of(tableName, hbaseAccessor, saltKeySize, originalScan.getCaching());
        final Scan[] splitScans = ScanUtils.splitScans(originalScan, keyDistributor);

        this.scanTasks = createScanTasks(scanTaskConfig, splitScans, numParallelThreads);
        for (ScanTask scanTask : scanTasks) {
            executor.execute(scanTask);
        }
    }


    private ScanTask[] createScanTasks(ScanTaskConfig scanTaskConfig, Scan[] splitScans, int numParallelThreads) {
        if (splitScans.length <= numParallelThreads) {
            ScanTask[] scanTasks = new ScanTask[splitScans.length];
            for (int i = 0; i < splitScans.length; i++) {
                Scan scan = splitScans[i];
                scanTasks[i] = new ScanTask(scanTaskConfig, scan);
            }
            return scanTasks;
        } else {
            int maxIndividualScans = (splitScans.length + (numParallelThreads - 1)) / numParallelThreads;
            List<List<Scan>> scanDistributions = new ArrayList<>(numParallelThreads);
            for (int i = 0; i < numParallelThreads; i++) {
                scanDistributions.add(new ArrayList<>(maxIndividualScans));
            }
            for (int i = 0; i < splitScans.length; i++) {
                scanDistributions.get(i % numParallelThreads).add(splitScans[i]);
            }

            ScanTask[] scanTasks = new ScanTask[scanDistributions.size()];
            for (int i = 0; i < scanDistributions.size(); i++) {
                List<Scan> scanDistribution = scanDistributions.get(i);
                Scan[] scansForSingleTask = scanDistribution.toArray(new Scan[0]);
                scanTasks[i] = new ScanTask(scanTaskConfig, scansForSingleTask);
            }
            return scanTasks;
        }
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

    private Result nextInternal() throws IOException {
        Result result = null;
        LocalScanner fetchedScanner = null;
        for (ScanTask localScanner : this.scanTasks) {
            // fail fast in case of errors
            checkTask(localScanner);
            if (localScanner.isExhausted()) {
                continue;
            }
            Result localResult = localScanner.next();
            if (localResult == null) {
                continue;
            }

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

    private void checkTask(ScanTask scanTask) {
        Throwable th = scanTask.getThrowable();
        if (th != null) {
            throw new ScanTaskException(th);
        }
    }

    @Override
    public void close() {
        for (ScanTask scanTask : this.scanTasks) {
            scanTask.close();
        }
    }

    public boolean renewLease() {
        return false;
    }

    public ScanMetrics getScanMetrics() {
        return null;
    }
}
