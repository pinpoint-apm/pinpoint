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

import com.navercorp.pinpoint.common.hbase.TableFactory;
import com.navercorp.pinpoint.common.hbase.scan.ScanUtils;
import com.navercorp.pinpoint.common.hbase.util.ScanMetricUtils;
import com.navercorp.pinpoint.common.hbase.wd.DistributedScanner;
import com.navercorp.pinpoint.common.hbase.wd.LocalScanner;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author HyunGil Jeong
 */
public class ScanTask implements Runnable, LocalScanner {

    private static final Result END_RESULT = new Result();

    private final TableName tableName;
    private final TableFactory tableFactory;
    private final int saltKeySize;

    private final Scan[] scans;
    private final BlockingQueue<Result> resultQueue;

    private volatile Throwable throwable;
    private volatile boolean isQueueClosed = false;
    private volatile boolean isDone = false;

    private Result localBuffer;

    private final ScanMetrics scanMetrics;

    public ScanTask(ScanTaskConfig scanTaskConfig, Scan... scans) {
        Objects.requireNonNull(scanTaskConfig, "scanTaskConfig");
        Assert.notEmpty(scans, "scans");
        this.tableName = scanTaskConfig.getTableName();
        this.tableFactory = scanTaskConfig.getTableFactory();
        this.saltKeySize = scanTaskConfig.getSaltKeySize();
        this.scans = scans;
        this.resultQueue = new ArrayBlockingQueue<>(scanTaskConfig.getScanTaskQueueSize());

        this.scanMetrics = getScanMetrics(scanTaskConfig.isScanMetricsEnabled());
    }

    private ScanMetrics getScanMetrics(boolean scanMetricsEnabled) {
        if (scanMetricsEnabled) {
            return new ScanMetrics();
        }
        return null;
    }

    @Override
    public void run() {
        Table table = null;
        try {
            // TODO Avoid ThreadPool Deadlock : tableFactory.getTable(this.tableName, ParallelScannerThreadPool);
            table = tableFactory.getTable(this.tableName);
            ResultScanner scanner = createResultScanner(table);
            try {
                for (Result result : scanner) {
                    this.resultQueue.put(result);
                    if (this.isDone) {
                        break;
                    }
                }
            } finally {
                this.isDone = true;
                this.resultQueue.put(END_RESULT);
                ScanUtils.closeScanner(scanner);
                if (scanMetrics != null) {
                    ScanMetricUtils.sum(scanMetrics, scanner.getScanMetrics());
                }
            }
        } catch (Throwable th) {
            this.throwable = th;
            this.resultQueue.clear();
            this.resultQueue.offer(END_RESULT);
        } finally {
            tableFactory.releaseTable(table);
        }
    }

    private ResultScanner createResultScanner(Table table) throws IOException {
        if (scans.length == 1) {
            Scan scan = scans[0];
            return table.getScanner(scan);
        } else {
            ResultScanner[] scanners = ScanUtils.newScanners(table, scans);
            return new DistributedScanner(saltKeySize, scanners);
        }
    }

    @Override
    public Result next() throws IOException {
        if (this.isQueueClosed) {
            return null;
        }
        if (localBuffer != null) {
            return localBuffer;
        }
        Result take = null;
        try {
            take = this.resultQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            isDone = true;
            throw new InterruptedIOException("ScanTask Interrupted");
        }
        if (take == END_RESULT) {
            this.isQueueClosed = true;
            localBuffer = null;
            return null;
        }
        localBuffer = take;
        return localBuffer;
    }

    @Override
    public void consume() {
        localBuffer = null;
    }

    @Override
    public boolean isExhausted() {
        return this.isQueueClosed;
    }

    @Override
    public void close() {
        this.isDone = true;
        // signal threads blocked on resultQueue
        this.resultQueue.clear();
        this.resultQueue.add(END_RESULT);
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public ScanMetrics getScanMetrics() {
        return scanMetrics;
    }
}
