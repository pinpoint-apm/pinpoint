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
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.hbase.wd.DistributedScan;
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

        final DistributedScan dScan = keyDistributor.getDistributedScans(originalScan);
        final ScanTaskConfig scanTaskConfig = ScanTaskConfig.of(tableName, hbaseAccessor.getTableFactory(), saltKeySize,
                originalScan.getCaching(), hbaseAccessor.getConfiguration(), dScan.isEnableScanMetrics());

        this.scanTasks = createScanTasks(scanTaskConfig, dScan, numParallelThreads);
        for (ScanTask scanTask : scanTasks) {
            executor.execute(scanTask);
        }
    }


    private ScanTask[] createScanTasks(ScanTaskConfig scanTaskConfig, DistributedScan distributedScan, int numParallelThreads) {
        Scan[] splitScans = distributedScan.getScans();
        if (splitScans.length <= numParallelThreads) {
            ScanTask[] scanTasks = new ScanTask[splitScans.length];
            for (int i = 0; i < splitScans.length; i++) {
                Scan scan = splitScans[i];
                scanTasks[i] = new ScanTask(scanTaskConfig, scan);
            }
            return scanTasks;
        } else {
            List<List<Scan>> scanDistributions = scanDistributions(splitScans, numParallelThreads);

            ScanTask[] scanTasks = new ScanTask[scanDistributions.size()];
            for (int i = 0; i < scanDistributions.size(); i++) {
                List<Scan> scanDistribution = scanDistributions.get(i);
                Scan[] scansForSingleTask = scanDistribution.toArray(new Scan[0]);
                scanTasks[i] = new ScanTask(scanTaskConfig, scansForSingleTask);
            }
            return scanTasks;
        }
    }

    private List<List<Scan>> scanDistributions(Scan[] scans, int numParallelThreads) {
        int numDistributions = Math.min(scans.length, numParallelThreads);
        int initialCapacity = (scans.length + numDistributions - 1) / numDistributions;

        List<List<Scan>> distributions = new ArrayList<>(numDistributions);
        for (int i = 0; i < numDistributions; i++) {
            distributions.add(new ArrayList<>(initialCapacity));
        }

        for (int i = 0; i < scans.length; i++) {
            int bucketIndex = i % numDistributions;
            distributions.get(bucketIndex).add(scans[i]);
        }
        return distributions;
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
