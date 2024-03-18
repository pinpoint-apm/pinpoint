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
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.module.AgentIdHolder;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.loadedclass.LoadedClassMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.profiler.monitor.metric.totalthread.TotalThreadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetricSnapshot;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class AgentStatCollector implements AgentStatMetricCollector<AgentStatMetricSnapshot> {

    private final AgentId agentId;
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
    private final AgentStatMetricCollector<TotalThreadMetricSnapshot> totalThreadMetricCollector;
    private final AgentStatMetricCollector<LoadedClassMetricSnapshot> loadedClassMetricCollector;

    @Inject
    public AgentStatCollector(
            @AgentIdHolder AgentId agentId,
            @AgentStartTime long agentStartTimestamp,
            AgentStatMetricCollector<JvmGcMetricSnapshot> jvmGcMetricCollector,
            AgentStatMetricCollector<CpuLoadMetricSnapshot> cpuLoadMetricCollector,
            AgentStatMetricCollector<TransactionMetricSnapshot> transactionMetricCollector,
            AgentStatMetricCollector<ActiveTraceHistogram> activeTraceMetricCollector,
            AgentStatMetricCollector<DataSourceMetricSnapshot> dataSourceMetricCollector,
            AgentStatMetricCollector<ResponseTimeValue> responseTimeMetricCollector,
            AgentStatMetricCollector<DeadlockMetricSnapshot> deadlockMetricCollector,
            AgentStatMetricCollector<FileDescriptorMetricSnapshot> fileDescriptorMetricCollector,
            AgentStatMetricCollector<BufferMetricSnapshot> bufferMetricCollector,
            AgentStatMetricCollector<TotalThreadMetricSnapshot> totalThreadMetricCollector,
            AgentStatMetricCollector<LoadedClassMetricSnapshot> loadedClassMetricCollector) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTimestamp = agentStartTimestamp;
        this.jvmGcMetricCollector = Objects.requireNonNull(jvmGcMetricCollector, "jvmGcMetricCollector");
        this.cpuLoadMetricCollector = Objects.requireNonNull(cpuLoadMetricCollector, "cpuLoadMetricCollector");
        this.transactionMetricCollector = Objects.requireNonNull(transactionMetricCollector, "transactionMetricCollector");
        this.activeTraceMetricCollector = Objects.requireNonNull(activeTraceMetricCollector, "activeTraceMetricCollector");
        this.dataSourceMetricCollector = Objects.requireNonNull(dataSourceMetricCollector, "dataSourceMetricCollector");
        this.responseTimeMetricCollector = Objects.requireNonNull(responseTimeMetricCollector, "responseTimeMetricCollector");
        this.deadlockMetricCollector = Objects.requireNonNull(deadlockMetricCollector, "deadlockMetricCollector");
        this.fileDescriptorMetricCollector = Objects.requireNonNull(fileDescriptorMetricCollector, "fileDescriptorMetricCollector");
        this.bufferMetricCollector = Objects.requireNonNull(bufferMetricCollector, "bufferMetricCollector");
        this.totalThreadMetricCollector = Objects.requireNonNull(totalThreadMetricCollector, "totalThreadMetricCollector");
        this.loadedClassMetricCollector = Objects.requireNonNull(loadedClassMetricCollector, "loadedClassMetricCollector");
    }

    @Override
    public AgentStatMetricSnapshot collect() {
        AgentStatMetricSnapshot agentStat = new AgentStatMetricSnapshot();
        agentStat.setAgentId(AgentId.unwrap(agentId));
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
        agentStat.setTotalThread(totalThreadMetricCollector.collect());
        agentStat.setLoadedClassCount(loadedClassMetricCollector.collect());

        return agentStat;
    }

    @Override
    public String toString() {
        return "AgentStatCollector{" + "agentId='" + agentId + '\'' +
                ", agentStartTimestamp=" + agentStartTimestamp +
                ", jvmGcMetricCollector=" + jvmGcMetricCollector +
                ", cpuLoadMetricCollector=" + cpuLoadMetricCollector +
                ", transactionMetricCollector=" + transactionMetricCollector +
                ", activeTraceMetricCollector=" + activeTraceMetricCollector +
                ", dataSourceMetricCollector=" + dataSourceMetricCollector +
                ", responseTimeMetricCollector=" + responseTimeMetricCollector +
                ", deadlockMetricCollector=" + deadlockMetricCollector +
                ", fileDescriptorMetricCollector=" + fileDescriptorMetricCollector +
                ", bufferMetricCollector=" + bufferMetricCollector +
                ", totalThreadMetricCollector=" + totalThreadMetricCollector +
                ", loadedClassMetricCollector=" + loadedClassMetricCollector +
                '}';
    }
}