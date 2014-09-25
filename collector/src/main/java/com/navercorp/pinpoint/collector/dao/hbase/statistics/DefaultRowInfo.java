package com.nhn.pinpoint.collector.dao.hbase.statistics;

/**
 * @author emeroad
 */
public class DefaultRowInfo implements RowInfo {

    private RowKey rowKey;
    private ColumnName columnName;

    public DefaultRowInfo(RowKey rowKey, ColumnName columnName) {
        if (rowKey == null) {
            throw new NullPointerException("rowKey must not be null");
        }
        if (columnName == null) {
            throw new NullPointerException("columnName must not be null");
        }

        this.rowKey = rowKey;
        this.columnName = columnName;
    }

    public RowKey getRowKey() {
        return rowKey;
    }

    public ColumnName getColumnName() {
        return columnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultRowInfo that = (DefaultRowInfo) o;

        if (!columnName.equals(that.columnName)) return false;
        if (!rowKey.equals(that.rowKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rowKey.hashCode();
        result = 31 * result + columnName.hashCode();
        return result;
    }
}
