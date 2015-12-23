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
import com.sematext.hbase.wd.DistributedScanner;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.data.hadoop.hbase.ResultsExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author HyunGil Jeong
 */
public class ParallelDistributedScannerTableCallback<T> extends AbstractDistributedScannerTableCallback<T> {

    private final ExecutorService executor;
    private final int numCaching;
    private final int numParallelThreads;

    protected ParallelDistributedScannerTableCallback(Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> resultsExtractor, ExecutorService executor, int numParallelThreads) {
        super(scan, rowKeyDistributor, resultsExtractor);
        this.executor = executor;
        this.numCaching = scan.getCaching();
        this.numParallelThreads = numParallelThreads;
    }

    @Override
    protected ResultScanner createResultScanner(ResultScanner[] scanners) throws IOException {
        if (scanners.length < this.numParallelThreads) {
            return new ParallelResultScanner(this.executor, this.numCaching, rowKeyDistributor, scanners);
        } else {
            ResultScanner[] distributedScanners = createDistributedScanners(scanners);
            return new ParallelResultScanner(this.executor, this.numCaching, rowKeyDistributor, distributedScanners);
        }
    }

    private ResultScanner[] createDistributedScanners(ResultScanner[] scanners) throws IOException {
        int maxIndividualScans = (scanners.length + (this.numParallelThreads - 1)) / this.numParallelThreads;
        List<List<ResultScanner>> scannerDistributions = new ArrayList<>(this.numParallelThreads);
        for (int i = 0; i < this.numParallelThreads; ++i) {
            scannerDistributions.add(new ArrayList<ResultScanner>(maxIndividualScans));
        }
        for (int i = 0; i < scanners.length; ++i) {
            scannerDistributions.get(i % this.numParallelThreads).add(scanners[i]);
        }
        DistributedScanner[] distributedScanners = new DistributedScanner[this.numParallelThreads];
        for (int i = 0; i < distributedScanners.length; ++i) {
            List<ResultScanner> scannerDistribution = scannerDistributions.get(i);
            ResultScanner[] scannersForDistributedScanner = scannerDistribution.toArray(new ResultScanner[scannerDistribution.size()]);
            distributedScanners[i] = new DistributedScanner(rowKeyDistributor, scannersForDistributedScanner);
        }
        return distributedScanners;
    }

}
