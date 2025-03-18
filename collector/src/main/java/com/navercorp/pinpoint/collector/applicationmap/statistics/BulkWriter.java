package com.navercorp.pinpoint.collector.applicationmap.statistics;

/**
 * @author emeroad
 */
public interface BulkWriter {
    void increment(RowKey rowKey, ColumnName columnName);

    void increment(RowKey rowKey, ColumnName columnName, long addition);

    void updateMax(RowKey rowKey, ColumnName columnName, long max);

    void flushLink();

    void flushAvgMax();
}
