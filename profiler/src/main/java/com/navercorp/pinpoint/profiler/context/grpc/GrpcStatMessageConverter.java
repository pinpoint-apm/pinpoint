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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.grpc.trace.PActiveTrace;
import com.navercorp.pinpoint.grpc.trace.PActiveTraceHistogram;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PCpuLoad;
import com.navercorp.pinpoint.grpc.trace.PDataSource;
import com.navercorp.pinpoint.grpc.trace.PDataSourceList;
import com.navercorp.pinpoint.grpc.trace.PDeadlock;
import com.navercorp.pinpoint.grpc.trace.PDirectBuffer;
import com.navercorp.pinpoint.grpc.trace.PFileDescriptor;
import com.navercorp.pinpoint.grpc.trace.PJvmGc;
import com.navercorp.pinpoint.grpc.trace.PJvmGcDetailed;
import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import com.navercorp.pinpoint.grpc.trace.PResponseTime;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PTransaction;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogramUtils;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
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

import java.util.List;

/**
 * @author jaehong.kim
 */
public class GrpcStatMessageConverter implements MessageConverter<GeneratedMessageV3> {
    private GrpcThreadDumpMessageConverter threadDumpMessageConverter = new GrpcThreadDumpMessageConverter();
    private GrpcJvmGcTypeMessageConverter jvmGcTypeConverter = new GrpcJvmGcTypeMessageConverter();

    @Override
    public GeneratedMessageV3 toMessage(Object message) {
        if (message instanceof AgentStatMetricSnapshotBatch) {
            final AgentStatMetricSnapshotBatch agentStatMetricSnapshotBatch = (AgentStatMetricSnapshotBatch) message;
            final PAgentStatBatch.Builder agentStatBatchBuilder = PAgentStatBatch.newBuilder();
            // Skip agentId, startTimestamp
            for (AgentStatMetricSnapshot agentStatMetricSnapshot : agentStatMetricSnapshotBatch.getAgentStats()) {
                final PAgentStat agentStat = converAgentStat(agentStatMetricSnapshot);
                agentStatBatchBuilder.addAgentStat(agentStat);
            }
            return agentStatBatchBuilder.build();
        } else if (message instanceof AgentStatMetricSnapshot) {
            final AgentStatMetricSnapshot agentStatMetricSnapshot = (AgentStatMetricSnapshot) message;
            final PAgentStat agentStat = converAgentStat(agentStatMetricSnapshot);
            return agentStat;
        }
        return null;
    }

    private PAgentStat converAgentStat(final AgentStatMetricSnapshot agentStatMetricSnapshot) {
        final PAgentStat.Builder agentStatBuilder = PAgentStat.newBuilder();
        agentStatBuilder.setTimestamp(agentStatMetricSnapshot.getTimestamp());
        agentStatBuilder.setCollectInterval(agentStatMetricSnapshot.getCollectInterval());
        // Skip agent information(agentId, startTimestamp)

        // Metric
        final JvmGcMetricSnapshot jvmGcMetricSnapshot = agentStatMetricSnapshot.getGc();
        if (jvmGcMetricSnapshot != null) {
            final PJvmGc jvmGc = convertJvmGc(jvmGcMetricSnapshot);
            agentStatBuilder.setGc(jvmGc);
        }

        final CpuLoadMetricSnapshot cpuLoadMetricSnapshot = agentStatMetricSnapshot.getCpuLoad();
        if (cpuLoadMetricSnapshot != null) {
            final PCpuLoad cpuLoad = convertCpuLoad(cpuLoadMetricSnapshot);
            agentStatBuilder.setCpuLoad(cpuLoad);
        }

        final TransactionMetricSnapshot transactionMetricSnapshot = agentStatMetricSnapshot.getTransaction();
        if (transactionMetricSnapshot != null) {
            final PTransaction transaction = convertTransaction(transactionMetricSnapshot);
            agentStatBuilder.setTransaction(transaction);
        }
        final ActiveTraceHistogram activeTraceHistogram = agentStatMetricSnapshot.getActiveTrace();
        if (activeTraceHistogram != null) {
            final PActiveTrace activeTrace = convertActiveTrace(activeTraceHistogram);
            agentStatBuilder.setActiveTrace(activeTrace);
        }

        final DataSourceMetricSnapshot dataSourceMetricSnapshot = agentStatMetricSnapshot.getDataSourceList();
        if (dataSourceMetricSnapshot != null) {
            final PDataSourceList dataSourceList = convertDataSourceList(dataSourceMetricSnapshot);
            agentStatBuilder.setDataSourceList(dataSourceList);
        }

        final ResponseTimeValue responseTimeValue = agentStatMetricSnapshot.getResponseTime();
        if (responseTimeValue != null) {
            final PResponseTime responseTime = convertResponseTime(responseTimeValue);
            agentStatBuilder.setResponseTime(responseTime);
        }

        final DeadlockMetricSnapshot deadlockMetricSnapshot = agentStatMetricSnapshot.getDeadlock();
        if (deadlockMetricSnapshot != null) {
            final PDeadlock deadlock = convertDeadlock(deadlockMetricSnapshot);
            agentStatBuilder.setDeadlock(deadlock);
        }

        final FileDescriptorMetricSnapshot fileDescriptorMetricSnapshot = agentStatMetricSnapshot.getFileDescriptor();
        if (fileDescriptorMetricSnapshot != null) {
            final PFileDescriptor fileDescriptor = convertFileDescriptor(fileDescriptorMetricSnapshot);
            agentStatBuilder.setFileDescriptor(fileDescriptor);
        }

        final BufferMetricSnapshot bufferMetricSnapshot = agentStatMetricSnapshot.getDirectBuffer();
        if (bufferMetricSnapshot != null) {
            final PDirectBuffer directBuffer = convertDirectBuffer(bufferMetricSnapshot);
            agentStatBuilder.setDirectBuffer(directBuffer);
        }
        return agentStatBuilder.build();
    }

