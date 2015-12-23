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

package com.navercorp.pinpoint.common.hbase;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * @author HyunGil Jeong
 */
public class ParallelResultScanner implements ResultScanner {

    private static final int DEFAULT_SCAN_TASK_QUEUE_SIZE = 100;

    private final AbstractRowKeyDistributor keyDistributor;
    private final List<ScanTask> scanTasks;
    private final Result[] nextResults;
    private Result next = null;

    public ParallelResultScanner(ExecutorService executor, int numCaching, AbstractRowKeyDistributor keyDistributor, ResultScanner[] scanners) throws IOException {
        if (executor == null) {
            throw new NullPointerException("executor must not be null");
        }
        if (keyDistributor == null) {
            throw new NullPointerException("keyDistributor must not be null");
        }
        if (scanners == null) {
            throw new NullPointerException("scanners must not be null");
        }
        this.keyDistributor = keyDistributor;
        this.nextResults = new Result[scanners.length];
        this.scanTasks = new ArrayList<>(scanners.length);
        final int scanTaskQueueSize = numCaching < DEFAULT_SCAN_TASK_QUEUE_SIZE ? numCaching : DEFAULT_SCAN_TASK_QUEUE_SIZE;
        for (ResultScanner scanner : scanners) {
            ScanTask scanTask = new ScanTask(scanner, scanTaskQueueSize);
            this.scanTasks.add(scanTask);
            executor.execute(scanTask);
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

    private static class ScanTask implements Runnable {

        private static final Result END_RESULT = new Result();

        private final ResultScanner scanner;
        private final BlockingQueue<Result> resultQueue;
        private volatile boolean isQueueClosed = false;
        private volatile boolean isFinished = false;

        private ScanTask(ResultScanner scanner, int queueSize) {
            this.scanner = scanner;
            this.resultQueue = new ArrayBlockingQueue<>(queueSize);
        }

        @Override
        public void run() {
            try {
                for (Result result : this.scanner) {
                    this.resultQueue.put(result);
                    if (this.isFinished) {
                        break;
                    }
                }
                this.resultQueue.put(END_RESULT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.resultQueue.clear();
                this.resultQueue.add(END_RESULT);
            } finally {
                this.isFinished = true;
                this.scanner.close();
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
            this.isFinished = true;
            // signal threads blocked on resultQueue
            this.resultQueue.clear();
            this.resultQueue.offer(END_RESULT);
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
