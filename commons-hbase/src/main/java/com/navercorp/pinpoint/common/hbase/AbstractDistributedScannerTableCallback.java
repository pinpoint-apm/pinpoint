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

import com.navercorp.pinpoint.common.util.StopWatch;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.TableCallback;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public abstract class AbstractDistributedScannerTableCallback<T> implements TableCallback<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final boolean debugEnabled = this.logger.isDebugEnabled();

    protected final Scan scan;
    protected final AbstractRowKeyDistributor rowKeyDistributor;
    protected final ResultsExtractor<T> resultsExtractor;

    protected AbstractDistributedScannerTableCallback(Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> resultsExtractor) {
        this.scan = scan;
        this.rowKeyDistributor = rowKeyDistributor;
        this.resultsExtractor = resultsExtractor;
    }

    @Override
    public T doInTable(HTableInterface table) throws Throwable {
        StopWatch watch = null;
        if (debugEnabled) {
            watch = new StopWatch();
            watch.start();
        }
        final ResultScanner[] splitScanners = splitScanners(table);
        final ResultScanner scanner = createResultScanner(splitScanners);
        if (debugEnabled) {
            logger.debug("DistributedScanner createTime:{}", watch.stop());
            watch.start();
        }
        try {
            return resultsExtractor.extractData(scanner);
        } finally {
            scanner.close();
            if (debugEnabled) {
                logger.debug("DistributedScanner scanTime:{}", watch.stop());
            }
        }
    }

    protected abstract ResultScanner createResultScanner(ResultScanner[] scanners) throws IOException;

    private ResultScanner[] splitScanners(HTableInterface htable) throws IOException {
        Scan[] scans = this.rowKeyDistributor.getDistributedScans(this.scan);
        final int length = scans.length;
        for(int i = 0; i < length; i++) {
            Scan scan = scans[i];
            // other properties are already set upon construction
            scan.setId(this.scan.getId() + "-" + i);
        }

        ResultScanner[] scanners = new ResultScanner[length];
        boolean success = false;
        try {
            for (int i = 0; i < length; i++) {
                scanners[i] = htable.getScanner(scans[i]);
            }
            success = true;
        } finally {
            if (!success) {
                closeScanner(scanners);
            }
        }
        return scanners;
    }

    private void closeScanner(ResultScanner[] scannerList ) {
        for (ResultScanner scanner : scannerList) {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Exception e) {
                    logger.warn("Scanner.close() error Caused:{}", e.getMessage(), e);
                }
            }
        }
    }
}
