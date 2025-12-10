/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowInfo;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import org.apache.hadoop.hbase.TableName;

import java.util.Objects;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public record DefaultRowInfo(TableName tableName,
                             RowKey rowKey,
                             ColumnName columnName) implements RowInfo {

    public DefaultRowInfo {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnName, "columnName");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        DefaultRowInfo that = (DefaultRowInfo) o;
        return rowKey.equals(that.rowKey) && tableName.equals(that.tableName) && columnName.equals(that.columnName);
    }

    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + rowKey.hashCode();
        result = 31 * result + columnName.hashCode();
        return result;
    }
}
