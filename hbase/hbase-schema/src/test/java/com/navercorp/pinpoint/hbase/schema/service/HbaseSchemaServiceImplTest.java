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

import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import com.navercorp.pinpoint.hbase.schema.core.CheckSum;
import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaStatus;
import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaVerifier;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableChange;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class HbaseSchemaServiceImplTest {

    @Mock
    private HbaseAdminOperation hbaseAdminOperation;

    @Mock
    private SchemaChangeLogService schemaChangeLogService;

    @Mock
    private HbaseSchemaVerifier<TableDescriptor> hbaseSchemaVerifier;

    private HbaseSchemaService hbaseSchemaService;


    @BeforeEach
    public void beforeEach() {
        hbaseSchemaService = new HbaseSchemaServiceImpl(hbaseAdminOperation, schemaChangeLogService, hbaseSchemaVerifier);
    }

    @Test
    public void init_shouldReturnTrue_afterInitialization() {
        final String namespace = "namespace";
        when(schemaChangeLogService.isAvailable(namespace)).thenReturn(false);

        boolean initialized = hbaseSchemaService.init(namespace);
        assertThat(initialized).isTrue();
        verify(schemaChangeLogService, atLeastOnce()).init(anyString());
    }

    @Test
    public void init_shouldReturnFalse_ifSchemaChangeLogServiceIsAlreadyAvailable() {
        final String namespace = "namespace";
        when(schemaChangeLogService.isAvailable(namespace)).thenReturn(true);

        boolean initialized = hbaseSchemaService.init(namespace);
        assertThat(initialized).isFalse();
        verify(schemaChangeLogService, never()).init(anyString());
    }

    @Test
    public void getSchemaStatus_noTables() {
        final String namespace = "namespace";
        final List<ChangeSet> changeSets = List.of(newChangeSet("id1", "value1"));
        when(schemaChangeLogService.isAvailable(namespace)).thenReturn(false);

        HbaseSchemaStatus schemaStatus = hbaseSchemaService.getSchemaStatus(namespace, changeSets);
        assertThat(schemaStatus).isEqualTo(HbaseSchemaStatus.NONE);
    }

    @Test
    public void getSchemaStatus_noSchemaChangeLogs() {
        final String namespace = "namespace";
        final List<ChangeSet> changeSets = List.of(newChangeSet("id1", "value1"));
        when(schemaChangeLogService.isAvailable(namespace)).thenReturn(true);
        when(schemaChangeLogService.getSchemaChangeLogs(namespace)).thenReturn(Collections.emptyList());

        HbaseSchemaStatus schemaStatus = hbaseSchemaService.getSchemaStatus(namespace, changeSets);
        assertThat(schemaStatus).isEqualTo(HbaseSchemaStatus.NONE);
    }

    @Test
    public void getSchemaStatus_invalidSchemaChangeLogs() {
        final String namespace = "namespace";
        final List<ChangeSet> changeSets = List.of(
                newChangeSet("id1", "value1"),
                newChangeSet("id2", "value2"),
                newChangeSet("id3", "value3"));
        final List<SchemaChangeLog> schemaChangeLogs = List.of(
                newSchemaChangeLog("id1", "value2", 2),
                newSchemaChangeLog("id2", "value1", 1),
                newSchemaChangeLog("someOtherId", "value3", 3));
        when(schemaChangeLogService.isAvailable(namespace)).thenReturn(true);
        when(schemaChangeLogService.getSchemaChangeLogs(namespace)).thenReturn(schemaChangeLogs);

        HbaseSchemaStatus schemaStatus = hbaseSchemaService.getSchemaStatus(namespace, changeSets);
        assertThat(schemaStatus).isEqualTo(HbaseSchemaStatus.INVALID);
    }

    @Test
    public void getSchemaStatus_validSchemaChangeLogs() {
        final String namespace = "namespace";
        final List<ChangeSet> changeSets = List.of(
                newChangeSet("id1", "value1"),
                newChangeSet("id2", "value2"),
                newChangeSet("id3", "value3"));
        final List<SchemaChangeLog> schemaChangeLogs = List.of(
                newSchemaChangeLog("id1", "value1", 1),
                newSchemaChangeLog("id2", "value2", 2),
                newSchemaChangeLog("id3", "value3", 3));
        when(schemaChangeLogService.isAvailable(namespace)).thenReturn(true);
        when(schemaChangeLogService.getSchemaChangeLogs(namespace)).thenReturn(schemaChangeLogs);

        HbaseSchemaStatus schemaStatus = hbaseSchemaService.getSchemaStatus(namespace, changeSets);
        assertThat(schemaStatus).isEqualTo(HbaseSchemaStatus.VALID);
    }

    @Test
    public void getSchemaStatus_validButNeedUpdateSchemaChangeLogs() {
        final String namespace = "namespace";
        final List<ChangeSet> changeSets = List.of(
                newChangeSet("id1", "value1"),
                newChangeSet("id2", "value2"),
                newChangeSet("id3", "value3"));
        final List<SchemaChangeLog> schemaChangeLogs = List.of(
                newSchemaChangeLog("id1", "value1", 1),
                newSchemaChangeLog("id2", "value2", 2));
        when(schemaChangeLogService.isAvailable(namespace)).thenReturn(true);
        when(schemaChangeLogService.getSchemaChangeLogs(namespace)).thenReturn(schemaChangeLogs);

        HbaseSchemaStatus schemaStatus = hbaseSchemaService.getSchemaStatus(namespace, changeSets);
        assertThat(schemaStatus).isEqualTo(HbaseSchemaStatus.VALID_OUT_OF_DATE);
    }

    private ChangeSet newChangeSet(String id, String value) {
        return new ChangeSet(id, value, Collections.emptyList());
    }

    private ChangeSet newChangeSet(String id, String value, TableChange... tableChanges) {
        return new ChangeSet(id, value, List.of(tableChanges));
    }

    private SchemaChangeLog newSchemaChangeLog(String id, String value, int execOrder) {
        return new SchemaChangeLog.Builder()
                .id(id)
                .value(value)
                .checkSum(CheckSum.compute(CheckSum.getCurrentVersion(), value))
                .execOrder(execOrder)
                .execTimestamp(System.currentTimeMillis())
                .build();
    }
}
