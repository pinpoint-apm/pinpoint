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
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
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

import java.util.Arrays;

@Component
public class GrpcAgentStatMapper {

    @Autowired
    private GrpcJvmGcBoMapper jvmGcBoMapper;

    @Autowired
    private GrpcJvmGcDetailedBoMapper jvmGcDetailedBoMapper;

    @Autowired
    private GrpcCpuLoadBoMapper cpuLoadBoMapper;

    @Autowired
    private GrpcTransactionBoMapper transactionBoMapper;

    @Autowired
    private GrpcActiveTraceBoMapper activeTraceBoMapper;

    @Autowired
    private GrpcDataSourceBoMapper dataSourceBoMapper;

    @Autowired
    private GrpcResponseTimeBoMapper responseTimeBoMapper;

    @Autowired
    private GrpcDeadlockThreadCountBoMapper deadlockThreadCountBoMapper;

    @Autowired
    private GrpcFileDescriptorBoMapper fileDescriptorBoMapper;

    @Autowired
    private GrpcDirectBufferBoMapper directBufferBoMapper;

    public AgentStatBo map(PAgentStat agentStat) {
        if (agentStat == null) {
            return null;
        }

        final AgentHeaderFactory.Header agentInfo = ServerContext.getAgentInfo();
        final String agentId = agentInfo.getAgentId();
        final long startTimestamp = agentInfo.getAgentStartTime();
        final long timestamp = agentStat.getTimestamp();
        final AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setAgentId(agentId);

        // jvmGc
        final PJvmGc jvmGc = agentStat.getGc();
        if (jvmGc != null) {
            final JvmGcBo jvmGcBo = this.jvmGcBoMapper.map(jvmGc);
            setBaseData(jvmGcBo, agentId, startTimestamp, timestamp);
            agentStatBo.setJvmGcBos(Arrays.asList(jvmGcBo));

            // jvmGcDetailed
            final PJvmGcDetailed jvmGcDetailed = jvmGc.getJvmGcDetailed();
            if (jvmGcDetailed != null) {
                final JvmGcDetailedBo jvmGcDetailedBo = this.jvmGcDetailedBoMapper.map(jvmGcDetailed);
                setBaseData(jvmGcDetailedBo, agentId, startTimestamp, timestamp);
                agentStatBo.setJvmGcDetailedBos(Arrays.asList(jvmGcDetailedBo));
            }
        }

        // cpuLoad
        final PCpuLoad cpuLoad = agentStat.getCpuLoad();
        if (cpuLoad != null) {
            final CpuLoadBo cpuLoadBo = this.cpuLoadBoMapper.map(cpuLoad);
            setBaseData(cpuLoadBo, agentId, startTimestamp, timestamp);
            agentStatBo.setCpuLoadBos(Arrays.asList(cpuLoadBo));
        }

        // transaction
        final PTransaction transaction = agentStat.getTransaction();
        if (transaction != null) {
            final TransactionBo transactionBo = this.transactionBoMapper.map(transaction);
            setBaseData(transactionBo, agentId, startTimestamp, timestamp);
            transactionBo.setCollectInterval(agentStat.getCollectInterval());
            agentStatBo.setTransactionBos(Arrays.asList(transactionBo));
        }

        // activeTrace
        final PActiveTrace activeTrace = agentStat.getActiveTrace();
        if (activeTrace != null && activeTrace.getHistogram() != null) {
            final ActiveTraceBo activeTraceBo = this.activeTraceBoMapper.map(activeTrace);
            setBaseData(activeTraceBo, agentId, startTimestamp, timestamp);
            agentStatBo.setActiveTraceBos(Arrays.asList(activeTraceBo));
        }

        // datasource
        final PDataSourceList dataSourceList = agentStat.getDataSourceList();
        if (dataSourceList != null) {
            final DataSourceListBo dataSourceListBo = new DataSourceListBo();
            setBaseData(dataSourceListBo, agentId, startTimestamp, timestamp);
            for (PDataSource dataSource : dataSourceList.getDataSourceList()) {
                final DataSourceBo dataSourceBo = dataSourceBoMapper.map(dataSource);
                setBaseData(dataSourceBo, agentId, startTimestamp, timestamp);
                dataSourceListBo.add(dataSourceBo);
            }
            agentStatBo.setDataSourceListBos(Arrays.asList(dataSourceListBo));
        }

        // response time
        final PResponseTime responseTime = agentStat.getResponseTime();
        if (responseTime != null) {
            final ResponseTimeBo responseTimeBo = this.responseTimeBoMapper.map(responseTime);
            setBaseData(responseTimeBo, agentId, startTimestamp, timestamp);
            agentStatBo.setResponseTimeBos(Arrays.asList(responseTimeBo));
        }

        // deadlock
        final PDeadlock deadlock = agentStat.getDeadlock();
        if (deadlock != null) {
            final DeadlockThreadCountBo deadlockThreadCountBo = this.deadlockThreadCountBoMapper.map(deadlock);
            setBaseData(deadlockThreadCountBo, agentId, startTimestamp, timestamp);
            agentStatBo.setDeadlockThreadCountBos(Arrays.asList(deadlockThreadCountBo));
        }

        // fileDescriptor
        final PFileDescriptor fileDescriptor = agentStat.getFileDescriptor();
        if (fileDescriptor != null) {
            final FileDescriptorBo fileDescriptorBo = this.fileDescriptorBoMapper.map(fileDescriptor);
            setBaseData(fileDescriptorBo, agentId, startTimestamp, timestamp);
            agentStatBo.setFileDescriptorBos(Arrays.asList(fileDescriptorBo));
        }

        // directBuffer
        final PDirectBuffer directBuffer = agentStat.getDirectBuffer();
        if (directBuffer != null) {
            final DirectBufferBo directBufferBo = this.directBufferBoMapper.map(directBuffer);
            setBaseData(directBufferBo, agentId, startTimestamp, timestamp);
            agentStatBo.setDirectBufferBos(Arrays.asList(directBufferBo));
        }

        return agentStatBo;
    }

    private void setBaseData(AgentStatDataPoint agentStatDataPoint, String agentId, long startTimestamp, long timestamp) {
        agentStatDataPoint.setAgentId(agentId);
        agentStatDataPoint.setStartTimestamp(startTimestamp);
        agentStatDataPoint.setTimestamp(timestamp);
    }
}