    private PJvmGc convertJvmGc(JvmGcMetricSnapshot jvmGcMetricSnapshot) {
        final PJvmGc.Builder jvmGcBuilder = PJvmGc.newBuilder();
        jvmGcBuilder.setJvmMemoryHeapMax(jvmGcMetricSnapshot.getJvmMemoryHeapMax());
        jvmGcBuilder.setJvmMemoryHeapUsed(jvmGcMetricSnapshot.getJvmMemoryHeapUsed());
        jvmGcBuilder.setJvmMemoryNonHeapMax(jvmGcMetricSnapshot.getJvmMemoryNonHeapMax());
        jvmGcBuilder.setJvmMemoryNonHeapUsed(jvmGcMetricSnapshot.getJvmMemoryNonHeapUsed());
        jvmGcBuilder.setJvmGcOldCount(jvmGcMetricSnapshot.getJvmGcOldCount());
        jvmGcBuilder.setJvmGcOldTime(jvmGcMetricSnapshot.getJvmGcOldTime());

        final PJvmGcType jvmGcType = this.jvmGcTypeConverter.toMessage(jvmGcMetricSnapshot.getType());
        jvmGcBuilder.setType(jvmGcType);

        if (jvmGcMetricSnapshot.getJvmGcDetailed() != null) {
            final JvmGcDetailedMetricSnapshot jvmGcDetailedMetricSnapshot = jvmGcMetricSnapshot.getJvmGcDetailed();
            final PJvmGcDetailed.Builder jvmGcDetailedBuilder = PJvmGcDetailed.newBuilder();
            jvmGcDetailedBuilder.setJvmPoolNewGenUsed(jvmGcDetailedMetricSnapshot.getJvmPoolNewGenUsed());
            jvmGcDetailedBuilder.setJvmPoolOldGenUsed(jvmGcDetailedMetricSnapshot.getJvmPoolOldGenUsed());
            jvmGcDetailedBuilder.setJvmPoolSurvivorSpaceUsed(jvmGcDetailedMetricSnapshot.getJvmPoolSurvivorSpaceUsed());
            jvmGcDetailedBuilder.setJvmPoolCodeCacheUsed(jvmGcDetailedMetricSnapshot.getJvmPoolCodeCacheUsed());
            jvmGcDetailedBuilder.setJvmPoolPermGenUsed(jvmGcDetailedMetricSnapshot.getJvmPoolPermGenUsed());
            jvmGcDetailedBuilder.setJvmPoolMetaspaceUsed(jvmGcDetailedMetricSnapshot.getJvmPoolMetaspaceUsed());
            jvmGcDetailedBuilder.setJvmGcNewCount(jvmGcDetailedMetricSnapshot.getJvmGcNewCount());
            jvmGcDetailedBuilder.setJvmGcNewTime(jvmGcDetailedMetricSnapshot.getJvmGcNewTime());
            jvmGcBuilder.setJvmGcDetailed(jvmGcDetailedBuilder.build());
        }
        return jvmGcBuilder.build();
    }

    private PCpuLoad convertCpuLoad(CpuLoadMetricSnapshot cpuLoadMetricSnapshot) {
        final PCpuLoad.Builder cpuLoadBuilder = PCpuLoad.newBuilder();
        cpuLoadBuilder.setJvmCpuLoad(cpuLoadMetricSnapshot.getJvmCpuUsage());
        cpuLoadBuilder.setSystemCpuLoad(cpuLoadMetricSnapshot.getSystemCpuUsage());
        return cpuLoadBuilder.build();
    }

