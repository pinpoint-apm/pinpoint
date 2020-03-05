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

import com.navercorp.pinpoint.hbase.schema.definition.xml.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.definition.xml.Table;
import com.navercorp.pinpoint.hbase.schema.reader.InvalidHbaseSchemaException;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateTableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ModifyTableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableConfiguration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class TableChangeMapper {

    ColumnFamilyChangeMapper columnFamilyChangeMapper = new ColumnFamilyChangeMapper();
    TableConfigurationMapper tableConfigurationMapper = new TableConfigurationMapper();
    SplitOptionMapper splitOptionMapper = new SplitOptionMapper();

    public TableChange map(ChangeSet.ModifyTable modifyTable) {
        String tableName = modifyTable.getName();
        if (StringUtils.isEmpty(tableName)) {
            throw new InvalidHbaseSchemaException("Table name must not be empty");
        }

        List<ColumnFamilyChange> columnFamilyChanges = mapColumnFamilies(modifyTable.getCreateColumnFamily());

        return new ModifyTableChange(tableName, TableConfiguration.EMPTY_CONFIGURATION, columnFamilyChanges);
    }

    public TableChange map(ChangeSet.CreateTable createTable) {
        String tableName = createTable.getName();
        if (StringUtils.isEmpty(tableName)) {
            throw new InvalidHbaseSchemaException("Table name must not be empty");
        }

        TableConfiguration tableConfiguration = tableConfigurationMapper.mapConfiguration(createTable.getConfiguration());
        List<ColumnFamilyChange> columnFamilyChanges = mapColumnFamilies(createTable.getCreateColumnFamily());
        CreateTableChange.SplitOption splitOption = splitOptionMapper.mapSplitOption(createTable.getSplit());

        return new CreateTableChange(tableName, tableConfiguration, columnFamilyChanges, splitOption);
    }

    private List<ColumnFamilyChange> mapColumnFamilies(List<Table.CreateColumnFamily> createColumnFamilies) {
        if (CollectionUtils.isEmpty(createColumnFamilies)) {
            return Collections.emptyList();
        }
        Map<String, ColumnFamilyChange> createColumnFamilyChanges = new LinkedHashMap<>();
        for (Table.CreateColumnFamily createColumnFamily : createColumnFamilies) {
            String columnFamilyName = createColumnFamily.getName();
            ColumnFamilyChange columnFamilyChange = columnFamilyChangeMapper.mapCreate(createColumnFamily);
            if (createColumnFamilyChanges.put(columnFamilyName, columnFamilyChange) != null) {
                throw new InvalidHbaseSchemaException("Duplicate ColumnFamily name : " + columnFamilyName);
            }
        }
        return new ArrayList<>(createColumnFamilyChanges.values());
    }
}

