package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultBulkWriter implements BulkWriter {

    private final Logger logger;

    private final HbaseOperations2 hbaseTemplate;
    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final BulkIncrementer bulkIncrementer;

    private final BulkUpdater bulkUpdater;

    private final HbaseColumnFamily tableDescriptor;
    private final TableNameProvider tableNameProvider;


    public DefaultBulkWriter(String loggerName,
                             HbaseOperations2 hbaseTemplate,
                             RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                             BulkIncrementer bulkIncrementer,
                             BulkUpdater bulkUpdater,
                             HbaseColumnFamily tableDescriptor,
                             TableNameProvider tableNameProvider) {
        this.logger = LogManager.getLogger(loggerName);
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
        this.bulkIncrementer = Objects.requireNonNull(bulkIncrementer, "bulkIncrementer");
        this.bulkUpdater = Objects.requireNonNull(bulkUpdater, "bulkUpdater");
        this.tableDescriptor = Objects.requireNonNull(tableDescriptor, "tableDescriptor");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void increment(RowKey rowKey, ColumnName columnName) {
        TableName tableName = tableNameProvider.getTableName(tableDescriptor.getTable());
        this.bulkIncrementer.increment(tableName, rowKey, columnName);
    }

    @Override
    public void increment(RowKey rowKey, ColumnName columnName, long addition) {
        TableName tableName = tableNameProvider.getTableName(tableDescriptor.getTable());
        this.bulkIncrementer.increment(tableName, rowKey, columnName, addition);
    }

    @Override
    public void updateMax(RowKey rowKey, ColumnName columnName, long value) {
        TableName tableName = tableNameProvider.getTableName(tableDescriptor.getTable());
        this.bulkUpdater.updateMax(tableName, rowKey, columnName, value);
    }

    @Override
    public void flushLink() {

        // update statistics by rowkey and column for now. need to update it by rowkey later.
        Map<TableName, List<Increment>> incrementMap = bulkIncrementer.getIncrements(rowKeyDistributorByHashPrefix);

        for (Map.Entry<TableName, List<Increment>> entry : incrementMap.entrySet()) {
            TableName tableName = entry.getKey();
            List<Increment> increments = entry.getValue();
            if (logger.isDebugEnabled()) {
                logger.debug("flush {} to [{}] Increment:{}", this.getClass().getSimpleName(), tableName, increments.size());
            }
            hbaseTemplate.increment(tableName, increments);
        }

    }

    private void checkAndMax(TableName tableName, byte[] rowKey, byte[] columnName, long val) {
        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");

        hbaseTemplate.maxColumnValue(tableName, rowKey, getColumnFamilyName(), columnName, val);
    }

    @Override
    public void flushAvgMax() {

        Map<RowInfo, Long> maxUpdateMap = bulkUpdater.getMaxUpdate();
        if (logger.isDebugEnabled()) {
            final int size = maxUpdateMap.size();
            if (size > 0) {
                logger.debug("flush {} checkAndMax:{}", this.getClass().getSimpleName(), size);
            }
        }

        for (Map.Entry<RowInfo, Long> entry : maxUpdateMap.entrySet()) {
            final RowInfo rowInfo = entry.getKey();
            final Long val = entry.getValue();
            final byte[] rowKey = getDistributedKey(rowInfo.getRowKey().getRowKey());
            checkAndMax(rowInfo.getTableName(), rowKey, rowInfo.getColumnName().getColumnName(), val);
        }
    }

    private byte[] getColumnFamilyName() {
        return tableDescriptor.getName();
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
