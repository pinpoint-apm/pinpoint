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

/**
 * @author HyunGil Jeong
 */
public class DistributedScannerTableCallback<T> extends AbstractDistributedScannerTableCallback<T> {

    protected DistributedScannerTableCallback(Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> resultsExtractor) {
        super(scan, rowKeyDistributor, resultsExtractor);
    }

    @Override
    protected ResultScanner createResultScanner(ResultScanner[] scanners) throws IOException {
        return new DistributedScanner(super.rowKeyDistributor, scanners);
    }

}
