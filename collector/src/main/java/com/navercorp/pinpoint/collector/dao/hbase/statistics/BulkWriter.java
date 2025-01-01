package com.navercorp.pinpoint.collector.dao.hbase.statistics;

/**
 * @author emeroad
 */
public interface BulkWriter<K, V> {
    void increment(K rowKey, V columnName);

    void increment(K rowKey, V columnName, long addition);

    void updateMax(K rowKey, V columnName, long value);

    void flushLink();

    void flushAvgMax();
}
