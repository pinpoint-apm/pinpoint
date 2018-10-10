/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.collector;

import com.google.inject.Inject;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.TDirectBuffer;
import com.navercorp.pinpoint.thrift.dto.TFileDescriptor;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TResponseTime;
import com.navercorp.pinpoint.thrift.dto.TTransaction;

/**
 * @author HyunGil Jeong
 */
public class AgentStatCollector implements AgentStatMetricCollector<TAgentStat> {

    private final String agentId;
    private final long agentStartTimestamp;
    private final AgentStatMetricCollector<TJvmGc> jvmGcMetricCollector;
    private final AgentStatMetricCollector<TCpuLoad> cpuLoadMetricCollector;
    private final AgentStatMetricCollector<TTransaction> transactionMetricCollector;
    private final AgentStatMetricCollector<TActiveTrace> activeTraceMetricCollector;
    private final AgentStatMetricCollector<TDataSourceList> dataSourceMetricCollector;
    private final AgentStatMetricCollector<TResponseTime> responseTimeMetricCollector;
    private final AgentStatMetricCollector<TDeadlock> deadlockMetricCollector;
    private final AgentStatMetricCollector<TFileDescriptor> fileDescriptorMetricCollector;
    private final AgentStatMetricCollector<TDirectBuffer> bufferMetricCollector;

    @Inject
    public AgentStatCollector(
            @AgentId String agentId,
            @AgentStartTime long agentStartTimestamp,
            AgentStatMetricCollector<TJvmGc> jvmGcMetricCollector,
            AgentStatMetricCollector<TCpuLoad> cpuLoadMetricCollector,
            AgentStatMetricCollector<TTransaction> transactionMetricCollector,
            AgentStatMetricCollector<TActiveTrace> activeTraceMetricCollector,
            AgentStatMetricCollector<TDataSourceList> dataSourceMetricCollector,
            AgentStatMetricCollector<TResponseTime> responseTimeMetricCollector,
            AgentStatMetricCollector<TDeadlock> deadlockMetricCollector,
            AgentStatMetricCollector<TFileDescriptor> fileDescriptorMetricCollector,
            AgentStatMetricCollector<TDirectBuffer> bufferMetricCollector) {
        this.agentId = Assert.requireNonNull(agentId, "agentId must not be null");
        this.agentStartTimestamp = agentStartTimestamp;
        this.jvmGcMetricCollector = Assert.requireNonNull(jvmGcMetricCollector, "jvmGcMetricCollector must not be null");
        this.cpuLoadMetricCollector = Assert.requireNonNull(cpuLoadMetricCollector, "cpuLoadMetricCollector must not be null");
        this.transactionMetricCollector = Assert.requireNonNull(transactionMetricCollector, "transactionMetricCollector must not be null");
        this.activeTraceMetricCollector = Assert.requireNonNull(activeTraceMetricCollector, "activeTraceMetricCollector must not be null");
        this.dataSourceMetricCollector = Assert.requireNonNull(dataSourceMetricCollector, "dataSourceMetricCollector must not be null");
        this.responseTimeMetricCollector = Assert.requireNonNull(responseTimeMetricCollector, "responseTimeMetricCollector must not be null");
        this.deadlockMetricCollector = Assert.requireNonNull(deadlockMetricCollector, "deadlockMetricCollector must not be null");
        this.fileDescriptorMetricCollector = Assert.requireNonNull(fileDescriptorMetricCollector, "fileDescriptorMetricCollector must not be null");
        this.bufferMetricCollector = Assert.requireNonNull(bufferMetricCollector, "bufferMetricCollector must not be null");
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
        agentStat.setFileDescriptor(fileDescriptorMetricCollector.collect());
        agentStat.setDirectBuffer(bufferMetricCollector.collect());

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
        sb.append(", fileDescriptorMetricCollector=").append(fileDescriptorMetricCollector);
        sb.append(", bufferMetricCollector=").append(bufferMetricCollector);
        sb.append('}');
        return sb.toString();
    }

}
