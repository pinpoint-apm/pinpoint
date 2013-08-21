package com.nhn.pinpoint.collector.dao.hbase.statistics;

/**
 *
 */
public interface RowInfo {

    RowKey getRowKey();

    ColumnName getColumnName();

}
