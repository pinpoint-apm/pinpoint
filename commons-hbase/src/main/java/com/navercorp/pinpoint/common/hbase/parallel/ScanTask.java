/*
 * Copyright 2015 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase.parallel;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.DistributedScanner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTableInterfaceFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.data.hadoop.hbase.HbaseUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author HyunGil Jeong
 */
public class ScanTask implements Runnable {

    private static final Result END_RESULT = new Result();

    private final String tableName;
    private final Configuration configuration;
    private final Charset charset;
    private final HTableInterfaceFactory tableFactory;
    private final AbstractRowKeyDistributor rowKeyDistributor;

    private final Scan[] scans;
    private final BlockingQueue<Result> resultQueue;

    private volatile Throwable throwable;
    private volatile boolean isQueueClosed = false;
    private volatile boolean isDone = false;

    public ScanTask(ScanTaskConfig scanTaskConfig, Scan... scans) {
        if (scanTaskConfig == null) {
            throw new NullPointerException("scanTaskConfig must not be null");
        }
        if (scans == null) {
            throw new NullPointerException("scans must not be null");
        }
        if (scans.length == 0) {
            throw new IllegalArgumentException("scans must not be empty");
        }
        this.tableName = scanTaskConfig.getTableName();
        this.configuration = scanTaskConfig.getConfiguration();
        this.charset = scanTaskConfig.getCharset();
        this.tableFactory = scanTaskConfig.getTableFactory();
        this.rowKeyDistributor = scanTaskConfig.getRowKeyDistributor();
        this.scans = scans;
        this.resultQueue = new ArrayBlockingQueue<>(scanTaskConfig.getScanTaskQueueSize());
    }

    @Override
    public void run() {
        HTableInterface table = null;
        try {
            table = HbaseUtils.getHTable(this.tableName, this.configuration, this.charset, this.tableFactory);
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
                scanner.close();
            }
        } catch (Throwable th) {
            this.throwable = th;
            this.resultQueue.clear();
            this.resultQueue.offer(END_RESULT);
        } finally {
            HbaseUtils.releaseTable(this.tableName, table, this.tableFactory);
        }
    }

    private ResultScanner createResultScanner(HTableInterface table) throws IOException {
        if (scans.length == 1) {
            Scan scan = scans[0];
            return table.getScanner(scan);
        } else {
            ResultScanner[] scanners = new ResultScanner[this.scans.length];
            for (int i = 0; i < scanners.length; ++i) {
                scanners[i] = table.getScanner(this.scans[i]);
            }
            return new DistributedScanner(this.rowKeyDistributor, scanners);
        }
    }

    public Result getResult() throws InterruptedException {
        if (this.isQueueClosed) {
            return null;
        }
        Result take = this.resultQueue.take();
        if (take == END_RESULT) {
            this.isQueueClosed = true;
            return null;
        }
        return take;
    }

    public void close() {
        this.isDone = true;
        // signal threads blocked on resultQueue
        this.resultQueue.clear();
        this.resultQueue.add(END_RESULT);
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

}
