/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.dao.AgentStatDaoV2;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.collector.service.HBaseAgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TResponseTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author HyunGil Jeong
 */
public class AgentStatHandlerV2Test {

    @Mock
    private AgentStatMapper agentStatMapper;

    @Mock
    private AgentStatBatchMapper agentStatBatchMapper;

    @Mock
    private AgentStatDaoV2<JvmGcBo> jvmGcDao;

    @Mock
    private AgentStatDaoV2<JvmGcDetailedBo> jvmGcDetailedDao;

    @Mock
    private AgentStatDaoV2<CpuLoadBo> cpuLoadDao;

    @Mock
    private AgentStatDaoV2<TransactionBo> transactionDao;

    @Mock
    private AgentStatDaoV2<ActiveTraceBo> activeTraceDao;

    @Mock
    private AgentStatDaoV2<DataSourceListBo> dataSourceDao;

    @Mock
    private AgentStatDaoV2<ResponseTimeBo> responseTimeDao;

    @Mock
    private AgentStatDaoV2<DeadlockBo> deadlockDao;

    @InjectMocks
    private HBaseAgentStatService hBaseAgentStatService = new HBaseAgentStatService();

    @Spy
    private List<AgentStatService> agentStatServiceList = new ArrayList<>();

    @InjectMocks
    private AgentStatHandlerV2 agentStatHandler = new AgentStatHandlerV2();

    @Before
    public void setUp() throws Exception {
        agentStatServiceList.add(hBaseAgentStatService);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleForTAgentStat() {
        // Given
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp);
        final AgentStatBo mappedAgentStat = new AgentStatBo();
        when(this.agentStatMapper.map(agentStat)).thenReturn(mappedAgentStat);
        // When
        agentStatHandler.handleSimple(agentStat);
        // Then
        verify(jvmGcDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getJvmGcBos());
        verify(jvmGcDetailedDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getJvmGcDetailedBos());
        verify(cpuLoadDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getCpuLoadBos());
        verify(transactionDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getTransactionBos());
        verify(activeTraceDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getActiveTraceBos());
        verify(dataSourceDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getDataSourceListBos());
        verify(responseTimeDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getResponseTimeBos());
        verify(deadlockDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getDeadlockBos());

    }

    @Test
    public void testHandleForTAgentStatBatch() {
        // Given
        final int numBatches = 6;
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStatBatch agentStatBatch = createAgentStatBatch(agentId, startTimestamp, numBatches);
        final AgentStatBo mappedAgentStat = new AgentStatBo();
        when(this.agentStatBatchMapper.map(agentStatBatch)).thenReturn(mappedAgentStat);
        // When
        agentStatHandler.handleSimple(agentStatBatch);
        // Then
        verify(jvmGcDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getJvmGcBos());
        verify(jvmGcDetailedDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getJvmGcDetailedBos());
        verify(cpuLoadDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getCpuLoadBos());
        verify(transactionDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getTransactionBos());
        verify(activeTraceDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getActiveTraceBos());
        verify(dataSourceDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getDataSourceListBos());
        verify(responseTimeDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getResponseTimeBos());
        verify(deadlockDao).insert(mappedAgentStat.getAgentId(), mappedAgentStat.getDeadlockBos());
    }

    @Test
    public void insertShouldNotBeCalledIfTAgentStatIsMappedToNull() {
        // Given
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp);
        final AgentStatBo mappedAgentStat = null;
        when(this.agentStatMapper.map(agentStat)).thenReturn(mappedAgentStat);
        // When
        agentStatHandler.handleSimple(agentStat);
        // Then
        verifyZeroInteractions(jvmGcDao);
        verifyZeroInteractions(jvmGcDetailedDao);
        verifyZeroInteractions(cpuLoadDao);
        verifyZeroInteractions(transactionDao);
        verifyZeroInteractions(activeTraceDao);
        verifyZeroInteractions(dataSourceDao);
        verifyZeroInteractions(responseTimeDao);
    }

    @Test
    public void insertShouldNotBeCalledIfTAgentStatBatchIsMappedToNull() {
        // Given
        final int numBatches = 6;
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStatBatch agentStatBatch = createAgentStatBatch(agentId, startTimestamp, numBatches);
        final AgentStatBo mappedAgentStat = null;
        when(this.agentStatBatchMapper.map(agentStatBatch)).thenReturn(mappedAgentStat);
        // When
        agentStatHandler.handleSimple(agentStatBatch);
        // Then
        verifyZeroInteractions(jvmGcDao);
        verifyZeroInteractions(jvmGcDetailedDao);
        verifyZeroInteractions(cpuLoadDao);
        verifyZeroInteractions(transactionDao);
        verifyZeroInteractions(activeTraceDao);
        verifyZeroInteractions(dataSourceDao);
        verifyZeroInteractions(responseTimeDao);
    }

    @Test(expected=IllegalArgumentException.class)
    public void handleShouldThrowIllegalArgumentExceptionForIncorrectTBaseObjects() {
        // Given
        final TAgentInfo wrongTBaseObject = new TAgentInfo();
        // When
        agentStatHandler.handleSimple(wrongTBaseObject);
        // Then
        fail();
    }

    private TAgentStatBatch createAgentStatBatch(String agentId, long startTimestamp, int numBatches) {
        final TAgentStatBatch agentStatBatch = new TAgentStatBatch();
        agentStatBatch.setAgentId(agentId);
        agentStatBatch.setStartTimestamp(startTimestamp);
        final List<TAgentStat> agentStats = new ArrayList<>(numBatches);
        for (int i = 0; i < numBatches; ++i) {
            agentStats.add(createAgentStat(agentId, startTimestamp));
        }
        agentStatBatch.setAgentStats(agentStats);
        return agentStatBatch;
    }

    private TAgentStat createAgentStat(String agentId, long startTimestamp) {
        final TAgentStat agentStat = new TAgentStat();
        agentStat.setAgentId(agentId);
        agentStat.setStartTimestamp(startTimestamp);
        agentStat.setGc(new TJvmGc());
        agentStat.setCpuLoad(new TCpuLoad());
        agentStat.setDataSourceList(new TDataSourceList());
        agentStat.setResponseTime(new TResponseTime());
        agentStat.setDeadlock(new TDeadlock());
        return agentStat;
    }

}
