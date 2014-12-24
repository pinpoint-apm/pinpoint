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