    private PTransaction convertTransaction(TransactionMetricSnapshot transactionMetricSnapshot) {
        final PTransaction.Builder transactionBuilder = PTransaction.newBuilder();
        transactionBuilder.setSampledNewCount(transactionMetricSnapshot.getSampledNewCount());
        transactionBuilder.setSampledContinuationCount(transactionMetricSnapshot.getSampledContinuationCount());
        transactionBuilder.setUnsampledNewCount(transactionMetricSnapshot.getUnsampledNewCount());
        transactionBuilder.setUnsampledContinuationCount(transactionMetricSnapshot.getUnsampledContinuationCount());
        transactionBuilder.setSkippedNewCount(transactionMetricSnapshot.getSkippedNewCount());
        transactionBuilder.setSkippedContinuationCount(transactionMetricSnapshot.getSkippedContinuationCount());
        return transactionBuilder.build();
    }

    private PActiveTrace convertActiveTrace(ActiveTraceHistogram activeTraceHistogram) {
        final PActiveTrace.Builder activeTraceBuilder = PActiveTrace.newBuilder();
        final PActiveTraceHistogram.Builder activeTraceHistogramBuilder = PActiveTraceHistogram.newBuilder();
        if (activeTraceHistogram.getHistogramSchema() != null) {
            activeTraceHistogramBuilder.setHistogramSchemaType(activeTraceHistogram.getHistogramSchema().getTypeCode());
        }
        final List<Integer> activeTraceCounts = ActiveTraceHistogramUtils.asList(activeTraceHistogram);
        activeTraceHistogramBuilder.addAllActiveTraceCount(activeTraceCounts);
        activeTraceBuilder.setHistogram(activeTraceHistogramBuilder.build());
        return activeTraceBuilder.build();
    }

    private PDataSourceList convertDataSourceList(DataSourceMetricSnapshot dataSourceMetricSnapshot) {
        final PDataSourceList.Builder dataSourceListBuilder = PDataSourceList.newBuilder();
        for (DataSource dataSourceCollectData : dataSourceMetricSnapshot.getDataSourceList()) {
            PDataSource.Builder dataSourceBuilder = PDataSource.newBuilder();
            dataSourceBuilder.setId(dataSourceCollectData.getId());
            dataSourceBuilder.setServiceTypeCode(dataSourceCollectData.getServiceTypeCode());
            if (dataSourceCollectData.getDatabaseName() != null) {
                dataSourceBuilder.setDatabaseName(dataSourceCollectData.getDatabaseName());
            }
            if (dataSourceCollectData.getActiveConnectionSize() != 0) {
                dataSourceBuilder.setActiveConnectionSize(dataSourceCollectData.getActiveConnectionSize());
            }
            if (dataSourceCollectData.getUrl() != null) {
                dataSourceBuilder.setUrl(dataSourceCollectData.getUrl());
            }
            dataSourceBuilder.setMaxConnectionSize(dataSourceCollectData.getMaxConnectionSize());
            dataSourceListBuilder.addDataSource(dataSourceBuilder.build());
        }
        return dataSourceListBuilder.build();
    }

    private PResponseTime convertResponseTime(ResponseTimeValue responseTimeCollectData) {
        final PResponseTime.Builder responseTime = PResponseTime.newBuilder();
        if (responseTimeCollectData.getAvg() != 0) {
            responseTime.setAvg(responseTimeCollectData.getAvg());
        }
        if (responseTimeCollectData.getMax() != 0) {
            responseTime.setMax(responseTimeCollectData.getMax());
        }
        return responseTime.build();
    }

    private PDeadlock convertDeadlock(DeadlockMetricSnapshot deadlockMetricSnapshot) {
        // Only send id values that have already been sent
        final PDeadlock.Builder deadlockBuilder = PDeadlock.newBuilder();
        deadlockBuilder.setCount(deadlockMetricSnapshot.getDeadlockedThreadCount());

        for (ThreadDumpMetricSnapshot threadDumpMetricSnapshot : deadlockMetricSnapshot.getDeadlockedThreadList()) {
            final PThreadDump threadDump = this.threadDumpMessageConverter.toMessage(threadDumpMetricSnapshot);
            if (threadDump != null) {
                deadlockBuilder.addThreadDump(threadDump);
            }
        }
        return deadlockBuilder.build();
    }

    private PFileDescriptor convertFileDescriptor(FileDescriptorMetricSnapshot fileDescriptorCollectData) {
        final PFileDescriptor.Builder fileDescriptorBuilder = PFileDescriptor.newBuilder();
        fileDescriptorBuilder.setOpenFileDescriptorCount(fileDescriptorCollectData.getOpenFileDescriptorCount());
        return fileDescriptorBuilder.build();
    }

    private PDirectBuffer convertDirectBuffer(BufferMetricSnapshot directBufferCollectData) {
        final PDirectBuffer.Builder directBufferBuilder = PDirectBuffer.newBuilder();
        directBufferBuilder.setDirectCount(directBufferCollectData.getDirectCount());
        directBufferBuilder.setDirectMemoryUsed(directBufferCollectData.getDirectMemoryUsed());
        directBufferBuilder.setMappedCount(directBufferCollectData.getMappedCount());
        directBufferBuilder.setMappedMemoryUsed(directBufferCollectData.getMappedMemoryUsed());
        return directBufferBuilder.build();
    }
}