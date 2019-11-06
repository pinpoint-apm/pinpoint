/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import org.apache.hadoop.hbase.TableName;

import java.util.Objects;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class DefaultRowInfo implements RowInfo {

    private final TableName tableName;
    private final RowKey rowKey;
    private final ColumnName columnName;

    public DefaultRowInfo(TableName tableName, RowKey rowKey, ColumnName columnName) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.rowKey = Objects.requireNonNull(rowKey, "rowKey");
        this.columnName = Objects.requireNonNull(columnName, "columnName");
    }

    @Override
    public TableName getTableName() {
        return tableName;
    }

    @Override
    public RowKey getRowKey() {
        return rowKey;
    }

    @Override
    public ColumnName getColumnName() {
        return columnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultRowInfo that = (DefaultRowInfo) o;

        if (!tableName.equals(that.tableName)) return false;
        if (!rowKey.equals(that.rowKey)) return false;
        return columnName.equals(that.columnName);
    }

    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + rowKey.hashCode();
        result = 31 * result + columnName.hashCode();
        return result;
    }
}
