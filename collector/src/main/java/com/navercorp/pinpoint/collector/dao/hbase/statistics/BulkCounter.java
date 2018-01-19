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
import org.apache.hadoop.hbase.TableName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HyunGil Jeong
 */
public class BulkCounter {

    private final Map<TableName, AtomicLongMap<RowInfo>> counters = new ConcurrentHashMap<>();

    public void increment(TableName tableName, RowInfo rowInfo) {
        AtomicLongMap<RowInfo> counter = counters.get(tableName);
        if (counter == null) {
            counter = AtomicLongMap.create();
            AtomicLongMap<RowInfo> prevCounter = counters.putIfAbsent(tableName, counter);
            if (prevCounter != null) {
                counter = prevCounter;
            }
        }
        counter.incrementAndGet(rowInfo);
    }

    public Map<TableName, Map<RowInfo, Long>> getAndReset() {
        final Map<TableName, Map<RowInfo, Long>> snapshot = new HashMap<>();
        for (Map.Entry<TableName, AtomicLongMap<RowInfo>> e : counters.entrySet()) {
            AtomicLongMap<RowInfo> counter = e.getValue();
            if (counter != null) {
                TableName tableName = e.getKey();
                snapshot.put(tableName, AtomicLongMapUtils.remove(counter));
            }
        }
        return snapshot;
    }
}
