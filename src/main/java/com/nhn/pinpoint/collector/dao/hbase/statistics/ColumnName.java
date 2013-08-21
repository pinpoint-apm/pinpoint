package com.nhn.pinpoint.collector.dao.hbase.statistics;

/**
 *
 */
public interface ColumnName {
    byte[] getColumnName();

    long getCallCount();

    void setCallCount(long callCount);
}
