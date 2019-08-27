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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public class SchemaChangeLogServiceImplTest {

    private final Random random = new Random();

    @Mock
    private SchemaChangeLogDao schemaChangeLogDao;

    private SchemaChangeLogService schemaChangeLogService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
        Assert.assertThat(schemaChangeLog.getId(), equalTo(changeSet.getId()));
        Assert.assertThat(schemaChangeLog.getExecOrder(), equalTo(1));
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
        Assert.assertThat(schemaChangeLog.getId(), equalTo(changeSet.getId()));
        Assert.assertThat(schemaChangeLog.getExecOrder(), equalTo(executionOrder));
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
        Assert.assertThat(schemaChangeLogs.size(), equalTo(numChangeSets));
        final int initialExecOrder = 1;
        for (int i = 0; i < numChangeSets; i++) {
            SchemaChangeLog schemaChangeLog = schemaChangeLogs.get(i);
            ChangeSet changeSet = changeSets.get(i);
            Assert.assertThat(schemaChangeLog.getId(), equalTo(changeSet.getId()));
            int expectedExecOrder = initialExecOrder + i;
            Assert.assertThat(schemaChangeLog.getExecOrder(), equalTo(expectedExecOrder));
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
        Assert.assertThat(schemaChangeLogs.size(), equalTo(numChangeSets));
        for (int i = 0; i < numChangeSets; i++) {
            SchemaChangeLog schemaChangeLog = schemaChangeLogs.get(i);
            ChangeSet changeSet = changeSets.get(i);
            Assert.assertThat(schemaChangeLog.getId(), equalTo(changeSet.getId()));
            int expectedExecOrder = initialExecOrder + i;
            Assert.assertThat(schemaChangeLog.getExecOrder(), equalTo(expectedExecOrder));
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
            Assert.assertThat(actualSchemaChangeLog.getId(), equalTo(expectedId));
            Assert.assertThat(actualSchemaChangeLog.getExecOrder(), equalTo(expectedOrder));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void getSchemaChangeLogs_shouldFailOnDuplicateOrder() {
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
        Assert.fail();
    }

    @Test(expected = IllegalStateException.class)
    public void getSchemaChangeLogs_shouldFailOnDuplicateId() {
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
        Assert.fail();
    }

    private static ChangeSet newChangeSet(String id) {
        return new ChangeSet(id, id, Collections.emptyList());
    }

    private static SchemaChangeLog newSchemaChangeLog(String id, int execOrder) {
        String value = id;
        return new SchemaChangeLog.Builder()
                .id(id)
                .execTimestamp(System.currentTimeMillis())
                .execOrder(execOrder)
                .checkSum(CheckSum.compute(CheckSum.getCurrentVersion(), value))
                .value(value)
                .build();
    }
}
