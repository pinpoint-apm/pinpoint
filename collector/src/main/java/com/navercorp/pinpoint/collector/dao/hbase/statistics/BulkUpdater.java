package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import org.apache.hadoop.hbase.TableName;

import java.util.Map;

public interface BulkUpdater {

    void updateMax(TableName tableName, RowKey rowKey, ColumnName columnName, long value);

    Map<RowInfo, Long> getMaxUpdate();

}
