/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

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
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PActiveTrace;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PCpuLoad;
import com.navercorp.pinpoint.grpc.trace.PDataSource;
import com.navercorp.pinpoint.grpc.trace.PDataSourceList;
import com.navercorp.pinpoint.grpc.trace.PDeadlock;
import com.navercorp.pinpoint.grpc.trace.PDirectBuffer;
import com.navercorp.pinpoint.grpc.trace.PFileDescriptor;
import com.navercorp.pinpoint.grpc.trace.PJvmGc;
import com.navercorp.pinpoint.grpc.trace.PJvmGcDetailed;
import com.navercorp.pinpoint.grpc.trace.PResponseTime;
import com.navercorp.pinpoint.grpc.trace.PTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

@Component
public class GrpcAgentStatMapper {

    private final GrpcJvmGcBoMapper jvmGcBoMapper;

    private final GrpcJvmGcDetailedBoMapper jvmGcDetailedBoMapper;

    private final GrpcCpuLoadBoMapper cpuLoadBoMapper;

    private final GrpcTransactionBoMapper transactionBoMapper;

    private final GrpcActiveTraceBoMapper activeTraceBoMapper;

    private final GrpcDataSourceBoMapper dataSourceBoMapper;

    private final GrpcResponseTimeBoMapper responseTimeBoMapper;

    private final GrpcDeadlockThreadCountBoMapper deadlockThreadCountBoMapper;

    private final GrpcFileDescriptorBoMapper fileDescriptorBoMapper;

    private final GrpcDirectBufferBoMapper directBufferBoMapper;

