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

package com.navercorp.pinpoint.profiler.monitor.metric;

import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetricSnapshot;

/**
 * @author jaehong.kim
 */
public class AgentStatMetricSnapshot {
    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private long collectInterval;
    private JvmGcMetricSnapshot gc;
    private CpuLoadMetricSnapshot cpuLoad;
    private TransactionMetricSnapshot transaction;
    private ActiveTraceHistogram activeTrace;
    private DataSourceMetricSnapshot dataSourceList;
    private ResponseTimeValue responseTime;
    private DeadlockMetricSnapshot deadlock;
    private FileDescriptorMetricSnapshot fileDescriptor;
    private BufferMetricSnapshot directBuffer;
    private String metadata;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getCollectInterval() {
        return collectInterval;
    }

    public void setCollectInterval(long collectInterval) {
        this.collectInterval = collectInterval;
    }

    public JvmGcMetricSnapshot getGc() {
        return gc;
    }

    public void setGc(JvmGcMetricSnapshot gc) {
        this.gc = gc;
    }

    public CpuLoadMetricSnapshot getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(CpuLoadMetricSnapshot cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public TransactionMetricSnapshot getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionMetricSnapshot transaction) {
        this.transaction = transaction;
    }

    public ActiveTraceHistogram getActiveTrace() {
        return activeTrace;
    }

    public void setActiveTrace(ActiveTraceHistogram activeTrace) {
        this.activeTrace = activeTrace;
    }

    public DataSourceMetricSnapshot getDataSourceList() {
        return dataSourceList;
    }

    public void setDataSourceList(DataSourceMetricSnapshot dataSourceList) {
        this.dataSourceList = dataSourceList;
    }

    public ResponseTimeValue getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(ResponseTimeValue responseTime) {
        this.responseTime = responseTime;
    }

    public DeadlockMetricSnapshot getDeadlock() {
        return deadlock;
    }

    public void setDeadlock(DeadlockMetricSnapshot deadlock) {
        this.deadlock = deadlock;
    }

    public FileDescriptorMetricSnapshot getFileDescriptor() {
        return fileDescriptor;
    }

    public void setFileDescriptor(FileDescriptorMetricSnapshot fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    public BufferMetricSnapshot getDirectBuffer() {
        return directBuffer;
    }

    public void setDirectBuffer(BufferMetricSnapshot directBuffer) {
        this.directBuffer = directBuffer;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentStatMetricSnapshot{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", collectInterval=").append(collectInterval);
        sb.append(", gc=").append(gc);
        sb.append(", cpuLoad=").append(cpuLoad);
        sb.append(", transaction=").append(transaction);
        sb.append(", activeTrace=").append(activeTrace);
        sb.append(", dataSourceList=").append(dataSourceList);
        sb.append(", responseTime=").append(responseTime);
        sb.append(", deadlock=").append(deadlock);
        sb.append(", fileDescriptor=").append(fileDescriptor);
        sb.append(", directBuffer=").append(directBuffer);
        sb.append(", metadata='").append(metadata).append('\'');
        sb.append('}');
        return sb.toString();
    }
}