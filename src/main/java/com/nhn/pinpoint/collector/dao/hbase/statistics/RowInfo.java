package com.nhn.pinpoint.collector.dao.hbase.statistics;

/**
 * @author emeroad
 */
public interface RowInfo {

    RowKey getRowKey();

    ColumnName getColumnName();

}
