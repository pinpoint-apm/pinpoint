package com.nhn.pinpoint.collector.dao.hbase.statistics;

/**
 * @author emeroad
 */
public interface ColumnName {
    byte[] getColumnName();

    long getCallCount();

    void setCallCount(long callCount);
}
