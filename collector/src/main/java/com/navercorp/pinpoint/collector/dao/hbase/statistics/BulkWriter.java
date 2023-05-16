package com.navercorp.pinpoint.collector.dao.hbase.statistics;

/**
 * @author emeroad
 */
public interface BulkWriter {
    void increment(RowKey rowKey, ColumnName columnName);

    void increment(RowKey rowKey, ColumnName columnName, long addition);

    void updateMax(RowKey rowKey, ColumnName columnName, long value);

    void flushLink();

    void flushAvgMax();
}
