/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.collector.model.kafka;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.commons.math3.util.Precision;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) tenantId must be entered
public class AgentStatModelConverter {

    public static final String DATASOUCE_TAG_ID_KEY = "id";
    public static final String DATASOUCE_TAG_SERVICE_TYPE_CODE_KEY = "serviceTypeCode";
    public static final String DATASOUCE_TAG_DATABASE_NAME_KEY = "databaseName";
    public static final String DATASOUCE_TAG_JDBC_URL_KEY = "jdbcUrl";

    public List<AgentStat> convertCpuLoad(List<CpuLoadBo> cpuLoadBoList, String tenantId) {
        List<AgentStat> agentStatList = cpuLoadBoList.stream()
                .flatMap(cpuLoadBo -> {
                    final AgentStatBuilder builder = new AgentStatBuilder(tenantId, cpuLoadBo);

                    AgentStat jvmCpuLoad = builder.build(AgentStatField.CPU_LOAD_JVM,
                            cpuLoadBo.getJvmCpuLoad());
                    AgentStat systemCpuLoad = builder.build(
                            AgentStatField.CPU_LOAD_SYSTEM,
                            cpuLoadBo.getSystemCpuLoad());

                    return Stream.of(jvmCpuLoad, systemCpuLoad);
                })
                .collect(Collectors.toList());

        return agentStatList;
    }

    public List<AgentStat> convertActiveTrace(List<ActiveTraceBo> activeTraceBoList, String tenantId) {
        List<AgentStat> agentStatList = activeTraceBoList.stream()
                .flatMap(activeTraceBo -> {
                    final AgentStatBuilder builder = new AgentStatBuilder(tenantId, activeTraceBo);

                    AgentStat fastCount = builder.build(
                            AgentStatField.ACTIVE_TRACE_FAST_COUNT,
                            activeTraceBo.getActiveTraceHistogram().getFastCount());
                    AgentStat normalCount = builder.build(
                            AgentStatField.ACTIVE_TRACE_NORNAL_COUNT,
                            activeTraceBo.getActiveTraceHistogram().getNormalCount());
                    AgentStat slowCount = builder.build(
                            AgentStatField.ACTIVE_TRACE_SLOW_COUNT,
                            activeTraceBo.getActiveTraceHistogram().getSlowCount());
                    AgentStat verySlowCount = builder.build(
                            AgentStatField.ACTIVE_TRACE_VERY_SLOW_COUNT,
                            activeTraceBo.getActiveTraceHistogram().getVerySlowCount());

                    double calculatedTotalCount = activeTraceBo.getActiveTraceHistogram().getFastCount() +
                            activeTraceBo.getActiveTraceHistogram().getNormalCount() +
                            activeTraceBo.getActiveTraceHistogram().getSlowCount() +
                            activeTraceBo.getActiveTraceHistogram().getVerySlowCount();
                    AgentStat totalCount = builder.build(
                            AgentStatField.ACTIVE_TRACE_TOTAL_COUNT,
                            calculatedTotalCount);
                    return Stream.of(fastCount, normalCount, slowCount, verySlowCount, totalCount);
                })
                .collect(Collectors.toList());

        return agentStatList;
    }

    public List<AgentStat> convertJvmGc(List<JvmGcBo> jvmGcBoList, String tenantId) {
        List<AgentStat> agentStatList = jvmGcBoList.stream()
                .flatMap(jvmGcBo -> {
                    final AgentStatBuilder builder = new AgentStatBuilder(tenantId, jvmGcBo);
                    AgentStat gcType = builder.build(
                            AgentStatField.JVM_GC_TYPE,
                            jvmGcBo.getGcType().getTypeCode());
                    AgentStat heapUsed = builder.build(
                            AgentStatField.JVM_GC_HEAP_USED,
                            jvmGcBo.getHeapUsed());
                    AgentStat heapMax = builder.build(
                            AgentStatField.JVM_GC_HEAP_MAX,
                            jvmGcBo.getHeapMax());
                    AgentStat nonHeapUsed = builder.build(
                            AgentStatField.JVM_GC_NONHEAP_USED,
                            jvmGcBo.getNonHeapUsed());
                    AgentStat nonHeapMax = builder.build(
                            AgentStatField.JVM_GC_NONHEAP_MAX,
                            jvmGcBo.getNonHeapMax());
                    AgentStat gcOldCount = builder.build(
                            AgentStatField.JVM_GC_NONHEAP_GC_OLD_COUNT,
                            jvmGcBo.getGcOldCount());
                    AgentStat gcOldTime = builder.build(
                            AgentStatField.JVM_GC_NONHEAP_GC_OLD_TIME,
                            jvmGcBo.getGcOldTime());
                    return Stream.of(gcType, heapUsed, heapMax, nonHeapUsed,
                            nonHeapMax, gcOldCount, gcOldTime);
                })
                .collect(Collectors.toList());

        return agentStatList;
    }


