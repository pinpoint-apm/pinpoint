/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class HbaseAgentLifeCycleDaoTest {

    @Mock
    private HbaseOperations2 hbaseOperations2;

    @Spy
    private final TableNameProvider tableNameProvider = new TableNameProvider() {

        @Override
        public TableName getTableName(HbaseTable hBaseTable) {
            return getTableName(hBaseTable.getName());
        }

        @Override
        public TableName getTableName(String tableName) {
            return TableName.valueOf(tableName);
        }

        @Override
        public boolean hasDefaultNameSpace() {
            return true;
        }
    };

    @Mock
    private RowMapper<AgentLifeCycleBo> agentLifeCycleMapper;

    private AgentLifeCycleDao agentLifeCycleDao;

    @BeforeEach
    public void beforeEach() {
        this.agentLifeCycleDao = new HbaseAgentLifeCycleDao(hbaseOperations2, tableNameProvider, agentLifeCycleMapper);
    }


    @Test
    public void status_should_be_set_appropriately_if_status_is_known() {
        // Given
        final String expectedAgentId = "test-agent";
        final long expectedTimestamp = 1000L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.RUNNING;

        final AgentLifeCycleBo scannedLifeCycleBo = createAgentLifeCycleBo(expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
        when(this.hbaseOperations2.find(any(TableName.class), any(Scan.class), any(ResultsExtractor.class))).thenReturn(scannedLifeCycleBo);
        // When
        AgentStatus agentStatus = this.agentLifeCycleDao.getAgentStatus(expectedAgentId, expectedTimestamp);
        // Then
        Assertions.assertEquals(expectedAgentId, agentStatus.getAgentId());
        Assertions.assertEquals(expectedTimestamp, agentStatus.getEventTimestamp());
        Assertions.assertEquals(expectedAgentLifeCycleState, agentStatus.getState());
    }

    @Test
    public void status_should_be_unknown_if_status_cannot_be_found() {
        // Given
        final String expectedAgentId = "test-agent";
        final long expectedTimestamp = 0L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.UNKNOWN;

        final AgentLifeCycleBo scannedLifeCycleBo = null;
        when(this.hbaseOperations2.find(any(TableName.class), any(Scan.class), any(ResultsExtractor.class))).thenReturn(scannedLifeCycleBo);
        // When
        AgentStatus agentStatus = this.agentLifeCycleDao.getAgentStatus(expectedAgentId, expectedTimestamp);
        // Then
        Assertions.assertEquals(expectedAgentId, agentStatus.getAgentId());
        Assertions.assertEquals(expectedTimestamp, agentStatus.getEventTimestamp());
        Assertions.assertEquals(expectedAgentLifeCycleState, agentStatus.getState());
    }

    @Test
    public void agentInfo_should_be_populated_appropriately_if_status_is_known() {
        // Given
        final String expectedAgentId = "test-agent";
        final long expectedTimestamp = 1000L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.RUNNING;

        final AgentLifeCycleBo scannedLifeCycleBo = createAgentLifeCycleBo(expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
        when(this.hbaseOperations2.find(any(TableName.class), any(Scan.class), any(ResultsExtractor.class))).thenReturn(scannedLifeCycleBo);
        // When
        AgentInfo givenAgentInfo = new AgentInfo();
        givenAgentInfo.setAgentId(expectedAgentId);
        givenAgentInfo.setStartTimestamp(expectedTimestamp);
        Optional<AgentStatus> agentStatus = this.agentLifeCycleDao.getAgentStatus(givenAgentInfo.getAgentId(), givenAgentInfo.getStartTimestamp(), expectedTimestamp);
        AgentStatus givenStatus = agentStatus.get();

        // Then
        Assertions.assertEquals(expectedAgentId, givenStatus.getAgentId());
        Assertions.assertEquals(expectedTimestamp, givenStatus.getEventTimestamp());
        Assertions.assertEquals(expectedAgentLifeCycleState, givenStatus.getState());
    }

    @Test
    public void agentInfo_should_be_populated_as_unknown_if_status_cannot_be_found() {
        // Given
        final String expectedAgentId = "test-agent";
        final long expectedTimestamp = 0L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.UNKNOWN;

        final AgentLifeCycleBo scannedLifeCycleBo = null;
        when(this.hbaseOperations2.find(any(TableName.class), any(Scan.class), any(ResultsExtractor.class))).thenReturn(scannedLifeCycleBo);

        AgentInfo givenAgentInfo = new AgentInfo();
        givenAgentInfo.setAgentId(expectedAgentId);
        givenAgentInfo.setStartTimestamp(expectedTimestamp);
        // When
        Optional<AgentStatus> agentStatus = this.agentLifeCycleDao.getAgentStatus(givenAgentInfo.getAgentId(), givenAgentInfo.getStartTimestamp(), expectedTimestamp);
        AgentStatus actualAgentStatus = agentStatus.get();

        // Then
        Assertions.assertEquals(expectedAgentId, actualAgentStatus.getAgentId());
        Assertions.assertEquals(expectedTimestamp, actualAgentStatus.getEventTimestamp());
        Assertions.assertEquals(expectedAgentLifeCycleState, actualAgentStatus.getState());
    }

    @Test
    public void agentInfos_should_be_populated_accordingly_even_with_nulls() {
        // Given
        final String expectedAgentId = "test-agent";
        final Instant expectedTimestamp = Instant.ofEpochMilli(1000);
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.RUNNING;

        final AgentLifeCycleBo scannedLifeCycleBo = createAgentLifeCycleBo(expectedAgentId, expectedTimestamp.toEpochMilli(), expectedAgentLifeCycleState);
        when(this.hbaseOperations2.findParallel(any(TableName.class), anyList(), any(ResultsExtractor.class))).thenReturn(Arrays.asList(scannedLifeCycleBo, scannedLifeCycleBo));

        AgentInfo nonNullAgentInfo = new AgentInfo();
        nonNullAgentInfo.setAgentId(expectedAgentId);
        nonNullAgentInfo.setStartTimestamp(expectedTimestamp.toEpochMilli());
        AgentInfo nullAgentInfo = null;
        List<AgentInfo> givenAgentInfos = Arrays.asList(nonNullAgentInfo, nullAgentInfo, nonNullAgentInfo, nullAgentInfo);
        // When
        AgentStatusQuery query = AgentStatusQuery.buildQuery(givenAgentInfos, expectedTimestamp);
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);

        // Then
        Assertions.assertEquals(nonNullAgentInfo, givenAgentInfos.get(0));
        Assertions.assertEquals(nullAgentInfo, givenAgentInfos.get(1));
        Assertions.assertEquals(nonNullAgentInfo, givenAgentInfos.get(2));
        Assertions.assertEquals(nullAgentInfo, givenAgentInfos.get(3));
        AgentStatus nonNullAgentInfoStatus = agentStatus.get(0).orElse(null);
        Assertions.assertEquals(expectedAgentId, nonNullAgentInfoStatus.getAgentId());
        Assertions.assertEquals(expectedTimestamp, Instant.ofEpochMilli(nonNullAgentInfoStatus.getEventTimestamp()));
        Assertions.assertEquals(expectedAgentLifeCycleState, nonNullAgentInfoStatus.getState());
    }

    @Test
    public void populateAgentStatus_should_not_crash_with_invalid_inputs() {
        this.agentLifeCycleDao.getAgentStatus(null, 1000, 1000L);
        AgentStatusQuery.Builder builder = AgentStatusQuery.newBuilder();
        AgentStatusQuery query = builder.build(Instant.ofEpochMilli(1000));
        this.agentLifeCycleDao.getAgentStatus(query);
    }

    private AgentLifeCycleBo createAgentLifeCycleBo(String agentId, long eventTimestamp, AgentLifeCycleState state) {
        return new AgentLifeCycleBo(agentId, 0L, eventTimestamp, 0L, state);
    }
}
