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

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ModifyTableChange extends TableChange {

    public ModifyTableChange(
            String name,
            TableConfiguration tableConfiguration,
            List<ColumnFamilyChange> columnFamilyChanges) {
        super(name, tableConfiguration, columnFamilyChanges);
    }

    @Override
    public ChangeType getType() {
        return ChangeType.MODIFY;
    }

    @Override
    public byte[][] getSplitKeys() {
        throw new UnsupportedOperationException("modify table does not have split keys");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModifyTableChange{");
        sb.append("name='").append(getName()).append('\'');
        sb.append(", configuration=").append(getTableConfiguration());
        sb.append(", columnFamilyChanges=").append(getColumnFamilyChanges());
        sb.append('}');
        return sb.toString();
    }
}
