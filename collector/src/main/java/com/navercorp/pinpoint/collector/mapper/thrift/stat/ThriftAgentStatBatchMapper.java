/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TDataSource;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component
public class ThriftAgentStatBatchMapper {

    private final ThriftJvmGcBoMapper jvmGcBoMapper;

    private final ThriftJvmGcDetailedBoMapper jvmGcDetailedBoMapper;

    private final ThriftCpuLoadBoMapper cpuLoadBoMapper;

    private final ThriftTransactionBoMapper transactionBoMapper;

    private final ThriftActiveTraceBoMapper activeTraceBoMapper;

    private final ThriftDataSourceBoMapper dataSourceBoMapper;

    private final ThriftResponseTimeBoMapper responseTimeBoMapper;

    private final ThriftDeadlockThreadCountBoMapper deadlockThreadCountBoMapper;

    private final ThriftFileDescriptorBoMapper fileDescriptorBoMapper;

    private final ThriftDirectBufferBoMapper directBufferBoMapper;

    public ThriftAgentStatBatchMapper(ThriftJvmGcBoMapper jvmGcBoMapper, ThriftJvmGcDetailedBoMapper jvmGcDetailedBoMapper,
                                      ThriftCpuLoadBoMapper cpuLoadBoMapper, ThriftTransactionBoMapper transactionBoMapper,
                                      ThriftActiveTraceBoMapper activeTraceBoMapper, ThriftDataSourceBoMapper dataSourceBoMapper,
                                      ThriftResponseTimeBoMapper responseTimeBoMapper, ThriftDeadlockThreadCountBoMapper deadlockThreadCountBoMapper,
                                      ThriftFileDescriptorBoMapper fileDescriptorBoMapper, ThriftDirectBufferBoMapper directBufferBoMapper) {
        this.jvmGcBoMapper = Objects.requireNonNull(jvmGcBoMapper, "jvmGcBoMapper");
        this.jvmGcDetailedBoMapper = Objects.requireNonNull(jvmGcDetailedBoMapper, "jvmGcDetailedBoMapper");
        this.cpuLoadBoMapper = Objects.requireNonNull(cpuLoadBoMapper, "cpuLoadBoMapper");
        this.transactionBoMapper = Objects.requireNonNull(transactionBoMapper, "transactionBoMapper");
        this.activeTraceBoMapper = Objects.requireNonNull(activeTraceBoMapper, "activeTraceBoMapper");
        this.dataSourceBoMapper = Objects.requireNonNull(dataSourceBoMapper, "dataSourceBoMapper");
        this.responseTimeBoMapper = Objects.requireNonNull(responseTimeBoMapper, "responseTimeBoMapper");
        this.deadlockThreadCountBoMapper = Objects.requireNonNull(deadlockThreadCountBoMapper, "deadlockThreadCountBoMapper");
        this.fileDescriptorBoMapper = Objects.requireNonNull(fileDescriptorBoMapper, "fileDescriptorBoMapper");
        this.directBufferBoMapper = Objects.requireNonNull(directBufferBoMapper, "directBufferBoMapper");
    }


