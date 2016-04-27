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

import static org.mockito.Mockito.*;

import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class HbaseAgentLifeCycleDaoTest {

    @Mock
    private HbaseOperations2 hbaseOperations2;

    @Mock
    private RowMapper<AgentLifeCycleBo> agentLifeCycleMapper;

    @InjectMocks
    private AgentLifeCycleDao agentLifeCycleDao = new HbaseAgentLifeCycleDao();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
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
        Assert.assertEquals(expectedAgentId, agentStatus.getAgentId());
        Assert.assertEquals(expectedTimestamp, agentStatus.getEventTimestamp());
        Assert.assertEquals(expectedAgentLifeCycleState, agentStatus.getState());
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
        Assert.assertEquals(expectedAgentId, agentStatus.getAgentId());
        Assert.assertEquals(expectedTimestamp, agentStatus.getEventTimestamp());
        Assert.assertEquals(expectedAgentLifeCycleState, agentStatus.getState());
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
        this.agentLifeCycleDao.populateAgentStatus(givenAgentInfo, expectedTimestamp);
        // Then
        AgentStatus actualAgentStatus = givenAgentInfo.getStatus();
        Assert.assertEquals(expectedAgentId, actualAgentStatus.getAgentId());
        Assert.assertEquals(expectedTimestamp, actualAgentStatus.getEventTimestamp());
        Assert.assertEquals(expectedAgentLifeCycleState, actualAgentStatus.getState());
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
        this.agentLifeCycleDao.populateAgentStatus(givenAgentInfo, expectedTimestamp);
        // Then
        AgentStatus actualAgentStatus = givenAgentInfo.getStatus();
        Assert.assertEquals(expectedAgentId, actualAgentStatus.getAgentId());
        Assert.assertEquals(expectedTimestamp, actualAgentStatus.getEventTimestamp());
        Assert.assertEquals(expectedAgentLifeCycleState, actualAgentStatus.getState());
    }

    @Test
    public void agentInfos_should_be_populated_accordingly_even_with_nulls() {
        // Given
        final String expectedAgentId = "test-agent";
        final long expectedTimestamp = 1000L;
        final AgentLifeCycleState expectedAgentLifeCycleState = AgentLifeCycleState.RUNNING;

        final AgentLifeCycleBo scannedLifeCycleBo = createAgentLifeCycleBo(expectedAgentId, expectedTimestamp, expectedAgentLifeCycleState);
        when(this.hbaseOperations2.findParallel(any(TableName.class), anyListOf(Scan.class), any(ResultsExtractor.class))).thenReturn(Arrays.asList(scannedLifeCycleBo, scannedLifeCycleBo));

        AgentInfo nonNullAgentInfo = new AgentInfo();
        nonNullAgentInfo.setAgentId(expectedAgentId);
        nonNullAgentInfo.setStartTimestamp(expectedTimestamp);
        AgentInfo nullAgentInfo = null;
        List<AgentInfo> givenAgentInfos = Arrays.asList(nonNullAgentInfo, nullAgentInfo, nonNullAgentInfo, nullAgentInfo);
        // When
        this.agentLifeCycleDao.populateAgentStatuses(givenAgentInfos, expectedTimestamp);
        // Then
        Assert.assertEquals(nonNullAgentInfo, givenAgentInfos.get(0));
        Assert.assertEquals(nullAgentInfo, givenAgentInfos.get(1));
        Assert.assertEquals(nonNullAgentInfo, givenAgentInfos.get(2));
        Assert.assertEquals(nullAgentInfo, givenAgentInfos.get(3));
        AgentStatus nonNullAgentInfoStatus = nonNullAgentInfo.getStatus();
        Assert.assertEquals(expectedAgentId, nonNullAgentInfoStatus.getAgentId());
        Assert.assertEquals(expectedTimestamp, nonNullAgentInfoStatus.getEventTimestamp());
        Assert.assertEquals(expectedAgentLifeCycleState, nonNullAgentInfoStatus.getState());
    }

    private AgentLifeCycleBo createAgentLifeCycleBo(String agentId, long eventTimestamp, AgentLifeCycleState state) {
        return new AgentLifeCycleBo(agentId, 0L, eventTimestamp, 0L, state);
    }
}