    public List<AgentStat> convertJvmGCDetailed(List<JvmGcDetailedBo> jvmGcDetailedBoList, String tenantId) {
        List<AgentStat> agentStatList = jvmGcDetailedBoList
                .stream()
                .flatMap(jvmGcDetailedBo -> {
                            final AgentStatBuilder builder = new AgentStatBuilder(tenantId, jvmGcDetailedBo);
                            AgentStat newGcCount = builder.build(
                                    AgentStatField.JVM_GC_DETAILED_GC_NEW_COUNT,
                                    jvmGcDetailedBo.getGcNewCount());
                            AgentStat newGcTime = builder.build(
                                    AgentStatField.JVM_GC_DETAILED_GC_NEW_TIME,
                                    jvmGcDetailedBo.getGcNewTime());
                            AgentStat codeCacheUsed = builder.build(
                                    AgentStatField.JVM_GC_DETAILED_CODE_CACHE_USED,
                                    jvmGcDetailedBo.getCodeCacheUsed());
                            AgentStat newGenUsed = builder.build(
                                    AgentStatField.JVM_GC_DETAILED_NEW_GEN_USED,
                                    jvmGcDetailedBo.getNewGenUsed());
                            AgentStat oldGenUsed = builder.build(
                                    AgentStatField.JVM_GC_DETAILED_OLD_GEN_USED,
                                    jvmGcDetailedBo.getOldGenUsed());
                            AgentStat survivorSpaceUsed = builder.build(
                                    AgentStatField.JVM_GC_DETAILED_SURVIVOR_SPACE_USED,
                                    jvmGcDetailedBo.getSurvivorSpaceUsed());
                            AgentStat permGenUsed = builder.build(
                                    AgentStatField.JVM_GC_DETAILED_PERM_GEN_USED,
                                    jvmGcDetailedBo.getPermGenUsed());
                            AgentStat metaspaceUsed = builder.build(
                                    AgentStatField.JVM_GC_DETAILED_METASPACE_USED,
                                    jvmGcDetailedBo.getMetaspaceUsed());

                            return Stream.of(newGcCount, newGcTime, codeCacheUsed,
                                    newGenUsed, oldGenUsed, survivorSpaceUsed,
                                    permGenUsed, metaspaceUsed);
                        }
                )
                .collect(Collectors.toList());

        return agentStatList;
    }

