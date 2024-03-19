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

import com.navercorp.pinpoint.common.model.SortKeyUtils;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
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
public class AgentStatModelConverter<T extends AgentStatDataPoint> {

    public static List<AgentStat> convertCpuLoadToAgentStat(List<CpuLoadBo> cpuLoadBoList, String tenantId) {
        List<AgentStat> agentStatList = cpuLoadBoList.stream()
                .flatMap(cpuLoadBo -> {
                    String sortKey = SortKeyUtils.generateKey(cpuLoadBo.getApplicationName(), cpuLoadBo.getAgentId(), AgentStatType.CPU_LOAD.getChartType());
                    AgentStat jvmCpuLoad = new AgentStat(tenantId, sortKey, cpuLoadBo.getApplicationName(), cpuLoadBo.getAgentId(),
                            AgentStatType.CPU_LOAD.getChartType(), AgentStatField.CPU_LOAD_JVM.getFieldName(),
                            cpuLoadBo.getJvmCpuLoad(), cpuLoadBo.getTimestamp());
                    AgentStat systemCpuLoad = new AgentStat(tenantId, sortKey, cpuLoadBo.getApplicationName(), cpuLoadBo.getAgentId(),
                            AgentStatType.CPU_LOAD.getChartType(), AgentStatField.CPU_LOAD_SYSTEM.getFieldName(),
                            cpuLoadBo.getSystemCpuLoad(), cpuLoadBo.getTimestamp());

                    return Stream.of(jvmCpuLoad, systemCpuLoad);
                })
                .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertActiveTraceToAgentStat(List<ActiveTraceBo> activeTraceBoList, String tenantId) {
        List<AgentStat> agentStatList = activeTraceBoList.stream()
                .flatMap(activeTraceBo -> {
                    String sortKey = SortKeyUtils.generateKey(activeTraceBo.getApplicationName(), activeTraceBo.getAgentId(), AgentStatType.ACTIVE_TRACE.getChartType());
                    AgentStat fastCount = new AgentStat(tenantId, sortKey, activeTraceBo.getApplicationName(), activeTraceBo.getAgentId(),
                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_FAST_COUNT.getFieldName(),
                            activeTraceBo.getActiveTraceHistogram().getFastCount(), activeTraceBo.getTimestamp());
                    AgentStat normalCount = new AgentStat(tenantId, sortKey, activeTraceBo.getApplicationName(), activeTraceBo.getAgentId(),
                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_NORNAL_COUNT.getFieldName(),
                            activeTraceBo.getActiveTraceHistogram().getNormalCount(), activeTraceBo.getTimestamp());
                    AgentStat slowCount = new AgentStat(tenantId, sortKey, activeTraceBo.getApplicationName(), activeTraceBo.getAgentId(),
                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_SLOW_COUNT.getFieldName(),
                            activeTraceBo.getActiveTraceHistogram().getSlowCount(), activeTraceBo.getTimestamp());
                    AgentStat verySlowCount = new AgentStat(tenantId, sortKey, activeTraceBo.getApplicationName(), activeTraceBo.getAgentId(),
                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_VERY_SLOW_COUNT.getFieldName(),
                            activeTraceBo.getActiveTraceHistogram().getVerySlowCount(), activeTraceBo.getTimestamp());

                    double calculatedTotalCount = activeTraceBo.getActiveTraceHistogram().getFastCount() +
                                                    activeTraceBo.getActiveTraceHistogram().getNormalCount() +
                                                    activeTraceBo.getActiveTraceHistogram().getSlowCount() +
                                                    activeTraceBo.getActiveTraceHistogram().getVerySlowCount();
                    AgentStat totalCount = new AgentStat(tenantId, sortKey, activeTraceBo.getApplicationName(), activeTraceBo.getAgentId(),
                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_TOTAL_COUNT.getFieldName(),
                            calculatedTotalCount, activeTraceBo.getTimestamp());
                    return Stream.of(fastCount, normalCount, slowCount, verySlowCount, totalCount);
                })
                .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertJvmGcToAgentStat(List<JvmGcBo> jvmGcBoList, String tenantId) {
        List<AgentStat> agentStatList = jvmGcBoList.stream()
                .flatMap(jvmGcBo -> {
                    String sortKey = SortKeyUtils.generateKey(jvmGcBo.getApplicationName(), jvmGcBo.getAgentId(), AgentStatType.JVM_GC.getChartType());
                    AgentStat gcType = new AgentStat(tenantId, sortKey, jvmGcBo.getApplicationName(), jvmGcBo.getAgentId(),
                            AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_TYPE.getFieldName(),
                            jvmGcBo.getGcType().getTypeCode(), jvmGcBo.getTimestamp());
                    AgentStat heapUsed = new AgentStat(tenantId, sortKey, jvmGcBo.getApplicationName(), jvmGcBo.getAgentId(),
                            AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_HEAP_USED.getFieldName(),
                            jvmGcBo.getHeapUsed(), jvmGcBo.getTimestamp());
                    AgentStat heapMax = new AgentStat(tenantId, sortKey, jvmGcBo.getApplicationName(), jvmGcBo.getAgentId(),
                            AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_HEAP_MAX.getFieldName(),
                            jvmGcBo.getHeapMax(), jvmGcBo.getTimestamp());
                    AgentStat nonHeapUsed = new AgentStat(tenantId, sortKey, jvmGcBo.getApplicationName(), jvmGcBo.getAgentId(),
                            AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_NONHEAP_USED.getFieldName(),
                            jvmGcBo.getNonHeapUsed(), jvmGcBo.getTimestamp());
                    AgentStat nonHeapMax = new AgentStat(tenantId, sortKey, jvmGcBo.getApplicationName(), jvmGcBo.getAgentId(),
                            AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_NONHEAP_MAX.getFieldName(),
                            jvmGcBo.getNonHeapMax(), jvmGcBo.getTimestamp());
                    AgentStat gcOldCount = new AgentStat(tenantId, sortKey, jvmGcBo.getApplicationName(), jvmGcBo.getAgentId(),
                            AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_NONHEAP_GC_OLD_COUNT.getFieldName(),
                            jvmGcBo.getGcOldCount(), jvmGcBo.getTimestamp());
                    AgentStat gcOldTime = new AgentStat(tenantId, sortKey, jvmGcBo.getApplicationName(), jvmGcBo.getAgentId(),
                            AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_NONHEAP_GC_OLD_TIME.getFieldName(),
                            jvmGcBo.getGcOldTime(), jvmGcBo.getTimestamp());
                    return Stream.of(gcType, heapUsed, heapMax, nonHeapUsed,
                            nonHeapMax, gcOldCount, gcOldTime);
                })
                .collect(Collectors.toList());

        return agentStatList;
    }


    public static List<AgentStat> convertJvmGCDetailedToAgentStat(List<JvmGcDetailedBo> jvmGcDetailedBoList, String tenantId) {
        List<AgentStat> agentStatList = jvmGcDetailedBoList
                .stream()
                .flatMap(jvmGcDetailedBo -> {
                            String sortKey = SortKeyUtils.generateKey(jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(), AgentStatType.JVM_GC_DETAILED.getChartType());
                            AgentStat newGcCount = new AgentStat(tenantId, sortKey, jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(),
                                    AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_GC_NEW_COUNT.getFieldName(),
                                    jvmGcDetailedBo.getGcNewCount(), jvmGcDetailedBo.getTimestamp());
                            AgentStat newGcTime = new AgentStat(tenantId, sortKey, jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(),
                                    AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_GC_NEW_TIME.getFieldName(),
                                    jvmGcDetailedBo.getGcNewTime(), jvmGcDetailedBo.getTimestamp());
                            AgentStat codeCacheUsed = new AgentStat(tenantId, sortKey, jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(),
                                    AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_CODE_CACHE_USED.getFieldName(),
                                    jvmGcDetailedBo.getCodeCacheUsed(), jvmGcDetailedBo.getTimestamp());
                            AgentStat newGenUsed = new AgentStat(tenantId, sortKey, jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(),
                                    AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_NEW_GEN_USED.getFieldName(),
                                    jvmGcDetailedBo.getNewGenUsed(), jvmGcDetailedBo.getTimestamp());
                            AgentStat oldGenUsed = new AgentStat(tenantId, sortKey, jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(),
                                    AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_OLD_GEN_USED.getFieldName(),
                                    jvmGcDetailedBo.getOldGenUsed(), jvmGcDetailedBo.getTimestamp());
                            AgentStat survivorSpaceUsed = new AgentStat(tenantId, sortKey, jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(),
                                    AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_SURVIVOR_SPACE_USED.getFieldName(),
                                    jvmGcDetailedBo.getSurvivorSpaceUsed(), jvmGcDetailedBo.getTimestamp());
                            AgentStat permGenUsed = new AgentStat(tenantId, sortKey, jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(),
                                    AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_PERM_GEN_USED.getFieldName(),
                                    jvmGcDetailedBo.getPermGenUsed(), jvmGcDetailedBo.getTimestamp());
                            AgentStat metaspaceUsed = new AgentStat(tenantId, sortKey, jvmGcDetailedBo.getApplicationName(), jvmGcDetailedBo.getAgentId(),
                                    AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_METASPACE_USED.getFieldName(),
                                    jvmGcDetailedBo.getMetaspaceUsed(), jvmGcDetailedBo.getTimestamp());

                            return Stream.of(newGcCount, newGcTime, codeCacheUsed,
                                    newGenUsed, oldGenUsed, survivorSpaceUsed,
                                    permGenUsed, metaspaceUsed);
                        }
                )
                .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertTransactionToAgentStat(List<TransactionBo> transactionBoList, String tenantId) {
        List<AgentStat> agentStatList = transactionBoList.stream()
                .flatMap(transactionBo -> {
                            String sortKey = SortKeyUtils.generateKey(transactionBo.getApplicationName(), transactionBo.getAgentId(), AgentStatType.TRANSACTION.getChartType());
                            AgentStat collectInterval = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_COLLECT_INTERVAL.getFieldName(),
                                    transactionBo.getCollectInterval(), transactionBo.getTimestamp());
                            AgentStat sampledNewCount = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_SAMPLED_NEW_COUNT.getFieldName(),
                                    transactionBo.getSampledNewCount(), transactionBo.getTimestamp());
                            AgentStat sampledContinuationCount = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_SAMPLED_CONTINUATION_COUNT.getFieldName(),
                                    transactionBo.getSampledContinuationCount(), transactionBo.getTimestamp());
                            AgentStat unsampledNewCount = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_UNSAMPLED_NEW_COUNT.getFieldName(),
                                    transactionBo.getUnsampledNewCount(), transactionBo.getTimestamp());
                            AgentStat unsampledContinuationCount = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_UNSAMPLED_CONTINUATION_COUNT.getFieldName(),
                                    transactionBo.getUnsampledContinuationCount(), transactionBo.getTimestamp());
                            AgentStat skippedNewSkipCount = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_SKIPPED_NEW_SKIP_COUNT.getFieldName(),
                                    transactionBo.getSkippedNewSkipCount(), transactionBo.getTimestamp());
                            AgentStat skippedContinuationCount = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_SKIPPED_CONTINUATION_COUNT.getFieldName(),
                                    transactionBo.getSkippedContinuationCount(), transactionBo.getTimestamp());

                            double calculatedTotal = transactionBo.getSampledNewCount() + transactionBo.getSampledContinuationCount() +
                                                    transactionBo.getUnsampledNewCount() + transactionBo.getUnsampledContinuationCount() +
                                                    transactionBo.getSkippedNewSkipCount() + transactionBo.getSkippedContinuationCount();
                            AgentStat totalCount = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_TOTAL_COUNT.getFieldName(),
                                    calculatedTotal, transactionBo.getTimestamp());

                            double calculatedTotalCountPerMs = Precision.round(calculatedTotal / (transactionBo.getCollectInterval() / 1000D), 1);
                            AgentStat totalCountPerMs = new AgentStat(tenantId, sortKey, transactionBo.getApplicationName(), transactionBo.getAgentId(),
                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_TOTAL_COUNT_PER_MS.getFieldName(),
                                    calculatedTotalCountPerMs, transactionBo.getTimestamp());

                            return Stream.of(collectInterval, sampledNewCount,
                                    sampledContinuationCount, unsampledNewCount,
                                    unsampledContinuationCount, skippedNewSkipCount, skippedContinuationCount, totalCount, totalCountPerMs);
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }

    public static List<AgentStat> convertResponseTimeToAgentStat(List<ResponseTimeBo> reponseTimeBoList, String tenantId) {
        List<AgentStat> agentStatList = reponseTimeBoList.stream()
                .flatMap(responseTimeBo -> {
                            String sortKey = SortKeyUtils.generateKey(responseTimeBo.getApplicationName(), responseTimeBo.getAgentId(), AgentStatType.RESPONSE_TIME.getChartType());
                            AgentStat avg = new AgentStat(tenantId, sortKey, responseTimeBo.getApplicationName(), responseTimeBo.getAgentId(),
                                    AgentStatType.RESPONSE_TIME.getChartType(), AgentStatField.RESPONSE_TIME_AVG.getFieldName(),
                                    responseTimeBo.getAvg(), responseTimeBo.getTimestamp());
                            AgentStat max = new AgentStat(tenantId, sortKey, responseTimeBo.getApplicationName(), responseTimeBo.getAgentId(),
                                    AgentStatType.RESPONSE_TIME.getChartType(), AgentStatField.RESPONSE_TIME_MAX.getFieldName(),
                                    responseTimeBo.getMax(), responseTimeBo.getTimestamp());

                            return Stream.of(avg, max);
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }

    public static List<AgentStat> convertDeadlockThreadCountToAgentStat(List<DeadlockThreadCountBo> deadlockThreadCountBoList, String tenantId) {
        List<AgentStat> agentStatList = deadlockThreadCountBoList.stream()
                .flatMap(deadlockThreadCountBo -> {
                            String sortKey = SortKeyUtils.generateKey(deadlockThreadCountBo.getApplicationName(), deadlockThreadCountBo.getAgentId(), AgentStatType.DEADLOCK.getChartType());
                            AgentStat deadlockedThreadCount = new AgentStat(tenantId, sortKey, deadlockThreadCountBo.getApplicationName(), deadlockThreadCountBo.getAgentId(),
                                    AgentStatType.DEADLOCK.getChartType(), AgentStatField.DEADLOCK_THREAD_COUNT.getFieldName(),
                                    deadlockThreadCountBo.getDeadlockedThreadCount(), deadlockThreadCountBo.getTimestamp());

                            return Stream.of(deadlockedThreadCount);
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }

    public static List<AgentStat> convertFileDescriptorToAgentStat(List<FileDescriptorBo> fileDescriptorBoList, String tenantId) {
        List<AgentStat> agentStatList = fileDescriptorBoList.stream()
                .flatMap(fileDescriptorBo -> {
                            String sortKey = SortKeyUtils.generateKey(fileDescriptorBo.getApplicationName(), fileDescriptorBo.getAgentId(), AgentStatType.FILE_DESCRIPTOR.getChartType());
                            AgentStat openFileDescriptorCount = new AgentStat(tenantId, sortKey, fileDescriptorBo.getApplicationName(), fileDescriptorBo.getAgentId(),
                                    AgentStatType.FILE_DESCRIPTOR.getChartType(), AgentStatField.OPEN_FILE_DESCRIPTOR_COUNT.getFieldName(),
                                    fileDescriptorBo.getOpenFileDescriptorCount(), fileDescriptorBo.getTimestamp());

                            return Stream.of(openFileDescriptorCount);
                        }
                )
                .collect(Collectors.toList());
        return agentStatList;
    }

    public static List<AgentStat> convertDirectBufferToAgentStat(List<DirectBufferBo> directBufferBoList, String tenantId) {
        List<AgentStat> agentStatList = directBufferBoList.stream()
                .flatMap(directBufferBo -> {
                            String sortKey = SortKeyUtils.generateKey(directBufferBo.getApplicationName(), directBufferBo.getAgentId(), AgentStatType.DIRECT_BUFFER.getChartType());
                            AgentStat directCount = new AgentStat(tenantId, sortKey, directBufferBo.getApplicationName(), directBufferBo.getAgentId(),
                                    AgentStatType.DIRECT_BUFFER.getChartType(), AgentStatField.DIRECT_BUFFER_DIRECT_COUNT.getFieldName(),
                                    directBufferBo.getDirectCount(), directBufferBo.getTimestamp());
                            AgentStat directMemoryUsed = new AgentStat(tenantId, sortKey, directBufferBo.getApplicationName(), directBufferBo.getAgentId(),
                                    AgentStatType.DIRECT_BUFFER.getChartType(), AgentStatField.DIRECT_BUFFER_DIRECT_MEMORY_USED.getFieldName(),
                                    directBufferBo.getDirectMemoryUsed(), directBufferBo.getTimestamp());
                            AgentStat mappedCount = new AgentStat(tenantId, sortKey, directBufferBo.getApplicationName(), directBufferBo.getAgentId(),
                                    AgentStatType.DIRECT_BUFFER.getChartType(), AgentStatField.DIRECT_BUFFER_MAPPED_COUNT.getFieldName(),
                                    directBufferBo.getMappedCount(), directBufferBo.getTimestamp());
                            AgentStat mappedMemoryUsed = new AgentStat(tenantId, sortKey, directBufferBo.getApplicationName(), directBufferBo.getAgentId(),
                                    AgentStatType.DIRECT_BUFFER.getChartType(), AgentStatField.DIRECT_BUFFER_MAPPED_MEMORY_USED.getFieldName(),
                                    directBufferBo.getMappedMemoryUsed(), directBufferBo.getTimestamp());

                            return Stream.of(directCount, directMemoryUsed, mappedCount, mappedMemoryUsed);
                        }
                )
                .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertTotalThreadCountToAgentStat(List<TotalThreadCountBo> totalThreadCountBoList, String tenantId) {
        List<AgentStat> agentStatList = totalThreadCountBoList.stream()
                .flatMap(totalThreadCountBo -> {
                            String sortKey = SortKeyUtils.generateKey(totalThreadCountBo.getApplicationName(), totalThreadCountBo.getAgentId(), AgentStatType.TOTAL_THREAD.getChartType());
                            AgentStat totalThreadCount = new AgentStat(tenantId, sortKey, totalThreadCountBo.getApplicationName(), totalThreadCountBo.getAgentId(),
                                    AgentStatType.TOTAL_THREAD.getChartType(), AgentStatField.TOTAL_THREAD_COUNT.getFieldName(),
                                    totalThreadCountBo.getTotalThreadCount(), totalThreadCountBo.getTimestamp());

                            return Stream.of(totalThreadCount);
                        }
                )
                .collect(Collectors.toList());

        return agentStatList;
    }


    public static List<AgentStat> convertLoadedClassToAgentStat(List<LoadedClassBo> loadedClassBoList, String tenantId) {
        List<AgentStat> agentStatList = loadedClassBoList.stream()
                .flatMap(loadedClassBo -> {
                            String sortKey = SortKeyUtils.generateKey(loadedClassBo.getApplicationName(), loadedClassBo.getAgentId(), AgentStatType.LOADED_CLASS.getChartType());
                            AgentStat loadedClassCount = new AgentStat(tenantId, sortKey, loadedClassBo.getApplicationName(), loadedClassBo.getAgentId(),
                                    AgentStatType.LOADED_CLASS.getChartType(), AgentStatField.CLASS_COUNT_LOAD.getFieldName(),
                                    loadedClassBo.getLoadedClassCount(), loadedClassBo.getTimestamp());
                            AgentStat unloadedClassCount = new AgentStat(tenantId, sortKey, loadedClassBo.getApplicationName(), loadedClassBo.getAgentId(),
                                    AgentStatType.LOADED_CLASS.getChartType(), AgentStatField.CLASS_COUNT_UN_LOADED.getFieldName(),
                                    loadedClassBo.getUnloadedClassCount(), loadedClassBo.getTimestamp());

                            return Stream.of(loadedClassCount, unloadedClassCount);
                        }
                )
                .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertDataSourceToAgentStat(List<DataSourceListBo> dataSourceListBoList, String tenantId) {
        List<AgentStat> agentStatList = dataSourceListBoList.stream()
                .flatMap(dataSourceListBo -> {
                            String sortKey = SortKeyUtils.generateKey(dataSourceListBo.getApplicationName(), dataSourceListBo.getAgentId(), AgentStatType.DATASOURCE.getChartType());
                            Stream.Builder<AgentStat> builder = Stream.builder();

                            for (DataSourceBo dataSourceBo : dataSourceListBo.getList()) {
                                List<Tag> tags = List.of(
                                    new Tag("id", String.valueOf(dataSourceBo.getId())),
                                    new Tag("serviceTypeCode", String.valueOf(dataSourceBo.getServiceTypeCode())),
                                    new Tag("databaseName", dataSourceBo.getDatabaseName()),
                                    new Tag("jdbcUrl", dataSourceBo.getJdbcUrl())
                                );

                                AgentStat activeConnectionSize = new AgentStat(tenantId, sortKey, dataSourceListBo.getApplicationName(), dataSourceListBo.getAgentId(),
                                        AgentStatType.DATASOURCE.getChartType(), AgentStatField.DATASOURCE_ACTIVE_CONNECTION_SIZE.getFieldName(),
                                        dataSourceBo.getActiveConnectionSize(), dataSourceListBo.getTimestamp(), tags, dataSourceBo.getJdbcUrl());

                                AgentStat maxConnectionSize = new AgentStat(tenantId, sortKey, dataSourceListBo.getApplicationName(), dataSourceListBo.getAgentId(),
                                        AgentStatType.DATASOURCE.getChartType(), AgentStatField.DATASOURCE_MAX_CONNECTION_SIZE.getFieldName(),
                                        dataSourceBo.getMaxConnectionSize(), dataSourceListBo.getTimestamp(), tags, dataSourceBo.getJdbcUrl());

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
