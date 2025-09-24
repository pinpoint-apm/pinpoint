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

import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
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

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    public RowKeyMerge(RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.rowKeyDistributorByHashPrefix = rowKeyDistributorByHashPrefix;
    }

    public Map<TableName, List<Increment>> createBulkIncrement(Map<RowInfo, Long> data) {
        final Map<TableKey, Map<RowKey, List<ColumnCallCount>>> tableRowKeyMap = mergeRowKeys(data);

        final Map<TableName, List<Increment>> tableIncrementMap = new HashMap<>();
        for (Map.Entry<TableKey, Map<RowKey, List<ColumnCallCount>>> tableRowKeys : tableRowKeyMap.entrySet()) {
            final TableKey tableKey = tableRowKeys.getKey();
            final List<Increment> incrementList = new ArrayList<>();
            for (Map.Entry<RowKey, List<ColumnCallCount>> rowKeyEntry : tableRowKeys.getValue().entrySet()) {
                Increment increment = createIncrement(tableKey, rowKeyEntry);
                incrementList.add(increment);
            }
            tableIncrementMap.put(tableKey.tableName(), incrementList);
        }
        return tableIncrementMap;
    }

    private Increment createIncrement(TableKey tableKey, Map.Entry<RowKey, List<ColumnCallCount>> rowKeyEntry) {
        RowKey rowKey = rowKeyEntry.getKey();
        byte[] key = getRowKey(rowKey, rowKeyDistributorByHashPrefix);
        final Increment increment = new Increment(key);
        increment.setReturnResults(false);
        for (ColumnCallCount columnName : rowKeyEntry.getValue()) {
            increment.addColumn(tableKey.family(), columnName.columnName(), columnName.callCount());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("create increment row:{}, column:{}", rowKey, rowKeyEntry.getValue());
        }
        return increment;
    }

    private byte[] getRowKey(RowKey rowKey, RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        if (rowKeyDistributorByHashPrefix == null) {
            return rowKey.getRowKey(ByteSaltKey.NONE.size());
        } else {
            ByteHasher hasher = rowKeyDistributorByHashPrefix.getByteHasher();
            byte[] bytes = rowKey.getRowKey(hasher.getSaltKey().size());
            return hasher.writeSaltKey(bytes);
        }
    }

    private Map<TableKey, Map<RowKey, List<ColumnCallCount>>> mergeRowKeys(Map<RowInfo, Long> data) {
        final Map<TableKey, Map<RowKey, List<ColumnCallCount>>> tables = new HashMap<>();

        for (Map.Entry<RowInfo, Long> entry : data.entrySet()) {
            final RowInfo rowInfo = entry.getKey();
            // write callCount to columnName and throw away
            final long callCount = entry.getValue();

            final RowKey rowKey = rowInfo.rowKey();

            final TableKey tableKey = new TableKey(rowInfo.tableName(), rowInfo.family());
            Map<RowKey, List<ColumnCallCount>> rows = tables.computeIfAbsent(tableKey, k -> new HashMap<>());
            List<ColumnCallCount> columnNames = rows.computeIfAbsent(rowKey, k -> new ArrayList<>());

            ColumnCallCount columnCallCount = new ColumnCallCount(rowInfo.columnName().getColumnName(), callCount);
            columnNames.add(columnCallCount);
        }
        return tables;
    }

    record TableKey(TableName tableName, byte[] family) {

        public TableKey {
            Objects.requireNonNull(tableName, "tableName");
            Objects.requireNonNull(family, "family");
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            TableKey tableKey = (TableKey) o;
            return Arrays.equals(family, tableKey.family) && tableName.equals(tableKey.tableName);
        }

        @Override
        public int hashCode() {
            int result = tableName.hashCode();
            result = 31 * result + Arrays.hashCode(family);
            return result;
        }
    }

    record ColumnCallCount(byte[] columnName, long callCount) {
    }
}
