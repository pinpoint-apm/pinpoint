package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.common.hbase.CheckAndMax;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.commons.collections4.ListUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultBulkWriter implements BulkWriter {

    private final Logger logger;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final BulkIncrementer bulkIncrementer;

    private final BulkUpdater bulkUpdater;

    private final HbaseColumnFamily tableDescriptor;
    private final TableNameProvider tableNameProvider;
    private final HbaseAsyncTemplate asyncTemplate;
    private int batchSize = 200;

    public DefaultBulkWriter(String loggerName,
                             HbaseAsyncTemplate asyncTemplate,
                             RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                             BulkIncrementer bulkIncrementer,
                             BulkUpdater bulkUpdater,
                             HbaseColumnFamily tableDescriptor,
                             TableNameProvider tableNameProvider) {
        this.logger = LogManager.getLogger(loggerName);
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
        this.bulkIncrementer = Objects.requireNonNull(bulkIncrementer, "bulkIncrementer");
        this.bulkUpdater = Objects.requireNonNull(bulkUpdater, "bulkUpdater");
        this.tableDescriptor = Objects.requireNonNull(tableDescriptor, "tableDescriptor");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
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
            List<List<Increment>> partition = ListUtils.partition(increments, batchSize);
            for (List<Increment> incrementList : partition) {
                asyncTemplate.increment(tableName, incrementList);
            }
        }

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

        Map<TableName, List<CheckAndMax>> maxUpdates = new HashMap<>();
        for (Map.Entry<RowInfo, Long> entry : maxUpdateMap.entrySet()) {
            final RowInfo rowInfo = entry.getKey();
            final Long val = entry.getValue();
            final byte[] rowKey = getDistributedKey(rowInfo.getRowKey().getRowKey());
            byte[] columnName = rowInfo.getColumnName().getColumnName();
            CheckAndMax checkAndMax = new CheckAndMax(rowKey, getColumnFamilyName(), columnName, val);

            List<CheckAndMax> checkAndMaxes = maxUpdates.computeIfAbsent(rowInfo.getTableName(), k -> new ArrayList<>());
            checkAndMaxes.add(checkAndMax);
        }

        for (Map.Entry<TableName, List<CheckAndMax>> entry : maxUpdates.entrySet()) {
            TableName tableName = entry.getKey();
            List<CheckAndMax> maxs = entry.getValue();
            List<List<CheckAndMax>> partition = ListUtils.partition(maxs, batchSize);
            for (List<CheckAndMax> checkAndMaxes : partition) {
                this.asyncTemplate.maxColumnValue(tableName, checkAndMaxes);
            }
        }
    }

    private byte[] getColumnFamilyName() {
        return tableDescriptor.getName();
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