    public GrpcAgentStatMapper(GrpcJvmGcBoMapper jvmGcBoMapper, GrpcJvmGcDetailedBoMapper jvmGcDetailedBoMapper, GrpcCpuLoadBoMapper cpuLoadBoMapper, GrpcTransactionBoMapper transactionBoMapper, GrpcActiveTraceBoMapper activeTraceBoMapper, GrpcDataSourceBoMapper dataSourceBoMapper, GrpcResponseTimeBoMapper responseTimeBoMapper, GrpcDeadlockThreadCountBoMapper deadlockThreadCountBoMapper, GrpcFileDescriptorBoMapper fileDescriptorBoMapper, GrpcDirectBufferBoMapper directBufferBoMapper) {
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


    public AgentStatBo map(PAgentStat agentStat) {
        if (agentStat == null) {
            return null;
        }

        final Header agentInfo = ServerContext.getAgentInfo();
        final String agentId = agentInfo.getAgentId();
        final long startTimestamp = agentInfo.getAgentStartTime();
        final long timestamp = agentStat.getTimestamp();
        final AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setAgentId(agentId);

        // jvmGc
        if (agentStat.hasGc()) {
            final PJvmGc jvmGc = agentStat.getGc();
            final JvmGcBo jvmGcBo = this.jvmGcBoMapper.map(jvmGc);
            setBaseData(jvmGcBo, agentId, startTimestamp, timestamp);
            agentStatBo.setJvmGcBos(Collections.singletonList(jvmGcBo));

            // jvmGcDetailed
            if (jvmGc.hasJvmGcDetailed()) {
                final PJvmGcDetailed jvmGcDetailed = jvmGc.getJvmGcDetailed();
                final JvmGcDetailedBo jvmGcDetailedBo = this.jvmGcDetailedBoMapper.map(jvmGcDetailed);
                setBaseData(jvmGcDetailedBo, agentId, startTimestamp, timestamp);
                agentStatBo.setJvmGcDetailedBos(Collections.singletonList(jvmGcDetailedBo));
            }
        }

        // cpuLoad
        if (agentStat.hasCpuLoad()) {
            final PCpuLoad cpuLoad = agentStat.getCpuLoad();
            final CpuLoadBo cpuLoadBo = this.cpuLoadBoMapper.map(cpuLoad);
            setBaseData(cpuLoadBo, agentId, startTimestamp, timestamp);
            agentStatBo.setCpuLoadBos(Collections.singletonList(cpuLoadBo));
        }

        // transaction
        if (agentStat.hasTransaction()) {
            final PTransaction transaction = agentStat.getTransaction();
            final TransactionBo transactionBo = this.transactionBoMapper.map(transaction);
            setBaseData(transactionBo, agentId, startTimestamp, timestamp);
            transactionBo.setCollectInterval(agentStat.getCollectInterval());
            agentStatBo.setTransactionBos(Collections.singletonList(transactionBo));
        }

        // activeTrace
        if (agentStat.hasActiveTrace()) {
            final PActiveTrace activeTrace = agentStat.getActiveTrace();
            if (activeTrace.hasHistogram()) {
                final ActiveTraceBo activeTraceBo = this.activeTraceBoMapper.map(activeTrace);
                setBaseData(activeTraceBo, agentId, startTimestamp, timestamp);
                agentStatBo.setActiveTraceBos(Collections.singletonList(activeTraceBo));
            }
        }

        // datasource
        if (agentStat.hasDataSourceList()) {
            final PDataSourceList dataSourceList = agentStat.getDataSourceList();
            final DataSourceListBo dataSourceListBo = new DataSourceListBo();
            setBaseData(dataSourceListBo, agentId, startTimestamp, timestamp);
            for (PDataSource dataSource : dataSourceList.getDataSourceList()) {
                final DataSourceBo dataSourceBo = dataSourceBoMapper.map(dataSource);
                setBaseData(dataSourceBo, agentId, startTimestamp, timestamp);
                dataSourceListBo.add(dataSourceBo);
            }
            agentStatBo.setDataSourceListBos(Collections.singletonList(dataSourceListBo));
        }

        // response time
        if (agentStat.hasResponseTime()) {
            final PResponseTime responseTime = agentStat.getResponseTime();
            final ResponseTimeBo responseTimeBo = this.responseTimeBoMapper.map(responseTime);
            setBaseData(responseTimeBo, agentId, startTimestamp, timestamp);
            agentStatBo.setResponseTimeBos(Collections.singletonList(responseTimeBo));
        }

        // deadlock
        if (agentStat.hasDeadlock()) {
            final PDeadlock deadlock = agentStat.getDeadlock();
            final DeadlockThreadCountBo deadlockThreadCountBo = this.deadlockThreadCountBoMapper.map(deadlock);
            setBaseData(deadlockThreadCountBo, agentId, startTimestamp, timestamp);
            agentStatBo.setDeadlockThreadCountBos(Collections.singletonList(deadlockThreadCountBo));
        }

        // fileDescriptor
        if (agentStat.hasFileDescriptor()) {
            final PFileDescriptor fileDescriptor = agentStat.getFileDescriptor();
            final FileDescriptorBo fileDescriptorBo = this.fileDescriptorBoMapper.map(fileDescriptor);
            setBaseData(fileDescriptorBo, agentId, startTimestamp, timestamp);
            agentStatBo.setFileDescriptorBos(Collections.singletonList(fileDescriptorBo));
        }

        // directBuffer
        if (agentStat.hasDirectBuffer()) {
            final PDirectBuffer directBuffer = agentStat.getDirectBuffer();
            final DirectBufferBo directBufferBo = this.directBufferBoMapper.map(directBuffer);
            setBaseData(directBufferBo, agentId, startTimestamp, timestamp);
            agentStatBo.setDirectBufferBos(Collections.singletonList(directBufferBo));
        }

        return agentStatBo;
    }

    private void setBaseData(AgentStatDataPoint agentStatDataPoint, String agentId, long startTimestamp, long timestamp) {
        agentStatDataPoint.setAgentId(agentId);
        agentStatDataPoint.setStartTimestamp(startTimestamp);
        agentStatDataPoint.setTimestamp(timestamp);
    }
}