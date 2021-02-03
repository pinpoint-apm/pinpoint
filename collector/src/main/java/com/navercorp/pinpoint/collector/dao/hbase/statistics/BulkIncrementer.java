/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.util.AtomicLongMapUtils;
import com.navercorp.pinpoint.common.util.Assert;

import com.google.common.util.concurrent.AtomicLongMap;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author HyunGil Jeong
 */
public interface BulkIncrementer {

    boolean increment(TableName tableName, RowKey rowKey, ColumnName columnName);

    Map<TableName, List<Increment>> getIncrements(RowKeyDistributorByHashPrefix rowKeyDistributor);


    class DefaultBulkIncrementer implements BulkIncrementer {

        private final RowKeyMerge rowKeyMerge;

        final AtomicLongMap<RowInfo> counter = AtomicLongMap.create();

        DefaultBulkIncrementer(RowKeyMerge rowKeyMerge) {
            this.rowKeyMerge = Objects.requireNonNull(rowKeyMerge, "rowKeyMerge");
        }

        @Override
        public boolean increment(TableName tableName, RowKey rowKey, ColumnName columnName) {
            RowInfo rowInfo = new DefaultRowInfo(tableName, rowKey, columnName);
            counter.incrementAndGet(rowInfo);
            return true;
        }

        @Override
        public Map<TableName, List<Increment>> getIncrements(RowKeyDistributorByHashPrefix rowKeyDistributor) {
            final Map<RowInfo, Long> snapshot = AtomicLongMapUtils.remove(counter);
            return rowKeyMerge.createBulkIncrement(snapshot, rowKeyDistributor);
        }
    }

    class SizeLimitedBulkIncrementer extends DefaultBulkIncrementer {
        private final AtomicLong count = new AtomicLong();

        private final int limitSize;
        private final int checkIntervalCount;
        private volatile boolean overflowState = false;

        SizeLimitedBulkIncrementer(RowKeyMerge rowKeyMerge, int limitSize) {
            super(rowKeyMerge);
            Assert.isTrue(limitSize > 0, "limit size must be ' > 0'");
            this.limitSize = limitSize;
            // executing to find out size at each call  is not good for performance
            this.checkIntervalCount = Math.max(limitSize / 100, 1);
        }

        @Override
        public boolean increment(TableName tableName, RowKey rowKey, ColumnName columnName) {
            if (count.incrementAndGet() % checkIntervalCount == 0) {
                checkState();
            }

            RowInfo rowInfo = new DefaultRowInfo(tableName, rowKey, columnName);
            if (overflowState) {
                if (!counter.containsKey(rowInfo)) {
                    return false;
                }
            }
            counter.incrementAndGet(rowInfo);
            return true;
        }

        private void checkState() {
            if (counter.size() > limitSize) {
                overflowState = true;
            } else {
                overflowState = false;
            }
        }

        @Override
        public Map<TableName, List<Increment>> getIncrements(RowKeyDistributorByHashPrefix rowKeyDistributor) {
            try {
                return super.getIncrements(rowKeyDistributor);
            } finally {
                overflowState = false;
                count.set(0L);
            }
        }
    }

}
