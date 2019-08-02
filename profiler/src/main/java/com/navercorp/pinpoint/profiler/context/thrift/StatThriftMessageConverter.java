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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogramUtils;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcDetailedMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSource;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetricSnapshot;
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TActiveTraceHistogram;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import com.navercorp.pinpoint.thrift.dto.TDataSource;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.TDirectBuffer;
import com.navercorp.pinpoint.thrift.dto.TFileDescriptor;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcDetailed;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;
import com.navercorp.pinpoint.thrift.dto.TResponseTime;
import com.navercorp.pinpoint.thrift.dto.TTransaction;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import org.apache.thrift.TBase;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class StatThriftMessageConverter implements MessageConverter<TBase<?, ?>> {
    private final ThreadDumpThriftMessageConverter threadDumpMessageConverter = new ThreadDumpThriftMessageConverter();
    private final JvmGcTypeThriftMessageConverter jvmGcTypeMessageConverter = new JvmGcTypeThriftMessageConverter();

    @Override
    public TBase<?, ?> toMessage(Object message) {
        if (message instanceof AgentStatMetricSnapshotBatch) {
            final AgentStatMetricSnapshotBatch agentStatMetricSnapshotBatch = (AgentStatMetricSnapshotBatch) message;
            final TAgentStatBatch agentStatBatch = new TAgentStatBatch();
            agentStatBatch.setAgentId(agentStatMetricSnapshotBatch.getAgentId());
            agentStatBatch.setStartTimestamp(agentStatMetricSnapshotBatch.getStartTimestamp());
            for (AgentStatMetricSnapshot agentStatMetricSnapshot : agentStatMetricSnapshotBatch.getAgentStats()) {
                final TAgentStat agentStat = convertAgentStat(agentStatMetricSnapshot);
                agentStatBatch.addToAgentStats(agentStat);
            }
            return agentStatBatch;
        } else if (message instanceof AgentStatMetricSnapshot) {
            final AgentStatMetricSnapshot agentStatMetricSnapshot = (AgentStatMetricSnapshot) message;
            final TAgentStat agentStat = convertAgentStat(agentStatMetricSnapshot);
            return agentStat;
        }
        return null;
    }

    private TAgentStat convertAgentStat(final AgentStatMetricSnapshot agentStatMetricSnapshot) {
        final TAgentStat agentStat = new TAgentStat();
        agentStat.setTimestamp(agentStatMetricSnapshot.getTimestamp());
        agentStat.setCollectInterval(agentStatMetricSnapshot.getCollectInterval());

        // Agent information
        agentStat.setAgentId(agentStatMetricSnapshot.getAgentId());
        agentStat.setStartTimestamp(agentStatMetricSnapshot.getStartTimestamp());

        // Metric
        final JvmGcMetricSnapshot jvmGcMetricSnapshot = agentStatMetricSnapshot.getGc();
        if (jvmGcMetricSnapshot != null) {
            final TJvmGc jvmGc = convertJvmGc(jvmGcMetricSnapshot);
            agentStat.setGc(jvmGc);
        }

        final CpuLoadMetricSnapshot cpuLoadMetricSnapshot = agentStatMetricSnapshot.getCpuLoad();
        if (cpuLoadMetricSnapshot != null) {
            final TCpuLoad cpuLoad = convertCpuLoad(cpuLoadMetricSnapshot);
            agentStat.setCpuLoad(cpuLoad);
        }

        final TransactionMetricSnapshot transactionMetricSnapshot = agentStatMetricSnapshot.getTransaction();
        if (transactionMetricSnapshot != null) {
            final TTransaction transaction = convertTransaction(transactionMetricSnapshot);
            agentStat.setTransaction(transaction);
        }

        final ActiveTraceHistogram activeTraceHistogram = agentStatMetricSnapshot.getActiveTrace();
        if (activeTraceHistogram != null) {
            final TActiveTrace activeTrace = convertActiveTrace(activeTraceHistogram);
            agentStat.setActiveTrace(activeTrace);
        }

        final DataSourceMetricSnapshot dataSourceMetricSnapshot = agentStatMetricSnapshot.getDataSourceList();
        if (dataSourceMetricSnapshot != null) {
            final TDataSourceList dataSourceList = convertDataSourceList(dataSourceMetricSnapshot);
            agentStat.setDataSourceList(dataSourceList);
        }

        final ResponseTimeValue responseTimeValue = agentStatMetricSnapshot.getResponseTime();
        if(responseTimeValue != null) {
            final TResponseTime responseTime = convertResponseTime(responseTimeValue);
            agentStat.setResponseTime(responseTime);
        }

        final DeadlockMetricSnapshot deadlockMetricSnapshot = agentStatMetricSnapshot.getDeadlock();
        if (deadlockMetricSnapshot != null) {
            final TDeadlock deadlock = convertDeadlock(deadlockMetricSnapshot);
            agentStat.setDeadlock(deadlock);
        }

        final FileDescriptorMetricSnapshot fileDescriptorMetricSnapshot = agentStatMetricSnapshot.getFileDescriptor();
        if(fileDescriptorMetricSnapshot != null) {
            final TFileDescriptor fileDescriptor = convertFileDescriptor(fileDescriptorMetricSnapshot);
            agentStat.setFileDescriptor(fileDescriptor);
        }

        final BufferMetricSnapshot bufferMetricSnapshot = agentStatMetricSnapshot.getDirectBuffer();
        if(bufferMetricSnapshot != null) {
            final TDirectBuffer directBuffer = convertDirectBuffer(bufferMetricSnapshot);
            agentStat.setDirectBuffer(directBuffer);
        }

        return agentStat;
    }

    private TJvmGc convertJvmGc(JvmGcMetricSnapshot jvmGcMetricSnapshot) {
        final TJvmGc jvmGc = new TJvmGc();
        jvmGc.setJvmMemoryHeapMax(jvmGcMetricSnapshot.getJvmMemoryHeapMax());
        jvmGc.setJvmMemoryHeapUsed(jvmGcMetricSnapshot.getJvmMemoryHeapUsed());
        jvmGc.setJvmMemoryNonHeapMax(jvmGcMetricSnapshot.getJvmMemoryNonHeapMax());
        jvmGc.setJvmMemoryNonHeapUsed(jvmGcMetricSnapshot.getJvmMemoryNonHeapUsed());
        jvmGc.setJvmGcOldCount(jvmGcMetricSnapshot.getJvmGcOldCount());
        jvmGc.setJvmGcOldTime(jvmGcMetricSnapshot.getJvmGcOldTime());

        final TJvmGcType jvmGcType = this.jvmGcTypeMessageConverter.toMessage(jvmGcMetricSnapshot.getType());
        jvmGc.setType(jvmGcType);

        if (jvmGcMetricSnapshot.getJvmGcDetailed() != null) {
            final JvmGcDetailedMetricSnapshot jvmGcDetailedMetricSnapshot = jvmGcMetricSnapshot.getJvmGcDetailed();
            final TJvmGcDetailed jvmGcDetailed = new TJvmGcDetailed();
            jvmGcDetailed.setJvmPoolNewGenUsed(jvmGcDetailedMetricSnapshot.getJvmPoolNewGenUsed());
            jvmGcDetailed.setJvmPoolOldGenUsed(jvmGcDetailedMetricSnapshot.getJvmPoolOldGenUsed());
            jvmGcDetailed.setJvmPoolSurvivorSpaceUsed(jvmGcDetailedMetricSnapshot.getJvmPoolSurvivorSpaceUsed());
            jvmGcDetailed.setJvmPoolCodeCacheUsed(jvmGcDetailedMetricSnapshot.getJvmPoolCodeCacheUsed());
            jvmGcDetailed.setJvmPoolPermGenUsed(jvmGcDetailedMetricSnapshot.getJvmPoolPermGenUsed());
            jvmGcDetailed.setJvmPoolMetaspaceUsed(jvmGcDetailedMetricSnapshot.getJvmPoolMetaspaceUsed());
            jvmGcDetailed.setJvmGcNewCount(jvmGcDetailedMetricSnapshot.getJvmGcNewCount());
            jvmGcDetailed.setJvmGcNewTime(jvmGcDetailedMetricSnapshot.getJvmGcNewTime());
            jvmGc.setJvmGcDetailed(jvmGcDetailed);
        }
        return jvmGc;
    }

    private TCpuLoad convertCpuLoad(CpuLoadMetricSnapshot cpuLoadMetricSnapshot) {
        final TCpuLoad cpuLoad = new TCpuLoad();
        cpuLoad.setJvmCpuLoad(cpuLoadMetricSnapshot.getJvmCpuUsage());
        cpuLoad.setSystemCpuLoad(cpuLoadMetricSnapshot.getSystemCpuUsage());
        return cpuLoad;
    }

    private TTransaction convertTransaction(TransactionMetricSnapshot transactionMetricSnapshot) {
        final TTransaction transaction = new TTransaction();
        transaction.setSampledNewCount(transactionMetricSnapshot.getSampledNewCount());
        transaction.setSampledContinuationCount(transactionMetricSnapshot.getSampledContinuationCount());
        transaction.setUnsampledNewCount(transactionMetricSnapshot.getUnsampledNewCount());
        transaction.setUnsampledContinuationCount(transactionMetricSnapshot.getUnsampledContinuationCount());
        transaction.setSkippedNewCount(transactionMetricSnapshot.getSkippedNewCount());
        transaction.setSkippedContinuationCount(transactionMetricSnapshot.getSkippedContinuationCount());
        return transaction;
    }

    private TActiveTrace convertActiveTrace(ActiveTraceHistogram activeTraceHistogramMetricSnapshot) {
        final TActiveTrace activeTrace = new TActiveTrace();
        final TActiveTraceHistogram activeTraceHistogram = new TActiveTraceHistogram();
        if(activeTraceHistogramMetricSnapshot.getHistogramSchema() != null) {
            activeTraceHistogram.setHistogramSchemaType(activeTraceHistogramMetricSnapshot.getHistogramSchema().getTypeCode());
        }
        final List<Integer> activeTraceCounts = ActiveTraceHistogramUtils.asList(activeTraceHistogramMetricSnapshot);
        activeTraceHistogram.setActiveTraceCount(activeTraceCounts);
        activeTrace.setHistogram(activeTraceHistogram);
        return activeTrace;
    }

    private TDataSourceList convertDataSourceList(DataSourceMetricSnapshot dataSourceMetricSnapshot) {
        final TDataSourceList dataSourceList = new TDataSourceList();
        for (DataSource dataSourceCollectData : dataSourceMetricSnapshot.getDataSourceList()) {
            final TDataSource dataSource = new TDataSource(dataSourceCollectData.getId());
            dataSource.setServiceTypeCode(dataSourceCollectData.getServiceTypeCode());
            if (dataSourceCollectData.getDatabaseName() != null) {
                dataSource.setDatabaseName(dataSourceCollectData.getDatabaseName());
            }
            if (dataSourceCollectData.getActiveConnectionSize() != 0) {
                dataSource.setActiveConnectionSize(dataSourceCollectData.getActiveConnectionSize());
            }
            if (dataSourceCollectData.getUrl() != null) {
                dataSource.setUrl(dataSourceCollectData.getUrl());
            }
            dataSource.setMaxConnectionSize(dataSourceCollectData.getMaxConnectionSize());
            dataSourceList.addToDataSourceList(dataSource);
        }
        return dataSourceList;
    }

    private TResponseTime convertResponseTime(ResponseTimeValue responseTimeValue) {
        final TResponseTime responseTime = new TResponseTime();
        if (responseTimeValue.getAvg() != 0) {
            responseTime.setAvg(responseTimeValue.getAvg());
        }
        if (responseTimeValue.getMax() != 0) {
            responseTime.setMax(responseTimeValue.getMax());
        }
        return responseTime;
    }


    private TDeadlock convertDeadlock(DeadlockMetricSnapshot deadlockMetricSnapshot) {
        // Only send id values that have already been sent
        final TDeadlock deadlock = new TDeadlock();
        deadlock.setDeadlockedThreadCount(deadlockMetricSnapshot.getDeadlockedThreadCount());

        for (ThreadDumpMetricSnapshot threadDumpMetricSnapshot : deadlockMetricSnapshot.getDeadlockedThreadList()) {
            final TThreadDump threadDump = this.threadDumpMessageConverter.toMessage(threadDumpMetricSnapshot);
            if (threadDump != null) {
                deadlock.addToDeadlockedThreadList(threadDump);
            }
        }
        return deadlock;
    }

    private TFileDescriptor convertFileDescriptor(FileDescriptorMetricSnapshot fileDescriptorMetricSnapshot) {
        final TFileDescriptor fileDescriptor = new TFileDescriptor();
        fileDescriptor.setOpenFileDescriptorCount(fileDescriptorMetricSnapshot.getOpenFileDescriptorCount());
        return fileDescriptor;
    }

    private TDirectBuffer convertDirectBuffer(BufferMetricSnapshot bufferMetricSnapshot) {
        final TDirectBuffer tdirectBuffer = new TDirectBuffer();
        tdirectBuffer.setDirectCount(bufferMetricSnapshot.getDirectCount());
        tdirectBuffer.setDirectMemoryUsed(bufferMetricSnapshot.getDirectMemoryUsed());
        tdirectBuffer.setMappedCount(bufferMetricSnapshot.getMappedCount());
        tdirectBuffer.setMappedMemoryUsed(bufferMetricSnapshot.getMappedMemoryUsed());
        return tdirectBuffer;
    }
}