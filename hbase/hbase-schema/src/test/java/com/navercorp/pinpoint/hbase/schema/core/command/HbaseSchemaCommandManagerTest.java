/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import com.navercorp.pinpoint.hbase.schema.reader.InvalidHbaseSchemaException;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeType;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyConfiguration;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.CreateTableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ModifyTableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public class HbaseSchemaCommandManagerTest {

    @Test
    public void shouldFilterTablesFromDifferentNamespace() {
        String namespace = "namespace";
        String differentNamespace = "differentNamespace";
        String tableName = "table1";
        HTableDescriptor sameNamespaceHtd = createHtd(namespace, tableName, "CF1");
        HTableDescriptor differentNamespaceHtd = createHtd(differentNamespace, tableName, "CF1");
        List<HTableDescriptor> htds = Arrays.asList(sameNamespaceHtd, differentNamespaceHtd);
        HbaseSchemaCommandManager manager = new HbaseSchemaCommandManager(namespace, null, htds);

        ColumnFamilyChange createColumnFamilyChange = newColumnFamilyChange("CF2");
        TableChange modifyTableChange = newTableChange(ChangeType.MODIFY, tableName, createColumnFamilyChange);
        ChangeSet modifyTableChangeSet = newChangeSet(modifyTableChange);
        manager.applyChangeSet(modifyTableChangeSet);

        List<HTableDescriptor> schemaSnapshot = manager.getSchemaSnapshot();
        assertThat(schemaSnapshot, contains(sameNamespaceHtd));
        assertThat(schemaSnapshot, not(contains(differentNamespaceHtd)));
    }

    @Test(expected = InvalidHbaseSchemaException.class)
    public void creatingExistingTableShouldFail() {
        String namespace = "namespace";
        String tableName = "table";
        HTableDescriptor existingTable = createHtd(namespace, tableName, "CF");
        HbaseSchemaCommandManager manager = new HbaseSchemaCommandManager(namespace, null, Arrays.asList(existingTable));

        TableChange createTableChange = newTableChange(ChangeType.CREATE, tableName);
        ChangeSet createTableChangeSet = newChangeSet(createTableChange);

        manager.applyChangeSet(createTableChangeSet);
    }

    @Test(expected = InvalidHbaseSchemaException.class)
    public void modifyingNonExistingTableShouldFail() {
        String namespace = "namespace";
        String tableName = "table";
        String nonExistingTableName = "anotherTable";
        HTableDescriptor existingTable = createHtd(namespace, tableName, "CF");
        HbaseSchemaCommandManager manager = new HbaseSchemaCommandManager(namespace, null, Arrays.asList(existingTable));

        TableChange modifyTableChange = newTableChange(ChangeType.MODIFY, nonExistingTableName);
        ChangeSet modifyTableChangeSet = newChangeSet(modifyTableChange);

        manager.applyChangeSet(modifyTableChangeSet);
    }

    @Test
    public void failedChangesShouldNotAffectTheSchema() {
        String namespace = "namespace";
        String tableName = "table";
        String columnFamilyName = "CF";
        HbaseSchemaCommandManager manager = new HbaseSchemaCommandManager(namespace, null);
        // initial create table
        ColumnFamilyChange columnFamilyChange = newColumnFamilyChange(columnFamilyName);
        TableChange createTableChange = newTableChange(ChangeType.CREATE, tableName, columnFamilyChange);
        ChangeSet createTableChangeSet = newChangeSet(createTableChange);
        manager.applyChangeSet(createTableChangeSet);
        List<HTableDescriptor> initialSnapshot = manager.getSchemaSnapshot();

        // modify non-existing table
        TableChange modifyNonExistingTableChange = newTableChange(ChangeType.MODIFY, "nonExistingTable", newColumnFamilyChange("newCF"));
        ChangeSet modifyNonExistingTableChangeSet = newChangeSet(modifyNonExistingTableChange);
        try {
            manager.applyChangeSet(modifyNonExistingTableChangeSet);
            fail("Expected an InvalidHbaseSchemaException to be thrown");
        } catch (InvalidHbaseSchemaException expected) {
            List<HTableDescriptor> currentSnapshot = manager.getSchemaSnapshot();
            assertThat(currentSnapshot, equalTo(initialSnapshot));
        }

        // create existing table
        TableChange createExistingTableChange = newTableChange(ChangeType.CREATE, tableName);
        ChangeSet createExistingTableChangeSet = newChangeSet(createExistingTableChange);
        try {
            manager.applyChangeSet(createExistingTableChangeSet);
            fail("Expected an InvalidHbaseSchemaException to be thrown");
        } catch (InvalidHbaseSchemaException expected) {
            List<HTableDescriptor> currentSnapshot = manager.getSchemaSnapshot();
            assertThat(currentSnapshot, equalTo(initialSnapshot));
        }

        // create existing column family
        ColumnFamilyChange createExistingColumnFamilyChange = newColumnFamilyChange(columnFamilyName);
        TableChange createExistingColumnFamilyTableChange = newTableChange(ChangeType.MODIFY, tableName, createExistingColumnFamilyChange);
        ChangeSet createExistingColumnFamilyChangeSet = newChangeSet(createExistingColumnFamilyTableChange);
        try {
            manager.applyChangeSet(createExistingColumnFamilyChangeSet);
            fail("Expected an InvalidHbaseSchemaException to be thrown");
        } catch (InvalidHbaseSchemaException expected) {
            List<HTableDescriptor> currentSnapshot = manager.getSchemaSnapshot();
            assertThat(currentSnapshot, equalTo(initialSnapshot));
        }
    }

    @Test
    public void modifyingTheSameTableMultipleTimesShouldBeMerged() {
        String namespace = "namespace";
        String tableName = "table";
        String existingColumnFamily = "CF";
        String newColumnFamily1 = "CF1";
        String newColumnFamily2 = "CF2";

        HTableDescriptor existingHtd = createHtd(namespace, tableName, existingColumnFamily);
        HbaseSchemaCommandManager manager = new HbaseSchemaCommandManager(namespace, null, Arrays.asList(new HTableDescriptor(existingHtd)));

        ChangeSet createColumnFamilyChangeSet1 = newChangeSet(newTableChange(ChangeType.MODIFY, tableName, newColumnFamilyChange(newColumnFamily1)));
        ChangeSet createColumnFamilyChangeSet2 = newChangeSet(newTableChange(ChangeType.MODIFY, tableName, newColumnFamilyChange(newColumnFamily2)));
        manager.applyChangeSet(createColumnFamilyChangeSet1);
        manager.applyChangeSet(createColumnFamilyChangeSet2);

        // verify schema snapshot
        List<HTableDescriptor> schemaSnapshot = manager.getSchemaSnapshot();
        assertThat(schemaSnapshot.size(), is(1));
        HTableDescriptor snapshotTable = schemaSnapshot.get(0);
        assertThat(snapshotTable.getTableName(), is(TableName.valueOf(namespace, tableName)));
        List<String> snapshotColumnFamilies = snapshotTable.getFamilies().stream().map(HColumnDescriptor::getNameAsString).collect(Collectors.toList());
        assertThat(snapshotColumnFamilies, contains(existingColumnFamily, newColumnFamily1, newColumnFamily2));

        // verify command - should add 2 column families
        HbaseAdminOperation mockHbaseAdminOperation = Mockito.mock(HbaseAdminOperation.class);
        when(mockHbaseAdminOperation.getTableDescriptor(existingHtd.getTableName())).thenReturn(existingHtd);
        doNothing().when(mockHbaseAdminOperation).addColumn(any(TableName.class), any(HColumnDescriptor.class));
        doNothing().when(mockHbaseAdminOperation).createTable(any(HTableDescriptor.class));
        for (TableCommand tableCommand : manager.getCommands()) {
            tableCommand.execute(mockHbaseAdminOperation);
        }
        verify(mockHbaseAdminOperation, times(2)).addColumn(any(TableName.class), any(HColumnDescriptor.class));
        verify(mockHbaseAdminOperation, never()).createTable(any(HTableDescriptor.class));
    }

    @Test
    public void creatingAndModifyingTheSameTableShouldBeMerged() {
        String namespace = "namespace";
        String tableName = "table";
        String columnFamily1 = "CF1";
        String columnFamily2 = "CF2";
        String columnFamily3 = "CF3";

        HbaseSchemaCommandManager manager = new HbaseSchemaCommandManager(namespace, null);

        ChangeSet createTableChangeSet = newChangeSet(newTableChange(ChangeType.CREATE, tableName, newColumnFamilyChange(columnFamily1)));
        ChangeSet addColumnFamilyChangeSet1 = newChangeSet(newTableChange(ChangeType.MODIFY, tableName, newColumnFamilyChange(columnFamily2)));
        ChangeSet addColumnFamilyChangeSet2 = newChangeSet(newTableChange(ChangeType.MODIFY, tableName, newColumnFamilyChange(columnFamily3)));

        manager.applyChangeSet(createTableChangeSet);
        manager.applyChangeSet(addColumnFamilyChangeSet1);
        manager.applyChangeSet(addColumnFamilyChangeSet2);

        // verify schema snapshot
        List<HTableDescriptor> schemaSnapshot = manager.getSchemaSnapshot();
        assertThat(schemaSnapshot.size(), is(1));
        HTableDescriptor snapshotTable = schemaSnapshot.get(0);
        assertThat(snapshotTable.getTableName(), is(TableName.valueOf(namespace, tableName)));
        List<String> snapshotColumnFamilies = snapshotTable.getFamilies().stream().map(HColumnDescriptor::getNameAsString).collect(Collectors.toList());
        assertThat(snapshotColumnFamilies, contains(columnFamily1, columnFamily2, columnFamily3));

        // verify command - should create 1 table (with all 3 column families)
        HbaseAdminOperation mockHbaseAdminOperation = Mockito.mock(HbaseAdminOperation.class);
        when(mockHbaseAdminOperation.tableExists(TableName.valueOf(namespace, tableName))).thenReturn(false);
        doNothing().when(mockHbaseAdminOperation).createTable(any(HTableDescriptor.class));
        for (TableCommand tableCommand : manager.getCommands()) {
            tableCommand.execute(mockHbaseAdminOperation);
        }
        verify(mockHbaseAdminOperation, times(1)).createTable(any(HTableDescriptor.class));
    }

    private ColumnFamilyChange newColumnFamilyChange(String cfName) {
        return new CreateColumnFamilyChange(cfName, ColumnFamilyConfiguration.EMPTY_CONFIGURATION);
    }

    private TableChange newTableChange(ChangeType changeType, String tableName, ColumnFamilyChange... cfChanges) {
        List<ColumnFamilyChange> columnFamilyChanges = Arrays.asList(cfChanges);
        switch (changeType) {
            case CREATE:
                return new CreateTableChange(tableName, TableConfiguration.EMPTY_CONFIGURATION, columnFamilyChanges, CreateTableChange.SplitOption.NONE);
            case MODIFY:
                return new ModifyTableChange(tableName, TableConfiguration.EMPTY_CONFIGURATION, columnFamilyChanges);
            default:
                throw new IllegalArgumentException("changeType : " + changeType + " not supported");
        }
    }

    private ChangeSet newChangeSet(TableChange... tableChanges) {
        return new ChangeSet("id", "value", Arrays.asList(tableChanges));
    }

    private HTableDescriptor createHtd(String namespace, String tableQualifier, String... columnFamilyNames) {
        TableName tableName = TableName.valueOf(namespace, tableQualifier);
        HTableDescriptor htd = new HTableDescriptor(tableName);
        for (String columnFamilyName : columnFamilyNames) {
            htd.addFamily(new HColumnDescriptor(columnFamilyName));
        }
        return htd;
    }
}
