/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.google.common.util.concurrent.AtomicLongMap;
import com.navercorp.pinpoint.collector.util.AtomicLongMapUtils;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import org.apache.hadoop.hbase.TableName;

import java.util.Map;

public class DefaultBulkIncrementer implements BulkIncrementer {

    private final AtomicLongMap<RowInfo> counter = AtomicLongMap.create();

    public DefaultBulkIncrementer() {
    }

    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName) {
        increment(tableName, rowKey, columnName, 1L);
    }

    public void increment(TableName tableName, RowKey rowKey, ColumnName columnName, long addition) {
        RowInfo rowInfo = new DefaultRowInfo(tableName, rowKey, columnName);
        counter.addAndGet(rowInfo, addition);
    }

    @Override
    public Map<RowInfo, Long> getIncrements() {
        return AtomicLongMapUtils.remove(counter);
    }

    @Override
    public int getSize() {
        return counter.size();
    }
}
