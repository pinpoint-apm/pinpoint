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

package com.navercorp.pinpoint.hbase.schema.core;

import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author HyunGil Jeong
 */
public class ChangeSetManagerTest {

    @Test
    public void getExecutedChangeSets() {
        ChangeSet changeSet1 = newChangeSet("id1", "value1");
        ChangeSet changeSet2 = newChangeSet("id2", "value2");
        ChangeSet changeSet3 = newChangeSet("id3", "value3");
        ChangeSet changeSet4 = newChangeSet("id4", "value4");
        List<ChangeSet> changeSets = List.of(changeSet1, changeSet2, changeSet3, changeSet4);
        List<SchemaChangeLog> schemaChangeLogs = newSchemaChangeLogs(changeSet1, changeSet2);

        ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
        List<ChangeSet> executedChangeSets = changeSetManager.getExecutedChangeSets(schemaChangeLogs);
        assertThat(executedChangeSets).contains(changeSet1, changeSet2);
    }

    @Test
    public void getExecutedChangeSets_emptySchemaChangeLogs() {
        ChangeSetManager changeSetManager = new ChangeSetManager(List.of(newChangeSet("id1", "value1")));
        assertThat(changeSetManager.getExecutedChangeSets(Collections.emptyList())).isEmpty();
        assertThat(changeSetManager.getExecutedChangeSets(null)).isEmpty();
    }

    @Test
    public void getExecutedChangeSets_largerSchemaChangeLogs() {
        ChangeSet changeSet = newChangeSet("id1", "value1");
        ChangeSetManager changeSetManager = new ChangeSetManager(List.of(changeSet));
        List<SchemaChangeLog> schemaChangeLogs = List.of(
                newSchemaChangeLog("id1", "value1", 1),
                newSchemaChangeLog("id2", "value2", 2));
        List<ChangeSet> executedChangeSets = changeSetManager.getExecutedChangeSets(schemaChangeLogs);
        assertThat(executedChangeSets).hasSize(1).contains(changeSet);
    }

