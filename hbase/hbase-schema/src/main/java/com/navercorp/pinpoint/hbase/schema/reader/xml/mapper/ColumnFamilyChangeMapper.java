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

package com.navercorp.pinpoint.hbase.schema.reader.xml.mapper;

import com.navercorp.pinpoint.hbase.schema.definition.xml.Table;
import com.navercorp.pinpoint.hbase.schema.reader.InvalidHbaseSchemaException;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyConfiguration;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateColumnFamilyChange;
import org.springframework.util.StringUtils;

/**
 * @author HyunGil Jeong
 */
public class ColumnFamilyChangeMapper {

    private final ColumnFamilyConfigurationMapper columnFamilyConfigurationMapper = new ColumnFamilyConfigurationMapper();

    public ColumnFamilyChange mapCreate(Table.CreateColumnFamily createColumnFamily) {
        String name = createColumnFamily.getName();
        if (StringUtils.isEmpty(name)) {
            throw new InvalidHbaseSchemaException("ColumnFamily name must not be empty");
        }

        ColumnFamilyConfiguration columnFamilyConfiguration = columnFamilyConfigurationMapper.mapConfiguration(createColumnFamily.getConfiguration());

        return new CreateColumnFamilyChange(name, columnFamilyConfiguration);
    }
}
