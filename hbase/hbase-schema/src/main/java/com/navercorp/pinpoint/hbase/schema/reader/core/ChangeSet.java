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

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ChangeSet {

    private final String id;
    private final String value;
    private final List<TableChange> tableChanges;

    public ChangeSet(String id, String value, List<TableChange> tableChanges) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("id must not be empty");
        }
        this.id = id;
        this.value = Objects.requireNonNull(value, "value");
        this.tableChanges = Objects.requireNonNull(tableChanges, "tableChanges");
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public List<TableChange> getTableChanges() {
        return Collections.unmodifiableList(tableChanges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeSet changeSet = (ChangeSet) o;
        return Objects.equals(id, changeSet.id) &&
                Objects.equals(value, changeSet.value) &&
                Objects.equals(tableChanges, changeSet.tableChanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, tableChanges);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeSet{");
        sb.append("id='").append(id).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", tableChanges=").append(tableChanges);
        sb.append('}');
        return sb.toString();
    }
}