    public List<AgentStat> convertTransaction(List<TransactionBo> transactionBoList, String tenantId) {
        List<AgentStat> agentStatList = transactionBoList.stream()
                .flatMap(transactionBo -> {
                            final AgentStatBuilder builder = new AgentStatBuilder(tenantId, transactionBo);
                            AgentStat collectInterval = builder.build(
                                    AgentStatField.TRANSACTION_COLLECT_INTERVAL,
                                    transactionBo.getCollectInterval());
                            AgentStat sampledNewCount = builder.build(
                                    AgentStatField.TRANSACTION_SAMPLED_NEW_COUNT,
                                    transactionBo.getSampledNewCount());
                            AgentStat sampledContinuationCount = builder.build(
                                    AgentStatField.TRANSACTION_SAMPLED_CONTINUATION_COUNT,
                                    transactionBo.getSampledContinuationCount());
                            AgentStat unsampledNewCount = builder.build(
                                    AgentStatField.TRANSACTION_UNSAMPLED_NEW_COUNT,
                                    transactionBo.getUnsampledNewCount());
                            AgentStat unsampledContinuationCount = builder.build(
                                    AgentStatField.TRANSACTION_UNSAMPLED_CONTINUATION_COUNT,
                                    transactionBo.getUnsampledContinuationCount());
                            AgentStat skippedNewSkipCount = builder.build(
                                    AgentStatField.TRANSACTION_SKIPPED_NEW_SKIP_COUNT,
                                    transactionBo.getSkippedNewSkipCount());
                            AgentStat skippedContinuationCount = builder.build(
                                    AgentStatField.TRANSACTION_SKIPPED_CONTINUATION_COUNT,
                                    transactionBo.getSkippedContinuationCount());

                            double calculatedTotal = transactionBo.getSampledNewCount() + transactionBo.getSampledContinuationCount() +
                                    transactionBo.getUnsampledNewCount() + transactionBo.getUnsampledContinuationCount() +
                                    transactionBo.getSkippedNewSkipCount() + transactionBo.getSkippedContinuationCount();
                            AgentStat totalCount = builder.build(
                                    AgentStatField.TRANSACTION_TOTAL_COUNT,
                                    calculatedTotal);

                            double calculatedTotalCountPerMs = Precision.round(calculatedTotal / (transactionBo.getCollectInterval() / 1000D), 1);
                            AgentStat totalCountPerMs = builder.build(
                                    AgentStatField.TRANSACTION_TOTAL_COUNT_PER_MS,
                                    calculatedTotalCountPerMs);

                            return Stream.of(collectInterval, sampledNewCount,
                                    sampledContinuationCount, unsampledNewCount,
                                    unsampledContinuationCount, skippedNewSkipCount, skippedContinuationCount, totalCount, totalCountPerMs);
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }

    public List<AgentStat> convertResponseTime(List<ResponseTimeBo> reponseTimeBoList, String tenantId) {
        List<AgentStat> agentStatList = reponseTimeBoList.stream()
                .flatMap(responseTimeBo -> {
                            final AgentStatBuilder builder = new AgentStatBuilder(tenantId, responseTimeBo);

                            AgentStat avg = builder.build(
                                    AgentStatField.RESPONSE_TIME_AVG,
                                    responseTimeBo.getAvg());
                            AgentStat max = builder.build(
                                    AgentStatField.RESPONSE_TIME_MAX,
                                    responseTimeBo.getMax());

                            return Stream.of(avg, max);
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }

    public List<AgentStat> convertDeadlockThreadCount(List<DeadlockThreadCountBo> deadlockThreadCountBoList, String tenantId) {
        List<AgentStat> agentStatList = deadlockThreadCountBoList.stream()
                .flatMap(deadlockThreadCountBo -> {
                            final AgentStatBuilder builder = new AgentStatBuilder(tenantId, deadlockThreadCountBo);

                            AgentStat deadlockedThreadCount = builder.build(
                                    AgentStatField.DEADLOCK_THREAD_COUNT,
                                    deadlockThreadCountBo.getDeadlockedThreadCount());

                            return Stream.of(deadlockedThreadCount);
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }

    public List<AgentStat> convertFileDescriptor(List<FileDescriptorBo> fileDescriptorBoList, String tenantId) {
        List<AgentStat> agentStatList = fileDescriptorBoList.stream()
                .flatMap(fileDescriptorBo -> {
                            final AgentStatBuilder builder = new AgentStatBuilder(tenantId, fileDescriptorBo);
                            AgentStat openFileDescriptorCount = builder.build(
                                    AgentStatField.OPEN_FILE_DESCRIPTOR_COUNT,
                                    fileDescriptorBo.getOpenFileDescriptorCount());

                            return Stream.of(openFileDescriptorCount);
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }

    public List<AgentStat> convertDirectBuffer(List<DirectBufferBo> directBufferBoList, String tenantId) {
        List<AgentStat> agentStatList = directBufferBoList.stream()
                .flatMap(directBufferBo -> {
                            final AgentStatBuilder builder = new AgentStatBuilder(tenantId, directBufferBo);

                            AgentStat directCount = builder.build(
                                    AgentStatField.DIRECT_BUFFER_DIRECT_COUNT,
                                    directBufferBo.getDirectCount());
                            AgentStat directMemoryUsed = builder.build(
                                    AgentStatField.DIRECT_BUFFER_DIRECT_MEMORY_USED,
                                    directBufferBo.getDirectMemoryUsed());
                            AgentStat mappedCount = builder.build(
                                    AgentStatField.DIRECT_BUFFER_MAPPED_COUNT,
                                    directBufferBo.getMappedCount());
                            AgentStat mappedMemoryUsed = builder.build(
                                    AgentStatField.DIRECT_BUFFER_MAPPED_MEMORY_USED,
                                    directBufferBo.getMappedMemoryUsed());

                            return Stream.of(directCount, directMemoryUsed, mappedCount, mappedMemoryUsed);
                        }
                )
                .collect(Collectors.toList());

        return agentStatList;
    }

    public List<AgentStat> convertTotalThreadCount(List<TotalThreadCountBo> totalThreadCountBoList, String tenantId) {
        List<AgentStat> agentStatList = totalThreadCountBoList.stream()
                .flatMap(totalThreadCountBo -> {
                            final AgentStatBuilder builder = new AgentStatBuilder(tenantId, totalThreadCountBo);

                            AgentStat totalThreadCount = builder.build(
                                    AgentStatField.TOTAL_THREAD_COUNT,
                                    totalThreadCountBo.getTotalThreadCount());

                            return Stream.of(totalThreadCount);
                        }
                )
                .collect(Collectors.toList());

        return agentStatList;
    }


    public List<AgentStat> convertLoadedClass(List<LoadedClassBo> loadedClassBoList, String tenantId) {
        List<AgentStat> agentStatList = loadedClassBoList.stream()
                .flatMap(loadedClassBo -> {
                            final AgentStatBuilder builder = new AgentStatBuilder(tenantId, loadedClassBo);
                            AgentStat loadedClassCount = builder.build(
                                    AgentStatField.CLASS_COUNT_LOAD,
                                    loadedClassBo.getLoadedClassCount());
                            AgentStat unloadedClassCount = builder.build(
                                    AgentStatField.CLASS_COUNT_UN_LOADED,
                                    loadedClassBo.getUnloadedClassCount());

                            return Stream.of(loadedClassCount, unloadedClassCount);
                        }
                )
                .collect(Collectors.toList());

        return agentStatList;
    }

    public List<AgentStat> convertDataSource(List<DataSourceListBo> dataSourceListBoList, String tenantId) {
        List<AgentStat> agentStatList = dataSourceListBoList.stream()
                .flatMap(dataSourceListBo -> {
                            final Stream.Builder<AgentStat> builder = Stream.builder();
                            final AgentStatBuilder statBuilder = new AgentStatBuilder(tenantId, dataSourceListBo);
                            for (DataSourceBo dataSourceBo : dataSourceListBo.getList()) {
                                List<Tag> tags = List.of(
                                        new Tag(DATASOUCE_TAG_ID_KEY, String.valueOf(dataSourceBo.getId())),
                                        new Tag(DATASOUCE_TAG_SERVICE_TYPE_CODE_KEY, String.valueOf(dataSourceBo.getServiceTypeCode())),
                                        new Tag(DATASOUCE_TAG_DATABASE_NAME_KEY, dataSourceBo.getDatabaseName()),
                                        new Tag(DATASOUCE_TAG_JDBC_URL_KEY, dataSourceBo.getJdbcUrl())
                                );

                                AgentStat activeConnectionSize = statBuilder.build(
                                        AgentStatField.DATASOURCE_ACTIVE_CONNECTION_SIZE,
                                        dataSourceBo.getActiveConnectionSize(), tags);

                                AgentStat maxConnectionSize = statBuilder.build(
                                        AgentStatField.DATASOURCE_MAX_CONNECTION_SIZE,
                                        dataSourceBo.getMaxConnectionSize(), tags);

                                builder.add(activeConnectionSize);
                                builder.add(maxConnectionSize);
                            }

                            return builder.build();
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }


}