    public AgentStatBo map(TAgentStatBatch tAgentStatBatch) {
        if (!tAgentStatBatch.isSetAgentStats()) {
            return null;
        }
        AgentStatBo agentStatBo = new AgentStatBo();
        final String agentId = tAgentStatBatch.getAgentId();
        final long startTimestamp = tAgentStatBatch.getStartTimestamp();
        agentStatBo.setAgentId(agentId);
        agentStatBo.setStartTimestamp(startTimestamp);


        int agentStatsSize = tAgentStatBatch.getAgentStatsSize();

        List<JvmGcBo> jvmGcBos = new ArrayList<>(agentStatsSize);
        List<JvmGcDetailedBo> jvmGcDetailedBos = new ArrayList<>(agentStatsSize);
        List<CpuLoadBo> cpuLoadBos = new ArrayList<>(agentStatsSize);
        List<TransactionBo> transactionBos = new ArrayList<>(agentStatsSize);
        List<ActiveTraceBo> activeTraceBos = new ArrayList<>(agentStatsSize);
        List<DataSourceListBo> dataSourceListBos = new ArrayList<DataSourceListBo>(agentStatsSize);
        List<ResponseTimeBo> responseTimeBos = new ArrayList<>(agentStatsSize);
        List<DeadlockThreadCountBo> deadlockThreadCountBos = new ArrayList<>(agentStatsSize);
        List<FileDescriptorBo> fileDescriptorBos = new ArrayList<>(agentStatsSize);
        List<DirectBufferBo> directBufferBos = new ArrayList<>(agentStatsSize);

        for (TAgentStat tAgentStat : tAgentStatBatch.getAgentStats()) {
            final long timestamp = tAgentStat.getTimestamp();
            // jvmGc
            if (tAgentStat.isSetGc()) {
                JvmGcBo jvmGcBo = this.jvmGcBoMapper.map(tAgentStat.getGc());
                setBaseData(jvmGcBo, agentId, startTimestamp, timestamp);
                jvmGcBos.add(jvmGcBo);
            }
            // jvmGcDetailed
            if (tAgentStat.isSetGc()) {
                if (tAgentStat.getGc().isSetJvmGcDetailed()) {
                    JvmGcDetailedBo jvmGcDetailedBo = this.jvmGcDetailedBoMapper.map(tAgentStat.getGc().getJvmGcDetailed());
                    setBaseData(jvmGcDetailedBo, agentId, startTimestamp, timestamp);
                    jvmGcDetailedBos.add(jvmGcDetailedBo);
                }
            }
            // cpuLoad
            if (tAgentStat.isSetCpuLoad()) {
                CpuLoadBo cpuLoadBo = this.cpuLoadBoMapper.map(tAgentStat.getCpuLoad());
                setBaseData(cpuLoadBo, agentId, startTimestamp, timestamp);
                cpuLoadBos.add(cpuLoadBo);
            }
            // transaction
            if (tAgentStat.isSetTransaction()) {
                TransactionBo transactionBo = this.transactionBoMapper.map(tAgentStat.getTransaction());
                setBaseData(transactionBo, agentId, startTimestamp, timestamp);
                transactionBo.setCollectInterval(tAgentStat.getCollectInterval());
                transactionBos.add(transactionBo);
            }
            // activeTrace
            if (tAgentStat.isSetActiveTrace() && tAgentStat.getActiveTrace().isSetHistogram()) {
                ActiveTraceBo activeTraceBo = this.activeTraceBoMapper.map(tAgentStat.getActiveTrace());
                setBaseData(activeTraceBo, agentId, startTimestamp, timestamp);
                activeTraceBos.add(activeTraceBo);
            }

            // datasource
            if (tAgentStat.isSetDataSourceList()) {
                DataSourceListBo dataSourceListBo = new DataSourceListBo();
                setBaseData(dataSourceListBo, agentId, startTimestamp, timestamp);

                TDataSourceList dataSourceList = tAgentStat.getDataSourceList();
                if (dataSourceList.getDataSourceListSize() > 0) {
                    for (TDataSource dataSource : dataSourceList.getDataSourceList()) {
                        DataSourceBo dataSourceBo = dataSourceBoMapper.map(dataSource);
                        setBaseData(dataSourceBo, agentId, startTimestamp, timestamp);
                        dataSourceListBo.add(dataSourceBo);
                    }
                }
                dataSourceListBos.add(dataSourceListBo);
            }

            // response time
            if (tAgentStat.isSetResponseTime()) {
                ResponseTimeBo responseTimeBo = this.responseTimeBoMapper.map(tAgentStat.getResponseTime());
                setBaseData(responseTimeBo, agentId, startTimestamp, timestamp);
                responseTimeBos.add(responseTimeBo);
            }

            // deadlock
            if (tAgentStat.isSetDeadlock()) {
                DeadlockThreadCountBo deadlockThreadCountBo = this.deadlockThreadCountBoMapper.map(tAgentStat.getDeadlock());
                setBaseData(deadlockThreadCountBo, agentId, startTimestamp, timestamp);
                deadlockThreadCountBos.add(deadlockThreadCountBo);
            }

            // fileDescriptor
            if (tAgentStat.isSetFileDescriptor()) {
                FileDescriptorBo fileDescriptorBo = this.fileDescriptorBoMapper.map(tAgentStat.getFileDescriptor());
                setBaseData(fileDescriptorBo, agentId, startTimestamp, timestamp);
                fileDescriptorBos.add(fileDescriptorBo);
            }

            // directBuffer
            if (tAgentStat.isSetDirectBuffer()) {
                DirectBufferBo directBufferBo = this.directBufferBoMapper.map(tAgentStat.getDirectBuffer());
                setBaseData(directBufferBo, agentId, startTimestamp, timestamp);
                directBufferBos.add(directBufferBo);
            }
        }

        agentStatBo.setJvmGcBos(jvmGcBos);
        agentStatBo.setJvmGcDetailedBos(jvmGcDetailedBos);
        agentStatBo.setCpuLoadBos(cpuLoadBos);
        agentStatBo.setTransactionBos(transactionBos);
        agentStatBo.setActiveTraceBos(activeTraceBos);
        agentStatBo.setDataSourceListBos(dataSourceListBos);
        agentStatBo.setResponseTimeBos(responseTimeBos);
        agentStatBo.setDeadlockThreadCountBos(deadlockThreadCountBos);
        agentStatBo.setFileDescriptorBos(fileDescriptorBos);
        agentStatBo.setDirectBufferBos(directBufferBos);
        return agentStatBo;
    }

    private void setBaseData(AgentStatDataPoint agentStatDataPoint, String agentId, long startTimestamp, long timestamp) {
        agentStatDataPoint.setAgentId(agentId);
        agentStatDataPoint.setStartTimestamp(startTimestamp);
        agentStatDataPoint.setTimestamp(timestamp);
    }
}
