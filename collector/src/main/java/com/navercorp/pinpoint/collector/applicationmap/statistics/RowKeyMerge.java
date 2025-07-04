/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class RowKeyMerge {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final byte[] family;

    public RowKeyMerge(HbaseColumnFamily columnFamily) {
        this(columnFamily.getName());
    }

    public RowKeyMerge(byte[] family) {
        Objects.requireNonNull(family, "family");

        this.family = Arrays.copyOf(family, family.length);
    }

    public Map<TableName, List<Increment>> createBulkIncrement(Map<RowInfo, Long> data, RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        if (data.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<TableName, List<Increment>> tableIncrementMap = new HashMap<>();
        final Map<TableName, Map<RowKey, List<ColumnCallCount>>> tableRowKeyMap = mergeRowKeys(data);

        for (Map.Entry<TableName, Map<RowKey, List<ColumnCallCount>>> tableRowKeys : tableRowKeyMap.entrySet()) {
            final TableName tableName = tableRowKeys.getKey();
            final List<Increment> incrementList = new ArrayList<>();
            for (Map.Entry<RowKey, List<ColumnCallCount>> rowKeyEntry : tableRowKeys.getValue().entrySet()) {
                Increment increment = createIncrement(rowKeyEntry, rowKeyDistributorByHashPrefix);
                incrementList.add(increment);


            }
            tableIncrementMap.put(tableName, incrementList);
        }
        return tableIncrementMap;
    }

    private Increment createIncrement(Map.Entry<RowKey, List<ColumnCallCount>> rowKeyEntry, RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        RowKey rowKey = rowKeyEntry.getKey();
        byte[] key = getRowKey(rowKey, rowKeyDistributorByHashPrefix);
        final Increment increment = new Increment(key);
        increment.setReturnResults(false);
        for (ColumnCallCount columnName : rowKeyEntry.getValue()) {
            increment.addColumn(family, columnName.columnName(), columnName.callCount());
        }

        logger.trace("create increment row:{}, column:{}", rowKey, rowKeyEntry.getValue());
        return increment;
    }

    private byte[] getRowKey(RowKey rowKey, RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        if (rowKeyDistributorByHashPrefix == null) {
            return rowKey.getRowKey();
        } else {
            return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey.getRowKey());
        }
    }

    private Map<TableName, Map<RowKey, List<ColumnCallCount>>> mergeRowKeys(Map<RowInfo, Long> data) {
        final Map<TableName, Map<RowKey, List<ColumnCallCount>>> tables = new HashMap<>();

        for (Map.Entry<RowInfo, Long> entry : data.entrySet()) {
            final RowInfo rowInfo = entry.getKey();
            // write callCount to columnName and throw away
            final long callCount = entry.getValue();

            final TableName tableName = rowInfo.getTableName();
            final RowKey rowKey = rowInfo.getRowKey();

            Map<RowKey, List<ColumnCallCount>> rows = tables.computeIfAbsent(tableName, k -> new HashMap<>());
            List<ColumnCallCount> columnNames = rows.computeIfAbsent(rowKey, k -> new ArrayList<>());

            ColumnCallCount columnCallCount = new ColumnCallCount(rowInfo.getColumnName().getColumnName(), callCount);
            columnNames.add(columnCallCount);
        }
        return tables;
    }

    record ColumnCallCount(byte[] columnName, long callCount) {
    }
}
