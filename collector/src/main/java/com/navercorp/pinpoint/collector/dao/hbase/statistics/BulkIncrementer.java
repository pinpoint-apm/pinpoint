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

import com.google.common.util.concurrent.AtomicLongMap;
import com.navercorp.pinpoint.collector.util.AtomicLongMapUtils;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class BulkIncrementer {

    private final RowKeyMerge rowKeyMerge;

    private final AtomicLongMap<RowInfo> counter = AtomicLongMap.create();

    public BulkIncrementer(RowKeyMerge rowKeyMerge) {
        this.rowKeyMerge = Objects.requireNonNull(rowKeyMerge, "rowKeyMerge");
    }

    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName) {
        RowInfo rowInfo = new DefaultRowInfo(tableName, rowKey, columnName);
        counter.incrementAndGet(rowInfo);
    }

    public Map<TableName, List<Increment>> getIncrements(RowKeyDistributorByHashPrefix rowKeyDistributor) {
        final Map<RowInfo, Long> snapshot = AtomicLongMapUtils.remove(counter);
        return rowKeyMerge.createBulkIncrement(snapshot, rowKeyDistributor);
    }
}
