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

package com.navercorp.pinpoint.collector.handler.thrift;

import com.navercorp.pinpoint.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.ThriftAgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.ThriftAgentStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.collector.service.HBaseAgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.*;
import com.navercorp.pinpoint.thrift.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public class ThriftAgentStatHandlerV2Test {

    @Mock
    private ThriftAgentStatMapper agentStatMapper;

    @Mock
    private ThriftAgentStatBatchMapper agentStatBatchMapper;

    @Mock
    private AgentStatDao<JvmGcBo> jvmGcDao;

    @Mock
    private AgentStatDao<JvmGcDetailedBo> jvmGcDetailedDao;

    @Mock
    private AgentStatDao<CpuLoadBo> cpuLoadDao;

    @Mock
    private AgentStatDao<TransactionBo> transactionDao;

    @Mock
    private AgentStatDao<ActiveTraceBo> activeTraceDao;

    @Mock
    private AgentStatDao<DataSourceListBo> dataSourceDao;

    @Mock
    private AgentStatDao<ResponseTimeBo> responseTimeDao;

    @Mock
    private AgentStatDao<DeadlockThreadCountBo> deadlockDao;

    @Mock
    private AgentStatDao<FileDescriptorBo> fileDescriptorDao;

    @Mock
    private AgentStatDao<DirectBufferBo> directBufferDao;

    @Mock
    private AgentStatDao<TotalThreadCountBo> totalThreadCountDao;

    @Mock
    private AgentStatDao<LoadedClassBo> loadedClassDao;

    @Spy
    private List<AgentStatService> agentStatServiceList = new ArrayList<>();

    private ThriftAgentStatHandlerV2 thriftAgentStatHandlerV2;
    private HBaseAgentStatService hBaseAgentStatService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        hBaseAgentStatService = new HBaseAgentStatService(new AgentStatDao[]{jvmGcDao, jvmGcDetailedDao, cpuLoadDao, transactionDao,
                activeTraceDao, dataSourceDao, responseTimeDao, deadlockDao, fileDescriptorDao,
                directBufferDao, totalThreadCountDao, loadedClassDao});
        agentStatServiceList.add(hBaseAgentStatService);
        thriftAgentStatHandlerV2 = new ThriftAgentStatHandlerV2(agentStatMapper, agentStatBatchMapper, agentStatServiceList.toArray(new AgentStatService[0]));
    }

    @Test
    public void testHandleForTAgentStat() {
        // Given
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp);
        final AgentStatBo mappedAgentStat = mock(AgentStatBo.class);
        when(this.agentStatMapper.map(agentStat)).thenReturn(mappedAgentStat);
        // When
        thriftAgentStatHandlerV2.handleSimple(agentStat);
        // Then
        verify(jvmGcDao).dispatch(mappedAgentStat);
        verify(jvmGcDetailedDao).dispatch(mappedAgentStat);
        verify(cpuLoadDao).dispatch(mappedAgentStat);
        verify(transactionDao).dispatch(mappedAgentStat);
        verify(activeTraceDao).dispatch(mappedAgentStat);
        verify(dataSourceDao).dispatch(mappedAgentStat);
        verify(responseTimeDao).dispatch(mappedAgentStat);
        verify(deadlockDao).dispatch(mappedAgentStat);
        verify(fileDescriptorDao).dispatch(mappedAgentStat);
        verify(directBufferDao).dispatch(mappedAgentStat);
        verify(totalThreadCountDao).dispatch(mappedAgentStat);
        verify(loadedClassDao).dispatch(mappedAgentStat);
    }

    @Test
    public void testHandleForTAgentStatBatch() {
        // Given
        final int numBatches = 6;
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStatBatch agentStatBatch = createAgentStatBatch(agentId, startTimestamp, numBatches);
        final AgentStatBo mappedAgentStat = mock(AgentStatBo.class);
        when(this.agentStatBatchMapper.map(agentStatBatch)).thenReturn(mappedAgentStat);
        // When
        thriftAgentStatHandlerV2.handleSimple(agentStatBatch);
        // Then
        verify(jvmGcDao).dispatch(mappedAgentStat);
        verify(jvmGcDetailedDao).dispatch(mappedAgentStat);
        verify(cpuLoadDao).dispatch(mappedAgentStat);
        verify(transactionDao).dispatch(mappedAgentStat);
        verify(activeTraceDao).dispatch(mappedAgentStat);
        verify(dataSourceDao).dispatch(mappedAgentStat);
        verify(responseTimeDao).dispatch(mappedAgentStat);
        verify(deadlockDao).dispatch(mappedAgentStat);
        verify(fileDescriptorDao).dispatch(mappedAgentStat);
        verify(directBufferDao).dispatch(mappedAgentStat);
        verify(totalThreadCountDao).dispatch(mappedAgentStat);
        verify(loadedClassDao).dispatch(mappedAgentStat);
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
        thriftAgentStatHandlerV2.handleSimple(agentStat);
        // Then
        verifyZeroInteractions(jvmGcDao);
        verifyZeroInteractions(jvmGcDetailedDao);
        verifyZeroInteractions(cpuLoadDao);
        verifyZeroInteractions(transactionDao);
        verifyZeroInteractions(activeTraceDao);
        verifyZeroInteractions(dataSourceDao);
        verifyZeroInteractions(responseTimeDao);
        verifyZeroInteractions(fileDescriptorDao);
        verifyZeroInteractions(directBufferDao);
        verifyZeroInteractions(totalThreadCountDao);
        verifyZeroInteractions(loadedClassDao);
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
        thriftAgentStatHandlerV2.handleSimple(agentStatBatch);
        // Then
        verifyZeroInteractions(jvmGcDao);
        verifyZeroInteractions(jvmGcDetailedDao);
        verifyZeroInteractions(cpuLoadDao);
        verifyZeroInteractions(transactionDao);
        verifyZeroInteractions(activeTraceDao);
        verifyZeroInteractions(dataSourceDao);
        verifyZeroInteractions(responseTimeDao);
        verifyZeroInteractions(fileDescriptorDao);
        verifyZeroInteractions(directBufferDao);
        verifyZeroInteractions(totalThreadCountDao);
        verifyZeroInteractions(loadedClassDao);
    }

    @Test
    public void handleShouldThrowIllegalArgumentExceptionForIncorrectTBaseObjects() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            // Given
            final TAgentInfo wrongTBaseObject = new TAgentInfo();
            // When
            thriftAgentStatHandlerV2.handleSimple(wrongTBaseObject);
            // Then
            fail();
        });
    }

    private TAgentStatBatch createAgentStatBatch(String agentId, long startTimestamp, int numBatches) {
        final TAgentStatBatch agentStatBatch = new TAgentStatBatch();
        agentStatBatch.setAgentId(agentId);
        agentStatBatch.setStartTimestamp(startTimestamp);
        final List<TAgentStat> agentStats = new ArrayList<>(numBatches);
        for (int i = 0; i < numBatches; i++) {
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
        agentStat.setFileDescriptor(new TFileDescriptor());
        agentStat.setDirectBuffer(new TDirectBuffer());
        agentStat.setTotalThreadCount(new TTotalThreadCount());
        agentStat.setLoadedClass(new TLoadedClass());
        return agentStat;
    }

}