    @Test
    public void getExecutedChangeSets_invalidId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ChangeSet changeSet = newChangeSet("id1", "value1");
            List<ChangeSet> changeSets = List.of(changeSet);
            List<SchemaChangeLog> schemaChangeLogs = List.of(newSchemaChangeLog("id2", "value1", 1));

            ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
            changeSetManager.getExecutedChangeSets(schemaChangeLogs);
        });
    }

    @Test
    public void getExecutedChangeSets_invalidCheckSum() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ChangeSet changeSet = newChangeSet("id1", "value1");
            List<ChangeSet> changeSets = List.of(changeSet);
            CheckSum invalidCheckSum = CheckSum.compute(CheckSum.getCurrentVersion(), "value2");
            List<SchemaChangeLog> schemaChangeLogs = List.of(newSchemaChangeLog("id1", "value1", invalidCheckSum, 1));

            ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
            changeSetManager.getExecutedChangeSets(schemaChangeLogs);
        });
    }

    @Test
    public void getExecutedChangeSets_invalidOrder() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ChangeSet changeSet1 = newChangeSet("id1", "value1");
            List<ChangeSet> changeSets = List.of(changeSet1);
            List<SchemaChangeLog> schemaChangeLogs = List.of(newSchemaChangeLog("id1", "value1", 2));

            ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
            changeSetManager.getExecutedChangeSets(schemaChangeLogs);
        });
    }

    @Test
    public void filterExecutedChangeSets() {
        ChangeSet changeSet1 = newChangeSet("id1", "value1");
        ChangeSet changeSet2 = newChangeSet("id2", "value2");
        ChangeSet changeSet3 = newChangeSet("id3", "value3");
        ChangeSet changeSet4 = newChangeSet("id4", "value4");
        List<ChangeSet> changeSets = List.of(changeSet1, changeSet2, changeSet3, changeSet4);
        List<SchemaChangeLog> schemaChangeLogs = newSchemaChangeLogs(changeSet1);

        ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
        List<ChangeSet> filteredChangeSets = changeSetManager.filterExecutedChangeSets(schemaChangeLogs);
        assertThat(filteredChangeSets).contains(changeSet2, changeSet3, changeSet4);
    }

    @Test
    public void filterExecutedChangeSets_emptySchemaChangeLogs() {
        ChangeSet changeSet1 = newChangeSet("id1", "value1");
        ChangeSet changeSet2 = newChangeSet("id2", "value2");
        List<ChangeSet> changeSets = List.of(changeSet1, changeSet2);
        ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
        assertThat(changeSetManager.filterExecutedChangeSets(Collections.emptyList())).contains(changeSet1, changeSet2);
        assertThat(changeSetManager.filterExecutedChangeSets(null)).contains(changeSet1, changeSet2);
    }

    @Test
    public void filterExecutedChangeSets_largerSchemaChangeLogs() {
        ChangeSet changeSet = newChangeSet("id1", "value1");
        ChangeSetManager changeSetManager = new ChangeSetManager(List.of(changeSet));
        List<SchemaChangeLog> schemaChangeLogs = List.of(
                newSchemaChangeLog("id1", "value1", 1),
                newSchemaChangeLog("id2", "value2", 2));
        List<ChangeSet> unexecutedChangeSets = changeSetManager.filterExecutedChangeSets(schemaChangeLogs);
        assertThat(unexecutedChangeSets).isEmpty();
    }

    @Test
    public void filterExecutedChangeSets_invalidId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ChangeSet changeSet = newChangeSet("id1", "value1");
            List<ChangeSet> changeSets = List.of(changeSet);
            List<SchemaChangeLog> schemaChangeLogs = List.of(newSchemaChangeLog("id2", "value1", 1));

            ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
            changeSetManager.filterExecutedChangeSets(schemaChangeLogs);
        });
    }

    @Test
    public void filterExecutedChangeSets_invalidCheckSum() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ChangeSet changeSet = newChangeSet("id1", "value1");
            List<ChangeSet> changeSets = List.of(changeSet);
            CheckSum invalidCheckSum = CheckSum.compute(CheckSum.getCurrentVersion(), "value2");
            List<SchemaChangeLog> schemaChangeLogs = List.of(newSchemaChangeLog("id1", "value1", invalidCheckSum, 1));

            ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
            changeSetManager.filterExecutedChangeSets(schemaChangeLogs);
        });
    }

    @Test
    public void filterExecutedChangeSets_invalidOrder() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ChangeSet changeSet1 = newChangeSet("id1", "value1");
            List<ChangeSet> changeSets = List.of(changeSet1);
            List<SchemaChangeLog> schemaChangeLogs = List.of(newSchemaChangeLog("id1", "value1", 2));

            ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
            changeSetManager.filterExecutedChangeSets(schemaChangeLogs);
        });
    }

    private ChangeSet newChangeSet(String id, String value) {
        return new ChangeSet(id, value, Collections.emptyList());
    }

    private SchemaChangeLog newSchemaChangeLog(String id, String value, int execOrder) {
        return newSchemaChangeLog(id, value, CheckSum.compute(CheckSum.getCurrentVersion(), value), execOrder);
    }

    private SchemaChangeLog newSchemaChangeLog(String id, String value, CheckSum checkSum, int execOrder) {
        return new SchemaChangeLog.Builder()
                .id(id)
                .value(value)
                .checkSum(checkSum)
                .execOrder(execOrder)
                .build();
    }

    private List<SchemaChangeLog> newSchemaChangeLogs(ChangeSet... changeSets) {
        List<SchemaChangeLog> schemaChangeLogs = new ArrayList<>();
        for (int i = 0; i < changeSets.length; i++) {
            final ChangeSet changeSet = changeSets[i];
            schemaChangeLogs.add(new SchemaChangeLog.Builder()
                    .id(changeSet.getId())
                    .value(changeSet.getValue())
                    .checkSum(CheckSum.compute(CheckSum.getCurrentVersion(), changeSet.getValue()))
                    .execOrder(i + 1)
                    .build());
        }
        return schemaChangeLogs;
    }
}
