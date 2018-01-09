/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.collector;

import com.google.inject.Inject;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.monitor.collector.activethread.ActiveTraceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.cpu.CpuLoadMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.datasource.DataSourceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.deadlock.DeadlockMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.jvmgc.JvmGcMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.response.ResponseTimeMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.transaction.TransactionMetricCollector;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;

/**
 * @author HyunGil Jeong
 */
public class AgentStatCollector implements AgentStatMetricCollector<TAgentStat> {

    private final String agentId;
    private final long agentStartTimestamp;
    private final JvmGcMetricCollector jvmGcMetricCollector;
    private final CpuLoadMetricCollector cpuLoadMetricCollector;
    private final TransactionMetricCollector transactionMetricCollector;
    private final ActiveTraceMetricCollector activeTraceMetricCollector;
    private final DataSourceMetricCollector dataSourceMetricCollector;
    private final ResponseTimeMetricCollector responseTimeMetricCollector;
    private final DeadlockMetricCollector deadlockMetricCollector;

    @Inject
    public AgentStatCollector(
            @AgentId String agentId,
            @AgentStartTime long agentStartTimestamp,
            JvmGcMetricCollector jvmGcMetricCollector,
            CpuLoadMetricCollector cpuLoadMetricCollector,
            TransactionMetricCollector transactionMetricCollector,
            ActiveTraceMetricCollector activeTraceMetricCollector,
            DataSourceMetricCollector dataSourceMetricCollector,
            ResponseTimeMetricCollector responseTimeMetricCollector,
            DeadlockMetricCollector deadlockMetricCollector) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (jvmGcMetricCollector == null) {
            throw new NullPointerException("jvmGcMetricCollector must not be null");
        }
        if (cpuLoadMetricCollector == null) {
            throw new NullPointerException("cpuLoadMetricCollector must not be null");
        }
        if (transactionMetricCollector == null) {
            throw new NullPointerException("transactionMetricCollector must not be null");
        }
        if (activeTraceMetricCollector == null) {
            throw new NullPointerException("activeTraceMetricCollector must not be null");
        }
        if (dataSourceMetricCollector == null) {
            throw new NullPointerException("dataSourceMetricCollector must not be null");
        }
        if (responseTimeMetricCollector == null) {
            throw new NullPointerException("responseTimeMetricCollector must not be null");
        }
        if (deadlockMetricCollector == null) {
            throw new NullPointerException("deadlockMetricCollector may not be null");
        }

        this.agentId = agentId;
        this.agentStartTimestamp = agentStartTimestamp;
        this.jvmGcMetricCollector = jvmGcMetricCollector;
        this.cpuLoadMetricCollector = cpuLoadMetricCollector;
        this.transactionMetricCollector = transactionMetricCollector;
        this.activeTraceMetricCollector = activeTraceMetricCollector;
        this.dataSourceMetricCollector = dataSourceMetricCollector;
        this.responseTimeMetricCollector = responseTimeMetricCollector;
        this.deadlockMetricCollector = deadlockMetricCollector;
    }

    @Override
    public TAgentStat collect() {
        TAgentStat agentStat = new TAgentStat();
        agentStat.setAgentId(agentId);
        agentStat.setStartTimestamp(agentStartTimestamp);
        agentStat.setGc(jvmGcMetricCollector.collect());
        agentStat.setCpuLoad(cpuLoadMetricCollector.collect());
        agentStat.setTransaction(transactionMetricCollector.collect());
        agentStat.setActiveTrace(activeTraceMetricCollector.collect());
        agentStat.setDataSourceList(dataSourceMetricCollector.collect());
        agentStat.setResponseTime(responseTimeMetricCollector.collect());
        agentStat.setDeadlock(deadlockMetricCollector.collect());

        return agentStat;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentStatCollector{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", agentStartTimestamp=").append(agentStartTimestamp);
        sb.append(", jvmGcMetricCollector=").append(jvmGcMetricCollector);
        sb.append(", cpuLoadMetricCollector=").append(cpuLoadMetricCollector);
        sb.append(", transactionMetricCollector=").append(transactionMetricCollector);
        sb.append(", activeTraceMetricCollector=").append(activeTraceMetricCollector);
        sb.append(", dataSourceMetricCollector=").append(dataSourceMetricCollector);
        sb.append(", responseTimeMetricCollector=").append(responseTimeMetricCollector);
        sb.append(", deadlockMetricCollector=").append(deadlockMetricCollector);
        sb.append('}');
        return sb.toString();
    }

}
