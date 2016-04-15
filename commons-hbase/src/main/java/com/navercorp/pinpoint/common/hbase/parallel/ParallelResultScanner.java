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

import com.navercorp.pinpoint.common.hbase.HbaseAccessor;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author HyunGil Jeong
 */
public class ParallelResultScanner implements ResultScanner {

    private final AbstractRowKeyDistributor keyDistributor;
    private final List<ScanTask> scanTasks;
    private final Result[] nextResults;
    private Result next = null;

    public ParallelResultScanner(TableName tableName, HbaseAccessor hbaseAccessor, ExecutorService executor, Scan originalScan, AbstractRowKeyDistributor keyDistributor, int numParallelThreads) throws IOException {
        if (hbaseAccessor == null) {
            throw new NullPointerException("hbaseAccessor must not be null");
        }
        if (executor == null) {
            throw new NullPointerException("executor must not be null");
        }
        if (keyDistributor == null) {
            throw new NullPointerException("keyDistributor must not be null");
        }
        if (originalScan == null) {
            throw new NullPointerException("originalScan must not be null");
        }
        this.keyDistributor = keyDistributor;

        final ScanTaskConfig scanTaskConfig = new ScanTaskConfig(tableName, hbaseAccessor, keyDistributor, originalScan.getCaching());
        final Scan[] splitScans = splitScans(originalScan);

        this.scanTasks = createScanTasks(scanTaskConfig, splitScans, numParallelThreads);
        this.nextResults = new Result[scanTasks.size()];
        for (ScanTask scanTask : scanTasks) {
            executor.execute(scanTask);
        }
    }

    private Scan[] splitScans(Scan originalScan) throws IOException {
        Scan[] scans = this.keyDistributor.getDistributedScans(originalScan);
        for (int i = 0; i < scans.length; ++i) {
            Scan scan = scans[i];
            scan.setId(originalScan.getId() + "-" + i);
        }
        return scans;
    }

    private List<ScanTask> createScanTasks(ScanTaskConfig scanTaskConfig, Scan[] splitScans, int numParallelThreads) {
        if (splitScans.length <= numParallelThreads) {
            List<ScanTask> scanTasks = new ArrayList<>(splitScans.length);
            for (Scan scan : splitScans) {
                scanTasks.add(new ScanTask(scanTaskConfig, scan));
            }
            return scanTasks;
        } else {
            int maxIndividualScans = (splitScans.length + (numParallelThreads - 1)) / numParallelThreads;
            List<List<Scan>> scanDistributions = new ArrayList<>(numParallelThreads);
            for (int i = 0; i < numParallelThreads; ++i) {
                scanDistributions.add(new ArrayList<Scan>(maxIndividualScans));
            }
            for (int i = 0; i < splitScans.length; ++i) {
                scanDistributions.get(i % numParallelThreads).add(splitScans[i]);
            }
            List<ScanTask> scanTasks = new ArrayList<>(numParallelThreads);
            for (List<Scan> scanDistribution : scanDistributions) {
                Scan[] scansForSingleTask = scanDistribution.toArray(new Scan[scanDistribution.size()]);
                scanTasks.add(new ScanTask(scanTaskConfig, scansForSingleTask));
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
        int indexOfResultToUse = -1;
        for (int i = 0; i < this.scanTasks.size(); ++i) {
            ScanTask scanTask = this.scanTasks.get(i);
            // fail fast in case of errors
            checkTask(scanTask);
            if (nextResults[i] == null) {
                try {
                    nextResults[i] = scanTask.getResult();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                if (nextResults[i] == null) {
                    continue;
                }
            }
            if (result == null || Bytes.compareTo(keyDistributor.getOriginalKey(nextResults[i].getRow()),
                    keyDistributor.getOriginalKey(result.getRow())) < 0) {
                result = nextResults[i];
                indexOfResultToUse = i;
            }
        }
        if (indexOfResultToUse >= 0) {
            nextResults[indexOfResultToUse] = null;
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
    public Result[] next(int nbRows) throws IOException {
        // Identical to HTable.ClientScanner implementation
        // Collect values to be returned here
        ArrayList<Result> resultSets = new ArrayList<>(nbRows);
        for (int i = 0; i < nbRows; i++) {
            Result next = next();
            if (next != null) {
                resultSets.add(next);
            } else {
                break;
            }
        }
        return resultSets.toArray(new Result[resultSets.size()]);
    }

    @Override
    public void close() {
        for (ScanTask scanTask : this.scanTasks) {
            scanTask.close();
        }
    }

    @Override
    public Iterator<Result> iterator() {
        // Identical to HTable.ClientScanner implementation
        return new Iterator<Result>() {
            // The next RowResult, possibly pre-read
            Result next = null;

            // return true if there is another item pending, false if there isn't.
            // this method is where the actual advancing takes place, but you need
            // to call next() to consume it. hasNext() will only advance if there
            // isn't a pending next().
            public boolean hasNext() {
                if (next == null) {
                    try {
                        next = ParallelResultScanner.this.next();
                        return next != null;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return true;
            }

            // get the pending next item and advance the iterator. returns null if
            // there is no next item.
            public Result next() {
                // since hasNext() does the real advancing, we call this to determine
                // if there is a next before proceeding.
                if (!hasNext()) {
                    return null;
                }

                // if we get to here, then hasNext() has given us an item to return.
                // we want to return the item and then null out the next pointer, so
                // we use a temporary variable.
                Result temp = next;
                next = null;
                return temp;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
