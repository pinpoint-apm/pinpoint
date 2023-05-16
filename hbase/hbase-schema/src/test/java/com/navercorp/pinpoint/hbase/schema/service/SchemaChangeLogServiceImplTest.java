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

package com.navercorp.pinpoint.hbase.schema.service;

import com.navercorp.pinpoint.hbase.schema.core.CheckSum;
import com.navercorp.pinpoint.hbase.schema.dao.SchemaChangeLogDao;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class SchemaChangeLogServiceImplTest {

    private final Random random = new Random();

    @Mock
    private SchemaChangeLogDao schemaChangeLogDao;

    private SchemaChangeLogService schemaChangeLogService;


    @BeforeEach
    public void beforeEach() {
        schemaChangeLogService = new SchemaChangeLogServiceImpl(schemaChangeLogDao);
    }


    @Test
    public void recordChangeSet() {
        // Given
        final ChangeSet changeSet = newChangeSet("id1");
        doNothing().when(schemaChangeLogDao).insertChangeLog(anyString(), any(SchemaChangeLog.class));
        // When
        SchemaChangeLog schemaChangeLog = schemaChangeLogService.recordChangeSet("namespace", changeSet);
        // Then
        assertThat(schemaChangeLog.getId()).isEqualTo(changeSet.getId());
        assertThat(schemaChangeLog.getExecOrder()).isEqualTo(1);
    }

    @Test
    public void recordChangeSet_withExecutionOrder() {
        // Given
        final ChangeSet changeSet = newChangeSet("id1");
        final int executionOrder = random.nextInt(100) + 1;
        doNothing().when(schemaChangeLogDao).insertChangeLog(anyString(), any(SchemaChangeLog.class));
        // When
        SchemaChangeLog schemaChangeLog = schemaChangeLogService.recordChangeSet("namespace", executionOrder, changeSet);
        // Then
        assertThat(schemaChangeLog.getId()).isEqualTo(changeSet.getId());
        assertThat(schemaChangeLog.getExecOrder()).isEqualTo(executionOrder);
    }

    @Test
    public void recordChangeSets() {
        // Given
        final int numChangeSets = random.nextInt(100) + 1;
        final List<ChangeSet> changeSets = new ArrayList<>(numChangeSets);
        for (int i = 0; i < numChangeSets; i++) {
            changeSets.add(newChangeSet("id" + (i + 1)));
        }
        doNothing().when(schemaChangeLogDao).insertChangeLog(anyString(), any(SchemaChangeLog.class));
        // When
        List<SchemaChangeLog> schemaChangeLogs = schemaChangeLogService.recordChangeSets("namespace", changeSets);
        // Then
        assertThat(schemaChangeLogs).hasSize(numChangeSets);
        final int initialExecOrder = 1;
        for (int i = 0; i < numChangeSets; i++) {
            SchemaChangeLog schemaChangeLog = schemaChangeLogs.get(i);
            ChangeSet changeSet = changeSets.get(i);
            assertThat(schemaChangeLog.getId()).isEqualTo(changeSet.getId());
            int expectedExecOrder = initialExecOrder + i;
            assertThat(schemaChangeLog.getExecOrder()).isEqualTo(expectedExecOrder);
        }
    }

    @Test
    public void recordChangeSets_withExecutionOrder() {
        // Given
        final int initialExecOrder = random.nextInt(10) + 1;
        final int numChangeSets = random.nextInt(100) + 1;
        final List<ChangeSet> changeSets = new ArrayList<>(numChangeSets);
        for (int i = 0; i < numChangeSets; i++) {
            changeSets.add(newChangeSet("id" + (i + 1)));
        }
        doNothing().when(schemaChangeLogDao).insertChangeLog(anyString(), any(SchemaChangeLog.class));
        // When
        List<SchemaChangeLog> schemaChangeLogs = schemaChangeLogService.recordChangeSets("namespace", initialExecOrder, changeSets);
        // Then
        assertThat(schemaChangeLogs).hasSize(numChangeSets);
        for (int i = 0; i < numChangeSets; i++) {
            SchemaChangeLog schemaChangeLog = schemaChangeLogs.get(i);
            ChangeSet changeSet = changeSets.get(i);
            assertThat(schemaChangeLog.getId()).isEqualTo(changeSet.getId());
            int expectedExecOrder = initialExecOrder + i;
            assertThat(schemaChangeLog.getExecOrder()).isEqualTo(expectedExecOrder);
        }
    }

    @Test
    public void getSchemaChangeLogs() {
        // Given
        final int numSchemaChangeLogs = random.nextInt(100) + 1;
        final List<SchemaChangeLog> schemaChangeLogs = new ArrayList<>(numSchemaChangeLogs);
        for (int i = 0; i < numSchemaChangeLogs; i++) {
            int order = i + 1;
            schemaChangeLogs.add(newSchemaChangeLog("id" + order, order));
        }
        when(schemaChangeLogDao.getChangeLogs(anyString())).thenReturn(schemaChangeLogs);
        // When
        List<SchemaChangeLog> actualSchemaChangeLogs = schemaChangeLogService.getSchemaChangeLogs("namespace");
        // Then
        for (int i = 0; i < actualSchemaChangeLogs.size(); i++) {
            final SchemaChangeLog actualSchemaChangeLog = actualSchemaChangeLogs.get(i);
            final int expectedOrder = i + 1;
            final String expectedId = "id" + expectedOrder;
            assertThat(actualSchemaChangeLog.getId()).isEqualTo(expectedId);
            assertThat(actualSchemaChangeLog.getExecOrder()).isEqualTo(expectedOrder);
        }
    }

    @Test
    public void getSchemaChangeLogs_shouldFailOnDuplicateOrder() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            // Given
            final int numSchemaChangeLogs = random.nextInt(100) + 1;
            final List<SchemaChangeLog> schemaChangeLogs = new ArrayList<>();
            int order = 1;
            for (int i = 0; i < numSchemaChangeLogs; i++) {
                schemaChangeLogs.add(newSchemaChangeLog("id" + order, order));
                order++;
            }
            // add schema change log with duplicate order
            final int duplicateOrder = random.nextInt(numSchemaChangeLogs) + 1;
            schemaChangeLogs.add(newSchemaChangeLog("duplicateOrderedId", duplicateOrder));
            when(schemaChangeLogDao.getChangeLogs(anyString())).thenReturn(schemaChangeLogs);
            // When
            schemaChangeLogService.getSchemaChangeLogs("namespace");
            // Then
            Assertions.fail();
        });
    }

    @Test
    public void getSchemaChangeLogs_shouldFailOnDuplicateId() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            // Given
            final int numSchemaChangeLogs = random.nextInt(100) + 1;
            final List<SchemaChangeLog> schemaChangeLogs = new ArrayList<>();
            int order = 1;
            for (int i = 0; i < numSchemaChangeLogs; i++) {
                schemaChangeLogs.add(newSchemaChangeLog("id" + order, order));
                order++;
            }
            // add duplicate element
            final String duplicateId = "id" + (random.nextInt(numSchemaChangeLogs) + 1);
            schemaChangeLogs.add(newSchemaChangeLog(duplicateId, order));
            when(schemaChangeLogDao.getChangeLogs(anyString())).thenReturn(schemaChangeLogs);
            // When
            schemaChangeLogService.getSchemaChangeLogs("namespace");
            // Then
            Assertions.fail();
        });
    }

    private static ChangeSet newChangeSet(String id) {
        return new ChangeSet(id, id, Collections.emptyList());
    }

    private static SchemaChangeLog newSchemaChangeLog(String id, int execOrder) {
        return new SchemaChangeLog.Builder()
                .id(id)
                .execTimestamp(System.currentTimeMillis())
                .execOrder(execOrder)
                .checkSum(CheckSum.compute(CheckSum.getCurrentVersion(), id))
                .value(id)
                .build();
    }
}
