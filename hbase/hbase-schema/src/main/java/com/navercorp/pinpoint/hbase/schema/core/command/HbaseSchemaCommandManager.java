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

package com.navercorp.pinpoint.hbase.schema.core.command;

import com.navercorp.pinpoint.hbase.schema.reader.InvalidHbaseSchemaException;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeType;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableChange;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
public class HbaseSchemaCommandManager {

    private final String namespace;
    private final Compression.Algorithm compressionAlgorithm;
    private final Set<TableName> affectedTables = new HashSet<>();
    private final Map<TableName, TableCommand> tableCommandMap = new LinkedHashMap<>();

    public HbaseSchemaCommandManager(String namespace, String compression) {
        this(namespace, compression, Collections.emptyList());
    }

    public HbaseSchemaCommandManager(String namespace, String compression, List<HTableDescriptor> currentHtds) {
        if (StringUtils.isEmpty(namespace)) {
            this.namespace = NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR;
        } else {
            this.namespace = namespace;
        }
        this.compressionAlgorithm = getCompressionAlgorithm(compression);
        for (HTableDescriptor htd : filterTablesByNamespace(currentHtds)) {
            tableCommandMap.put(htd.getTableName(), new ModifyTableCommand(htd, this.compressionAlgorithm));
        }
    }

    private Compression.Algorithm getCompressionAlgorithm(String compression) {
        if (StringUtils.isEmpty(compression)) {
            return Compression.Algorithm.NONE;
        }
        for (Compression.Algorithm compressionAlgorithm : Compression.Algorithm.values()) {
            if (compressionAlgorithm.getName().equalsIgnoreCase(compression)) {
                return compressionAlgorithm;
            }
        }
        throw new IllegalArgumentException("Unknown compression option : " + compression);
    }

    private List<HTableDescriptor> filterTablesByNamespace(List<HTableDescriptor> htds) {
        if (CollectionUtils.isEmpty(htds)) {
            return Collections.emptyList();
        }
        List<HTableDescriptor> filteredHtds = new ArrayList<>();
        for (HTableDescriptor htd : htds) {
            TableName tableName = htd.getTableName();
            String namespace = tableName.getNamespaceAsString();
            if (this.namespace.equalsIgnoreCase(namespace)) {
                filteredHtds.add(htd);
            }
        }
        return filteredHtds;
    }

    public String getNamespace() {
        return namespace;
    }

    public void applyChangeSet(ChangeSet changeSet) {
        if (changeSet == null) {
            throw new NullPointerException("changeSet");
        }
        List<TableChange> tableChanges = changeSet.getTableChanges();
        try {
            for (TableChange tableChange : tableChanges) {
                applyTableChange(tableChange);
            }
        } catch (Exception e) {
            throw new InvalidHbaseSchemaException("Error applying changeSet : " + changeSet.getId(), e);
        }
    }

    private void applyTableChange(TableChange tableChange) {
        ChangeType changeType = tableChange.getType();
        TableName tableName = TableName.valueOf(namespace, tableChange.getName());

        switch (changeType) {
            case CREATE:
                if (tableCommandMap.containsKey(tableName)) {
                    throw new IllegalArgumentException("Cannot create an existing table : " + tableName);
                }
                TableCommand createTableCommand = new CreateTableCommand(tableName, compressionAlgorithm, tableChange.getSplitKeys());
                createTableCommand.applyConfiguration(tableChange.getTableConfiguration());
                createTableCommand.applyColumnFamilyChanges(tableChange.getColumnFamilyChanges());
                tableCommandMap.put(tableName, createTableCommand);
                break;
            case MODIFY:
                TableCommand tableCommand = tableCommandMap.get(tableName);
                if (tableCommand == null) {
                    throw new IllegalArgumentException("Cannot modify a non-existent table : " + tableName);
                }
                tableCommand.applyConfiguration(tableChange.getTableConfiguration());
                tableCommand.applyColumnFamilyChanges(tableChange.getColumnFamilyChanges());
                break;
            default:
                throw new UnsupportedOperationException("Invalid change type : " + changeType);
        }
        affectedTables.add(tableName);
    }

    public List<TableCommand> getCommands() {
        return tableCommandMap.entrySet().stream()
                .filter(e -> affectedTables.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public List<HTableDescriptor> getSchemaSnapshot() {
        return tableCommandMap.entrySet().stream()
                .filter(e -> affectedTables.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .map(TableCommand::getHtd)
                .map(HTableDescriptor::new)
                .collect(Collectors.toList());
    }
}
