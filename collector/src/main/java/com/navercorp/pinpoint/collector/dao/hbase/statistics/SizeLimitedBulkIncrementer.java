/*
 * Copyright 2021 NAVER Corp.
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
package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.common.util.Assert;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;

import java.util.List;
import java.util.Map;
import java.util.Objects;

class SizeLimitedBulkIncrementer implements BulkIncrementer, BulkState {

    private volatile boolean overflowState = false;

    private final BulkIncrementer delegate;
    private final int limitSize;
    private final BulkOperationReporter reporter;

    SizeLimitedBulkIncrementer(BulkIncrementer delegate, int limitSize, BulkOperationReporter reporter) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");

        Assert.isTrue(limitSize > 0, "limit size must be ' > 0'");
        this.limitSize = limitSize;

        this.reporter = Objects.requireNonNull(reporter, "reporter");
    }

    @Override
    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName) {
        this.increment(tableName, rowKey, columnName, 1L);
    }

    @Override
    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName, long addition) {
        if (overflowState) {
            reporter.reportReject();
            return;
        }
        delegate.increment(tableName, rowKey, columnName, addition);
    }

    // Called by monitoring thread
    @Override
    public boolean checkState() {
        if (delegate.getSize() > limitSize) {
            overflowState = true;
            return false;
        } else {
            overflowState = false;
            return true;
        }
    }

    @Override
    public Map<TableName, List<Increment>> getIncrements(RowKeyDistributorByHashPrefix rowKeyDistributor) {
        try {
            return delegate.getIncrements(rowKeyDistributor);
        } finally {
            reporter.reportFlushAll();
            overflowState = false;
        }
    }

    @Override
    public int getSize() {
        return this.delegate.getSize();
    }
}
