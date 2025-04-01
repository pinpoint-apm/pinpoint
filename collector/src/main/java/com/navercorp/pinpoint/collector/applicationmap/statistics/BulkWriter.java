package com.navercorp.pinpoint.collector.applicationmap.statistics;

/**
 * @author emeroad
 */
public interface BulkWriter<K, V> {
    void increment(K rowKey, V columnName);

    void increment(K rowKey, V columnName, long addition);

    void updateMax(K rowKey, V columnName, long max);

    void flushLink();

    void flushAvgMax();
}
