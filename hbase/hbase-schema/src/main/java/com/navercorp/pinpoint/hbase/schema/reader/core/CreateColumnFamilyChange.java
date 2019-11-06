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

/**
 * @author HyunGil Jeong
 */
public class CreateColumnFamilyChange extends ColumnFamilyChange {

    public CreateColumnFamilyChange(String name, ColumnFamilyConfiguration columnFamilyConfiguration) {
        super(name, columnFamilyConfiguration);
    }

    @Override
    public ChangeType getType() {
        return ChangeType.CREATE;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateColumnFamilyChange{");
        sb.append("name='").append(getName()).append('\'');
        sb.append(", configuration=").append(getColumnFamilyConfiguration());
        sb.append('}');
        return sb.toString();
    }
}
