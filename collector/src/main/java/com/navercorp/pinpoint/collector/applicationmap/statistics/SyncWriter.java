/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.CheckAndMax;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.Increments;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;

import java.util.Objects;

/**
 * @author emeroad
 */
public class SyncWriter implements BulkWriter {

    private static final ByteSaltKey SALT_KEY = ByteSaltKey.SALT;

    private final HbaseOperations hbaseTemplate;
    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final HbaseColumnFamily tableDescriptor;
    private final TableNameProvider tableNameProvider;


    public SyncWriter(String loggerName,
                             HbaseOperations hbaseTemplate,
                             RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                             HbaseColumnFamily tableDescriptor,
                             TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
        this.tableDescriptor = Objects.requireNonNull(tableDescriptor, "tableDescriptor");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void increment(RowKey rowKey, ColumnName columnName) {
        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        this.increment(rowKey, columnName, 1L);
    }

    @Override
    public void increment(RowKey rowKey, ColumnName columnName, long addition) {
        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        TableName tableName = tableNameProvider.getTableName(this.tableDescriptor.getTable());
        final byte[] rowKeyBytes = getDistributedKey(rowKey);

        Increment increment = Increments.increment(rowKeyBytes, getColumnFamilyName(), columnName.getColumnName(), 1);
        increment.setReturnResults(false);

        this.hbaseTemplate.increment(tableName, increment);
    }

    @Override
    public void updateMax(RowKey rowKey, ColumnName columnName, long max) {

        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        TableName tableName = tableNameProvider.getTableName(this.tableDescriptor.getTable());
        final byte[] rowKeyBytes = getDistributedKey(rowKey);
        CheckAndMax checkAndMax = new CheckAndMax(rowKeyBytes, getColumnFamilyName(), columnName.getColumnName(), max);
        this.hbaseTemplate.maxColumnValue(tableName, checkAndMax);
    }

    @Override
    public void flushLink() {
        // empty
    }

    @Override
    public void flushAvgMax() {
        // empty
    }

    private byte[] getColumnFamilyName() {
        return tableDescriptor.getName();
    }

    private byte[] getDistributedKey(RowKey rowKey) {
        byte[] bytes = rowKey.getRowKey(SALT_KEY);
        bytes[0] = rowKeyDistributorByHashPrefix.getByteHasher().getHashPrefix(bytes, SALT_KEY.size());
        return bytes;
    }
}
