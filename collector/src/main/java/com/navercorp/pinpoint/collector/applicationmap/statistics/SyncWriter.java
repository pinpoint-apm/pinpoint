package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.hbase.CheckAndMax;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.Increments;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;

import java.util.Objects;

/**
 * @author emeroad
 */
public class SyncWriter implements BulkWriter {


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
        final byte[] rowKeyBytes = getDistributedKey(rowKey.getRowKey());

        Increment increment = Increments.increment(rowKeyBytes, getColumnFamilyName(), columnName.getColumnName(), 1);
        increment.setReturnResults(false);

        this.hbaseTemplate.increment(tableName, increment);
    }

    @Override
    public void updateMax(RowKey rowKey, ColumnName columnName, long max) {

        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        TableName tableName = tableNameProvider.getTableName(this.tableDescriptor.getTable());
        final byte[] rowKeyBytes = getDistributedKey(rowKey.getRowKey());
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

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
