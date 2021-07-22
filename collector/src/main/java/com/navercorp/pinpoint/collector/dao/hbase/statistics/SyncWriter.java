package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author emeroad
 */
public class SyncWriter implements BulkWriter {

    private final Logger logger;

    private final HbaseOperations2 hbaseTemplate;
    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final TableDescriptor<? extends HbaseColumnFamily> tableDescriptor;
    private final TableName tableName;


    public SyncWriter(String loggerName,
                             HbaseOperations2 hbaseTemplate,
                             RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                            TableDescriptor<? extends HbaseColumnFamily> tableDescriptor) {
        this.logger = LoggerFactory.getLogger(loggerName);
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
        this.tableDescriptor = Objects.requireNonNull(tableDescriptor, "tableDescriptor");
        this.tableName = this.tableDescriptor.getTableName();
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

        final byte[] rowKeyBytes = getDistributedKey(rowKey.getRowKey());
        this.hbaseTemplate.incrementColumnValue(tableName, rowKeyBytes, getColumnFamilyName(), columnName.getColumnName(), 1L);
    }

    @Override
    public void updateMax(RowKey rowKey, ColumnName columnName, long value) {

        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

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
        return tableDescriptor.getColumnFamilyName();
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
