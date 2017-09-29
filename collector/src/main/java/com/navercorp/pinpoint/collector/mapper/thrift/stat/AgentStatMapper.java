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

import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TDataSource;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentStatMapper implements ThriftBoMapper<AgentStatBo, TAgentStat> {

    @Autowired
    private JvmGcBoMapper jvmGcBoMapper;

    @Autowired
    private JvmGcDetailedBoMapper jvmGcDetailedBoMapper;

    @Autowired
    private CpuLoadBoMapper cpuLoadBoMapper;

    @Autowired
    private TransactionBoMapper transactionBoMapper;

    @Autowired
    private ActiveTraceBoMapper activeTraceBoMapper;

    @Autowired
    private DataSourceBoMapper dataSourceBoMapper;

    @Autowired
    private ResponseTimeBoMapper responseTimeBoMapper;

    @Autowired
    private DeadlockBoMapper deadlockBoMapper;

    @Override
    public AgentStatBo map(TAgentStat tAgentStat) {
        if (tAgentStat == null) {
            return null;
        }
        final String agentId = tAgentStat.getAgentId();
        final long startTimestamp = tAgentStat.getStartTimestamp();
        final long timestamp = tAgentStat.getTimestamp();
        AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setAgentId(agentId);
        // jvmGc
        if (tAgentStat.isSetGc()) {
            JvmGcBo jvmGcBo = this.jvmGcBoMapper.map(tAgentStat.getGc());
            setBaseData(jvmGcBo, agentId, startTimestamp, timestamp);
            agentStatBo.setJvmGcBos(Arrays.asList(jvmGcBo));
        }
        // jvmGcDetailed
        if (tAgentStat.isSetGc()) {
            if (tAgentStat.getGc().isSetJvmGcDetailed()) {
                JvmGcDetailedBo jvmGcDetailedBo = this.jvmGcDetailedBoMapper.map(tAgentStat.getGc().getJvmGcDetailed());
                setBaseData(jvmGcDetailedBo, agentId, startTimestamp, timestamp);
                agentStatBo.setJvmGcDetailedBos(Arrays.asList(jvmGcDetailedBo));
            }
        }
        // cpuLoad
        if (tAgentStat.isSetCpuLoad()) {
            CpuLoadBo cpuLoadBo = this.cpuLoadBoMapper.map(tAgentStat.getCpuLoad());
            setBaseData(cpuLoadBo, agentId, startTimestamp, timestamp);
            agentStatBo.setCpuLoadBos(Arrays.asList(cpuLoadBo));
        }
        // transaction
        if (tAgentStat.isSetTransaction()) {
            TransactionBo transactionBo = this.transactionBoMapper.map(tAgentStat.getTransaction());
            setBaseData(transactionBo, agentId, startTimestamp, timestamp);
            transactionBo.setCollectInterval(tAgentStat.getCollectInterval());
            agentStatBo.setTransactionBos(Arrays.asList(transactionBo));
        }
        // activeTrace
        if (tAgentStat.isSetActiveTrace() && tAgentStat.getActiveTrace().isSetHistogram()) {
            ActiveTraceBo activeTraceBo = this.activeTraceBoMapper.map(tAgentStat.getActiveTrace());
            setBaseData(activeTraceBo, agentId, startTimestamp, timestamp);
            agentStatBo.setActiveTraceBos(Arrays.asList(activeTraceBo));
        }
        // datasource
        if (tAgentStat.isSetDataSourceList()) {
            DataSourceListBo dataSourceListBo = new DataSourceListBo();
            setBaseData(dataSourceListBo, agentId, startTimestamp, timestamp);

            TDataSourceList dataSourceList = tAgentStat.getDataSourceList();
            for (TDataSource dataSource : dataSourceList.getDataSourceList()) {
                DataSourceBo dataSourceBo = dataSourceBoMapper.map(dataSource);
                setBaseData(dataSourceBo, agentId, startTimestamp, timestamp);
                dataSourceListBo.add(dataSourceBo);
            }
            agentStatBo.setDataSourceListBos(Arrays.asList(dataSourceListBo));
        }
        // response time
        if (tAgentStat.isSetResponseTime()) {
            ResponseTimeBo responseTimeBo = this.responseTimeBoMapper.map(tAgentStat.getResponseTime());
            setBaseData(responseTimeBo, agentId, startTimestamp, timestamp);
            agentStatBo.setResponseTimeBos(Arrays.asList(responseTimeBo));
        }
        // deadlock
        if (tAgentStat.isSetDeadlock()) {
            DeadlockBo deadlockBo = this.deadlockBoMapper.map(tAgentStat.getDeadlock());
            setBaseData(deadlockBo, agentId, startTimestamp, timestamp);
            agentStatBo.setDeadlockBos(Arrays.asList(deadlockBo));
        }

        return agentStatBo;
    }

    private void setBaseData(AgentStatDataPoint agentStatDataPoint, String agentId, long startTimestamp, long timestamp) {
        agentStatDataPoint.setAgentId(agentId);
        agentStatDataPoint.setStartTimestamp(startTimestamp);
        agentStatDataPoint.setTimestamp(timestamp);
    }

}
