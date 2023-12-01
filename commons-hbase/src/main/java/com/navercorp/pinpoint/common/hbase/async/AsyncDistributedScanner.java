/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.parallel.ScanUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AsyncDistributedScanner implements ResultScanner {
    private final AbstractRowKeyDistributor keyDistributor;
    private final ResultScanner[] scanners;
    private final List<Result>[] nextOfScanners;
    private Result next = null;

    @SuppressWarnings("unchecked")
    public AsyncDistributedScanner(AbstractRowKeyDistributor keyDistributor, ResultScanner[] scanners) throws IOException {
        this.keyDistributor = keyDistributor;
        this.scanners = scanners;
        this.nextOfScanners = new List[scanners.length];
        for (int i = 0; i < this.nextOfScanners.length; i++) {
            this.nextOfScanners[i] = new ArrayList<>();
        }
    }

    private boolean hasNext(int nbRows) throws IOException {
        if (next != null) {
            return true;
        }

        next = nextInternal(nbRows);

        return next != null;
    }

    @Override
    public Result next() throws IOException {
        if (hasNext(1)) {
            Result toReturn = next;
            next = null;
            return toReturn;
        }

        return null;
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
        ScanUtils.closeScanner(scanners);
    }

    @Override
    public boolean renewLease() {
        return false;
    }

    @Override
    public ScanMetrics getScanMetrics() {
        if (ArrayUtils.isEmpty(scanners)) {
            return null;
        }
        return scanners[0].getScanMetrics();
    }

    public static com.sematext.hbase.wd.DistributedScanner create(AsyncTable<?> hTable, Scan originalScan, AbstractRowKeyDistributor keyDistributor) throws IOException {
        Scan[] scans = keyDistributor.getDistributedScans(originalScan);

        ResultScanner[] rss = new ResultScanner[scans.length];
        for (int i = 0; i < scans.length; i++) {
            rss[i] = hTable.getScanner(scans[i]);
        }

        return new com.sematext.hbase.wd.DistributedScanner(keyDistributor, rss);
    }

    private Result nextInternal(int nbRows) throws IOException {
        Result result = null;
        int indexOfScannerToUse = -1;
        for (int i = 0; i < nextOfScanners.length; i++) {
            if (nextOfScanners[i] == null) {
                // result scanner is exhausted, don't advance it any more
                continue;
            }

            if (nextOfScanners[i].isEmpty()) {
                // advancing result scanner
                Result[] results = scanners[i].next(nbRows);
                if (results.length == 0) {
                    // marking result scanner as exhausted
                    nextOfScanners[i] = null;
                    continue;
                }
                nextOfScanners[i].addAll(Arrays.asList(results));
            }

            // if result is null or next record has original key less than the candidate to be returned
            if (result == null || Bytes.compareTo(keyDistributor.getOriginalKey(nextOfScanners[i].get(0).getRow()),
                    keyDistributor.getOriginalKey(result.getRow())) < 0) {
                result = nextOfScanners[i].get(0);
                indexOfScannerToUse = i;
            }
        }

        if (indexOfScannerToUse >= 0) {
            nextOfScanners[indexOfScannerToUse].remove(0);
        }

        return result;
    }

    @Override
    public Iterator<Result> iterator() {
        // Identical to HTable.ClientScanner implementation
        return new Iterator<>() {
            // The next RowResult, possibly pre-read
            Result next = null;

            // return true if there is another item pending, false if there isn't.
            // this method is where the actual advancing takes place, but you need
            // to call next() to consume it. hasNext() will only advance if there
            // isn't a pending next().
            public boolean hasNext() {
                if (next == null) {
                    try {
                        next = AsyncDistributedScanner.this.next();
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
