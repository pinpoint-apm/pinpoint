/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.reader.core;

import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public abstract class TableChange implements Change {

    private final String name;
    private final TableConfiguration tableConfiguration;
    private final List<ColumnFamilyChange> columnFamilyChanges;

    public TableChange(
            String name,
            TableConfiguration tableConfiguration,
            List<ColumnFamilyChange> columnFamilyChanges) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name must not be empty");
        }
        this.name = name;
        this.tableConfiguration = Objects.requireNonNull(tableConfiguration, "tableConfiguration");
        this.columnFamilyChanges = Objects.requireNonNull(columnFamilyChanges, "columnFamilyChanges");
    }

    @Override
    public String getName() {
        return name;
    }

    public TableConfiguration getTableConfiguration() {
        return tableConfiguration;
    }

    public List<ColumnFamilyChange> getColumnFamilyChanges() {
        return Collections.unmodifiableList(columnFamilyChanges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableChange that = (TableChange) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(tableConfiguration, that.tableConfiguration) &&
                Objects.equals(columnFamilyChanges, that.columnFamilyChanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tableConfiguration, columnFamilyChanges);
    }

    public abstract byte[][] getSplitKeys();
}
