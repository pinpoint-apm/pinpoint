package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;

import java.util.Objects;

/**
 * @author emeroad
 */
public class SyncWriter implements BulkWriter {


    private final HbaseOperations2 hbaseTemplate;
    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final HbaseColumnFamily tableDescriptor;
    private final TableNameProvider tableNameProvider;


    public SyncWriter(String loggerName,
                             HbaseOperations2 hbaseTemplate,
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
        final byte[] rowKeyBytes = getDistributedKey(rowKey.getRowKey());
        this.hbaseTemplate.incrementColumnValue(tableName, rowKeyBytes, getColumnFamilyName(), columnName.getColumnName(), 1L);
    }

    @Override
    public void updateMax(RowKey rowKey, ColumnName columnName, long value) {

        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        TableName tableName = tableNameProvider.getTableName(this.tableDescriptor.getTable());
        final byte[] rowKeyBytes = getDistributedKey(rowKey.getRowKey());
        this.hbaseTemplate.maxColumnValue(tableName, rowKeyBytes, getColumnFamilyName(), columnName.getColumnName(), value);
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

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
