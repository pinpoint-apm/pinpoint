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
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetricSnapshot;

/**
 * @author HyunGil Jeong
 */
public class AgentStatCollector implements AgentStatMetricCollector<AgentStatMetricSnapshot> {

    private final String agentId;
    private final long agentStartTimestamp;
    private final AgentStatMetricCollector<JvmGcMetricSnapshot> jvmGcMetricCollector;
    private final AgentStatMetricCollector<CpuLoadMetricSnapshot> cpuLoadMetricCollector;
    private final AgentStatMetricCollector<TransactionMetricSnapshot> transactionMetricCollector;
    private final AgentStatMetricCollector<ActiveTraceHistogram> activeTraceMetricCollector;
    private final AgentStatMetricCollector<DataSourceMetricSnapshot> dataSourceMetricCollector;
    private final AgentStatMetricCollector<ResponseTimeValue> responseTimeMetricCollector;
    private final AgentStatMetricCollector<DeadlockMetricSnapshot> deadlockMetricCollector;
    private final AgentStatMetricCollector<FileDescriptorMetricSnapshot> fileDescriptorMetricCollector;
    private final AgentStatMetricCollector<BufferMetricSnapshot> bufferMetricCollector;

    @Inject
    public AgentStatCollector(
            @AgentId String agentId,
            @AgentStartTime long agentStartTimestamp,
            AgentStatMetricCollector<JvmGcMetricSnapshot> jvmGcMetricCollector,
            AgentStatMetricCollector<CpuLoadMetricSnapshot> cpuLoadMetricCollector,
            AgentStatMetricCollector<TransactionMetricSnapshot> transactionMetricCollector,
            AgentStatMetricCollector<ActiveTraceHistogram> activeTraceMetricCollector,
            AgentStatMetricCollector<DataSourceMetricSnapshot> dataSourceMetricCollector,
            AgentStatMetricCollector<ResponseTimeValue> responseTimeMetricCollector,
            AgentStatMetricCollector<DeadlockMetricSnapshot> deadlockMetricCollector,
            AgentStatMetricCollector<FileDescriptorMetricSnapshot> fileDescriptorMetricCollector,
            AgentStatMetricCollector<BufferMetricSnapshot> bufferMetricCollector) {
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.agentStartTimestamp = agentStartTimestamp;
        this.jvmGcMetricCollector = Assert.requireNonNull(jvmGcMetricCollector, "jvmGcMetricCollector");
        this.cpuLoadMetricCollector = Assert.requireNonNull(cpuLoadMetricCollector, "cpuLoadMetricCollector");
        this.transactionMetricCollector = Assert.requireNonNull(transactionMetricCollector, "transactionMetricCollector");
        this.activeTraceMetricCollector = Assert.requireNonNull(activeTraceMetricCollector, "activeTraceMetricCollector");
        this.dataSourceMetricCollector = Assert.requireNonNull(dataSourceMetricCollector, "dataSourceMetricCollector");
        this.responseTimeMetricCollector = Assert.requireNonNull(responseTimeMetricCollector, "responseTimeMetricCollector");
        this.deadlockMetricCollector = Assert.requireNonNull(deadlockMetricCollector, "deadlockMetricCollector");
        this.fileDescriptorMetricCollector = Assert.requireNonNull(fileDescriptorMetricCollector, "fileDescriptorMetricCollector");
        this.bufferMetricCollector = Assert.requireNonNull(bufferMetricCollector, "bufferMetricCollector");
    }

    @Override
    public AgentStatMetricSnapshot collect() {
        AgentStatMetricSnapshot agentStat = new AgentStatMetricSnapshot();
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