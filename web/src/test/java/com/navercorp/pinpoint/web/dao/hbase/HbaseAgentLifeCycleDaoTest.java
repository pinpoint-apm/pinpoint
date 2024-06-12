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

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTableNameProvider;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class HbaseAgentLifeCycleDaoTest {

    @Mock
    private HbaseOperations hbaseOperations;

    @Spy
    private final TableNameProvider tableNameProvider = new HbaseTableNameProvider("default");

    @Mock
    private RowMapper<AgentLifeCycleBo> agentLifeCycleMapper;

    private AgentLifeCycleDao agentLifeCycleDao;

    @BeforeEach
    public void beforeEach() {
        this.agentLifeCycleDao = new HbaseAgentLifeCycleDao(hbaseOperations, tableNameProvider, agentLifeCycleMapper);
    }


    @Test
    public void status_should_be_set_appropriately_if_status_is_known() {
        // Given
        final AgentId expectedAgentId = AgentId.of("test-agent");
        final long expectedTimestamp = 1000L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.RUNNING;

        final AgentLifeCycleBo scannedLifeCycleBo = createAgentLifeCycleBo(AgentId.unwrap(expectedAgentId), expectedTimestamp, expectedAgentLifeCycleState);
        when(this.hbaseOperations.find(any(TableName.class), any(Scan.class), any(ResultsExtractor.class))).thenReturn(scannedLifeCycleBo);
        // When
        AgentStatus agentStatus = this.agentLifeCycleDao.getAgentStatus(AgentId.unwrap(expectedAgentId), expectedTimestamp);
        // Then
        assertStatus(agentStatus, expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
    }

    @Test
    public void status_should_be_unknown_if_status_cannot_be_found() {
        // Given
        final AgentId expectedAgentId = AgentId.of("test-agent");
        final long expectedTimestamp = 0L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.UNKNOWN;

        final AgentLifeCycleBo scannedLifeCycleBo = null;
        when(this.hbaseOperations.find(any(TableName.class), any(Scan.class), any(ResultsExtractor.class))).thenReturn(scannedLifeCycleBo);
        // When
        AgentStatus agentStatus = this.agentLifeCycleDao.getAgentStatus(AgentId.unwrap(expectedAgentId), expectedTimestamp);
        // Then
        assertStatus(agentStatus, expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
    }

    @Test
    public void agentInfo_should_be_populated_appropriately_if_status_is_known() {
        // Given
        final AgentId expectedAgentId = AgentId.of("test-agent");
        final long expectedTimestamp = 1000L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.RUNNING;

        final AgentLifeCycleBo scannedLifeCycleBo = createAgentLifeCycleBo(AgentId.unwrap(expectedAgentId), expectedTimestamp, expectedAgentLifeCycleState);
        when(this.hbaseOperations.find(any(TableName.class), any(Scan.class), any(ResultsExtractor.class))).thenReturn(scannedLifeCycleBo);
        // When
        AgentInfo givenAgentInfo = new AgentInfo();
        givenAgentInfo.setAgentId(expectedAgentId);
        givenAgentInfo.setStartTimestamp(expectedTimestamp);
        Optional<AgentStatus> agentStatus = this.agentLifeCycleDao.getAgentStatus(givenAgentInfo.getAgentId(), givenAgentInfo.getStartTimestamp(), expectedTimestamp);
        AgentStatus givenStatus = agentStatus.get();

        // Then
        assertStatus(givenStatus, expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
    }

    private void assertStatus(AgentStatus givenStatus, AgentId expectedAgentId, long expectedTimestamp, AgentLifeCycleState expectedAgentLifeCycleState) {
        assertThat(givenStatus)
                .extracting(AgentStatus::getAgentId, AgentStatus::getEventTimestamp, AgentStatus::getState)
                .containsExactly(expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
    }

    @Test
    public void agentInfo_should_be_populated_as_unknown_if_status_cannot_be_found() {
        // Given
        final AgentId expectedAgentId = AgentId.of("test-agent");
        final long expectedTimestamp = 0L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.UNKNOWN;

        final AgentLifeCycleBo scannedLifeCycleBo = null;
        when(this.hbaseOperations.find(any(TableName.class), any(Scan.class), any(ResultsExtractor.class))).thenReturn(scannedLifeCycleBo);

        AgentInfo givenAgentInfo = new AgentInfo();
        givenAgentInfo.setAgentId(expectedAgentId);
        givenAgentInfo.setStartTimestamp(expectedTimestamp);
        // When
        Optional<AgentStatus> agentStatus = this.agentLifeCycleDao.getAgentStatus(givenAgentInfo.getAgentId(), givenAgentInfo.getStartTimestamp(), expectedTimestamp);
        AgentStatus actualAgentStatus = agentStatus.get();

        // Then
        assertStatus(actualAgentStatus, expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
    }

    @Test
    public void agentInfos_should_be_populated_accordingly_even_with_nulls() {
        // Given
        final AgentId expectedAgentId = AgentId.of("test-agent");
        final long expectedTimestamp = 1000L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.RUNNING;

        final AgentLifeCycleBo scannedLifeCycleBo = createAgentLifeCycleBo(AgentId.unwrap(expectedAgentId), expectedTimestamp, expectedAgentLifeCycleState);
        when(this.hbaseOperations.findParallel(any(TableName.class), anyList(), any(ResultsExtractor.class))).thenReturn(List.of(scannedLifeCycleBo, scannedLifeCycleBo));

        AgentInfo nonNullAgentInfo = new AgentInfo();
        nonNullAgentInfo.setAgentId(expectedAgentId);
        nonNullAgentInfo.setStartTimestamp(expectedTimestamp);
        AgentInfo nullAgentInfo = null;
        List<AgentInfo> givenAgentInfos = Arrays.asList(nonNullAgentInfo, nullAgentInfo, nonNullAgentInfo, nullAgentInfo);
        // When
        AgentStatusQuery query = AgentStatusQuery.buildQuery(givenAgentInfos, Instant.ofEpochMilli(expectedTimestamp));
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);

        // Then
        assertThat(givenAgentInfos)
                .containsExactly(nonNullAgentInfo, nullAgentInfo, nonNullAgentInfo, nullAgentInfo);

        AgentStatus nonNullAgentInfoStatus = agentStatus.get(0).orElse(null);

        assertStatus(nonNullAgentInfoStatus, expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
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
