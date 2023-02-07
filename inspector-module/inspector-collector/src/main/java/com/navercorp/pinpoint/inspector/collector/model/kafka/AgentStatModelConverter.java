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
import com.navercorp.pinpoint.metric.common.model.json.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) tenantId must be entered
public class AgentStatModelConverter <T extends AgentStatDataPoint> {

    public static List<AgentStat> convertCpuLoadToAgentStat(List<CpuLoadBo> cpuLoadBoList) {
        List<AgentStat> agentStatList = cpuLoadBoList.stream()
                                                     .flatMap(cpuLoadBo -> {
                                                                 List<AgentStat> cpuStatList = new ArrayList<>();
                                                                 AgentStat jvmCpuLoad = new AgentStat("defaultTenantId", "applicationName", cpuLoadBo.getAgentId(),
                                                                                                         AgentStatType.CPU_LOAD.getChartType(), AgentStatField.CPU_LOAD_JVM.getFieldName(),
                                                                                                         cpuLoadBo.getJvmCpuLoad(), cpuLoadBo.getTimestamp());
                                                                 AgentStat systemCpuLoad = new AgentStat("defaultTenantId", "applicationName", cpuLoadBo.getAgentId(),
                                                                                                         AgentStatType.CPU_LOAD.getChartType(), AgentStatField.CPU_LOAD_SYSTEM.getFieldName(),
                                                                                                         cpuLoadBo.getSystemCpuLoad(), cpuLoadBo.getTimestamp());
                                                                 cpuStatList.add(jvmCpuLoad);
                                                                 cpuStatList.add(systemCpuLoad);
                                                                 return cpuStatList.stream();
                                                             })
                                                    .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertActiveTraceToAgentStat(List<ActiveTraceBo> activeTraceBoList) {
        List<AgentStat> agentStatList = activeTraceBoList.stream()
                                                         .flatMap( activeTraceBo -> {
                                                                    List<AgentStat> activeTraceList = new ArrayList<>();
                                                                    AgentStat fastCount = new AgentStat("defaultTenantId", "applicationName", activeTraceBo.getAgentId(),
                                                                                                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_FAST_COUNT.getFieldName(),
                                                                                                            activeTraceBo.getActiveTraceHistogram().getFastCount(), activeTraceBo.getTimestamp());
                                                                    AgentStat normalCount = new AgentStat("defaultTenantId", "applicationName", activeTraceBo.getAgentId(),
                                                                                                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_NORNAL_COUNT.getFieldName(),
                                                                                                            activeTraceBo.getActiveTraceHistogram().getNormalCount(), activeTraceBo.getTimestamp());
                                                                    AgentStat slowCount = new AgentStat("defaultTenantId", "applicationName", activeTraceBo.getAgentId(),
                                                                                                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_SLOW_COUNT.getFieldName(),
                                                                                                            activeTraceBo.getActiveTraceHistogram().getSlowCount(), activeTraceBo.getTimestamp());
                                                                    AgentStat verySlowCount = new AgentStat("defaultTenantId", "applicationName", activeTraceBo.getAgentId(),
                                                                                                            AgentStatType.ACTIVE_TRACE.getChartType(), AgentStatField.ACTIVE_TRACE_VERY_SLOW_COUNT.getFieldName(),
                                                                                                            activeTraceBo.getActiveTraceHistogram().getVerySlowCount(), activeTraceBo.getTimestamp());
                                                                    activeTraceList.add(fastCount);
                                                                    activeTraceList.add(normalCount);
                                                                    activeTraceList.add(slowCount);
                                                                    activeTraceList.add(verySlowCount);
                                                                    return activeTraceList.stream();
                                                                })
                                                         .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertJvmGcToAgentStat(List<JvmGcBo> jvmGcBoList) {
        List<AgentStat> agentStatList = jvmGcBoList.stream()
                                                   .flatMap(jvmGcBo -> {
                                                                            List<AgentStat> jvmGcStatList = new ArrayList<>();
                                                                            AgentStat gcType = new AgentStat("defaultTenantId", "applicationName", jvmGcBo.getAgentId(),
                                                                                                                AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_TYPE.getFieldName(),
                                                                                                                jvmGcBo.getGcType().getTypeCode(), jvmGcBo.getTimestamp());
                                                                            AgentStat heapUsed = new AgentStat("defaultTenantId", "applicationName", jvmGcBo.getAgentId(),
                                                                                                                   AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_HEAP_USED.getFieldName(),
                                                                                                                   jvmGcBo.getHeapUsed(), jvmGcBo.getTimestamp());
                                                                            AgentStat heapMax = new AgentStat("defaultTenantId", "applicationName", jvmGcBo.getAgentId(),
                                                                                                                   AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_HEAP_MAX.getFieldName(),
                                                                                                                   jvmGcBo.getHeapMax(), jvmGcBo.getTimestamp());
                                                                            AgentStat nonHeapUsed = new AgentStat("defaultTenantId", "applicationName", jvmGcBo.getAgentId(),
                                                                                                                   AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_NONHEAP_USED.getFieldName(),
                                                                                                                   jvmGcBo.getNonHeapUsed(), jvmGcBo.getTimestamp());
                                                                            AgentStat nonHeapMax = new AgentStat("defaultTenantId", "applicationName", jvmGcBo.getAgentId(),
                                                                                                                   AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_NONHEAP_MAX.getFieldName(),
                                                                                                                   jvmGcBo.getNonHeapMax(), jvmGcBo.getTimestamp());
                                                                            AgentStat gcOldCount = new AgentStat("defaultTenantId", "applicationName", jvmGcBo.getAgentId(),
                                                                                                                   AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_NONHEAP_GC_OLD_COUNT.getFieldName(),
                                                                                                                   jvmGcBo.getGcOldCount(), jvmGcBo.getTimestamp());
                                                                            AgentStat gcOldTime = new AgentStat("defaultTenantId", "applicationName", jvmGcBo.getAgentId(),
                                                                                                                   AgentStatType.JVM_GC.getChartType(), AgentStatField.JVM_GC_NONHEAP_GC_OLD_TIME.getFieldName(),
                                                                                                                   jvmGcBo.getGcOldTime(), jvmGcBo.getTimestamp());
                                                                            jvmGcStatList.add(gcType);
                                                                            jvmGcStatList.add(heapUsed);
                                                                            jvmGcStatList.add(heapMax);
                                                                            jvmGcStatList.add(nonHeapUsed);
                                                                            jvmGcStatList.add(nonHeapMax);
                                                                            jvmGcStatList.add(gcOldCount);
                                                                            jvmGcStatList.add(gcOldTime);

                                                                            return jvmGcStatList.stream();
                                                        })
                                                   .collect(Collectors.toList());

        return agentStatList;
    }


    public static List<AgentStat> convertJvmGCDetailedToAgentStat(List<JvmGcDetailedBo> jvmGcDetailedBoList) {
        List<AgentStat> agentStatList = jvmGcDetailedBoList.stream()
                                                            .flatMap(jvmGcDetailedBo -> {
                                                                                            List<AgentStat> jvmGcDetailStatList = new ArrayList<>();
                                                                                            AgentStat newGcCount = new AgentStat("defaultTenantId", "applicationName", jvmGcDetailedBo.getAgentId(),
                                                                                                                                AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_GC_NEW_COUNT.getFieldName(),
                                                                                                                                jvmGcDetailedBo.getGcNewCount(), jvmGcDetailedBo.getTimestamp());
                                                                                            AgentStat newGcTime = new AgentStat("defaultTenantId", "applicationName", jvmGcDetailedBo.getAgentId(),
                                                                                                                               AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_GC_NEW_TIME.getFieldName(),
                                                                                                                               jvmGcDetailedBo.getGcNewTime(), jvmGcDetailedBo.getTimestamp());
                                                                                            AgentStat codeCacheUsed = new AgentStat("defaultTenantId", "applicationName", jvmGcDetailedBo.getAgentId(),
                                                                                                                                   AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_CODE_CACHE_USED.getFieldName(),
                                                                                                                                   jvmGcDetailedBo.getCodeCacheUsed(), jvmGcDetailedBo.getTimestamp());
                                                                                            AgentStat newGenUsed = new AgentStat("defaultTenantId", "applicationName", jvmGcDetailedBo.getAgentId(),
                                                                                                                                   AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_NEW_GEN_USED.getFieldName(),
                                                                                                                                   jvmGcDetailedBo.getNewGenUsed(), jvmGcDetailedBo.getTimestamp());
                                                                                            AgentStat oldGenUsed = new AgentStat("defaultTenantId", "applicationName", jvmGcDetailedBo.getAgentId(),
                                                                                                                                   AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_OLD_GEN_USED.getFieldName(),
                                                                                                                                   jvmGcDetailedBo.getOldGenUsed(), jvmGcDetailedBo.getTimestamp());
                                                                                            AgentStat survivorSpaceUsed = new AgentStat("defaultTenantId", "applicationName", jvmGcDetailedBo.getAgentId(),
                                                                                                                                       AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_SURVIVOR_SPACE_USED.getFieldName(),
                                                                                                                                       jvmGcDetailedBo.getSurvivorSpaceUsed(), jvmGcDetailedBo.getTimestamp());
                                                                                            AgentStat permGenUsed = new AgentStat("defaultTenantId", "applicationName", jvmGcDetailedBo.getAgentId(),
                                                                                                                                   AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_PERM_GEN_USED.getFieldName(),
                                                                                                                                   jvmGcDetailedBo.getPermGenUsed(), jvmGcDetailedBo.getTimestamp());
                                                                                            AgentStat metaspaceUsed = new AgentStat("defaultTenantId", "applicationName", jvmGcDetailedBo.getAgentId(),
                                                                                                                                   AgentStatType.JVM_GC_DETAILED.getChartType(), AgentStatField.JVM_GC_DETAILED_METASPACE_USED.getFieldName(),
                                                                                                                                   jvmGcDetailedBo.getMetaspaceUsed(), jvmGcDetailedBo.getTimestamp());

                                                                                            jvmGcDetailStatList.add(newGcCount);
                                                                                            jvmGcDetailStatList.add(newGcTime);
                                                                                            jvmGcDetailStatList.add(codeCacheUsed);
                                                                                            jvmGcDetailStatList.add(newGenUsed);
                                                                                            jvmGcDetailStatList.add(oldGenUsed);
                                                                                            jvmGcDetailStatList.add(survivorSpaceUsed);
                                                                                            jvmGcDetailStatList.add(permGenUsed);
                                                                                            jvmGcDetailStatList.add(metaspaceUsed);

                                                                                            return jvmGcDetailStatList.stream();
                                                                                        }
                                                            )
                                                            .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertTransactionToAgentStat(List<TransactionBo> transactionBoList) {
        List<AgentStat> agentStatList = transactionBoList.stream()
                                                            .flatMap(transactionBo -> {
                                                                                            List<AgentStat> transactionStatList = new ArrayList<>();
                                                                                            AgentStat collectInterval = new AgentStat("defaultTenantId", "applicationName", transactionBo.getAgentId(),
                                                                                                                                        AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_COLLECT_INTERVAL.getFieldName(),
                                                                                                                                        transactionBo.getCollectInterval(), transactionBo.getTimestamp());
                                                                                            AgentStat sampledNewCount = new AgentStat("defaultTenantId", "applicationName", transactionBo.getAgentId(),
                                                                                                                                        AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_SAMPLED_NEW_COUNT.getFieldName(),
                                                                                                                                        transactionBo.getSampledNewCount(), transactionBo.getTimestamp());
                                                                                            AgentStat sampledContinuationCount = new AgentStat("defaultTenantId", "applicationName", transactionBo.getAgentId(),
                                                                                                                                        AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_SAMPLED_CONTINUATION_COUNT.getFieldName(),
                                                                                                                                        transactionBo.getSampledContinuationCount(), transactionBo.getTimestamp());
                                                                                            AgentStat unsampledNewCount = new AgentStat("defaultTenantId", "applicationName", transactionBo.getAgentId(),
                                                                                                                                        AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_UNSAMPLED_NEW_COUNT.getFieldName(),
                                                                                                                                        transactionBo.getUnsampledNewCount(), transactionBo.getTimestamp());
                                                                                            AgentStat unsampledContinuationCount = new AgentStat("defaultTenantId", "applicationName", transactionBo.getAgentId(),
                                                                                                                                                    AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_UNSAMPLED_CONTINUATION_COUNT.getFieldName(),
                                                                                                                                                    transactionBo.getUnsampledContinuationCount(), transactionBo.getTimestamp());
                                                                                            AgentStat skippedNewSkipCount = new AgentStat("defaultTenantId", "applicationName", transactionBo.getAgentId(),
                                                                                                                                            AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_SKIPPED_NEW_SKIP_COUNT.getFieldName(),
                                                                                                                                            transactionBo.getSkippedNewSkipCount(), transactionBo.getTimestamp());
                                                                                            AgentStat skippedContinuationCount = new AgentStat("defaultTenantId", "applicationName", transactionBo.getAgentId(),
                                                                                                                                                AgentStatType.TRANSACTION.getChartType(), AgentStatField.TRANSACTION_SKIPPED_CONTINUATION_COUNT.getFieldName(),
                                                                                                                                                transactionBo.getSkippedContinuationCount(), transactionBo.getTimestamp());

                                                                                            transactionStatList.add(collectInterval);
                                                                                            transactionStatList.add(sampledNewCount);
                                                                                            transactionStatList.add(sampledContinuationCount);
                                                                                            transactionStatList.add(unsampledNewCount);
                                                                                            transactionStatList.add(unsampledContinuationCount);
                                                                                            transactionStatList.add(skippedNewSkipCount);
                                                                                            transactionStatList.add(skippedContinuationCount);

                                                                                            return transactionStatList.stream();
                                                                                        }
                                                            )
                                                            .collect(Collectors.toList());
        return agentStatList;
    }

    public static List<AgentStat> convertResponseTimeToAgentStat(List<ResponseTimeBo> reponseTimeBoList) {
        List<AgentStat> agentStatList = reponseTimeBoList.stream()
                                                            .flatMap(responseTimeBo -> {
                                                                                            List<AgentStat> responseTimeStatList = new ArrayList<>();

                                                                                            AgentStat avg = new AgentStat("defaultTenantId", "applicationName", responseTimeBo.getAgentId(),
                                                                                                                            AgentStatType.RESPONSE_TIME.getChartType(), AgentStatField.RESPONSE_TIME_AVG.getFieldName(),
                                                                                                                            responseTimeBo.getAvg(), responseTimeBo.getTimestamp());
                                                                                            AgentStat max = new AgentStat("defaultTenantId", "applicationName", responseTimeBo.getAgentId(),
                                                                                                                            AgentStatType.RESPONSE_TIME.getChartType(), AgentStatField.RESPONSE_TIME_MAX.getFieldName(),
                                                                                                                            responseTimeBo.getMax(), responseTimeBo.getTimestamp());

                                                                                            responseTimeStatList.add(avg);
                                                                                            responseTimeStatList.add(max);

                                                                                            return responseTimeStatList.stream();
                                                                                        }
                                                            )
                                                            .collect(Collectors.toList());
        return agentStatList;
    }

    public static List<AgentStat> convertDeadlockThreadCountToAgentStat(List<DeadlockThreadCountBo> deadlockThreadCountBoList) {
        List<AgentStat> agentStatList = deadlockThreadCountBoList.stream()
                                                                    .flatMap(deadlockThreadCountBo -> {
                                                                                                            List<AgentStat> deadlockThreadCountStatList = new ArrayList<>();

                                                                                                            AgentStat deadlockedThreadCount = new AgentStat("defaultTenantId", "applicationName", deadlockThreadCountBo.getAgentId(),
                                                                                                                    AgentStatType.DEADLOCK.getChartType(), AgentStatField.DEADLOCK_THREAD_COUNT.getFieldName(),
                                                                                                                    deadlockThreadCountBo.getDeadlockedThreadCount(), deadlockThreadCountBo.getTimestamp());

                                                                                                            deadlockThreadCountStatList.add(deadlockedThreadCount);

                                                                                                            return deadlockThreadCountStatList.stream();
                                                                                                        }
                                                                        )
                                                                    .collect(Collectors.toList());
        return agentStatList;
    }

    public static List<AgentStat> convertFileDescriptorToAgentStat(List<FileDescriptorBo> fileDescriptorBoList) {
        List<AgentStat> agentStatList = fileDescriptorBoList.stream()
                                                                .flatMap(fileDescriptorBo -> {
                                                                                                List<AgentStat> fileDescriptorStatList = new ArrayList<>();

                                                                                                AgentStat openFileDescriptorCount = new AgentStat("defaultTenantId", "applicationName", fileDescriptorBo.getAgentId(),
                                                                                                        AgentStatType.FILE_DESCRIPTOR.getChartType(), AgentStatField.OPEN_FILE_DESCRIPTOR_COUNT.getFieldName(),
                                                                                                        fileDescriptorBo.getOpenFileDescriptorCount(), fileDescriptorBo.getTimestamp());

                                                                                                fileDescriptorStatList.add(openFileDescriptorCount);

                                                                                                return fileDescriptorStatList.stream();
                                                                                            }
                                                                )
                                                                .collect(Collectors.toList());
        return agentStatList;
    }

    public static List<AgentStat> convertDirectBufferToAgentStat(List<DirectBufferBo> directBufferBoList) {
        List<AgentStat> agentStatList = directBufferBoList.stream()
                                                            .flatMap(directBufferBo -> {
                                                                        List<AgentStat> fileDescriptorStatList = new ArrayList<>();

                                                                        AgentStat directCount = new AgentStat("defaultTenantId", "applicationName", directBufferBo.getAgentId(),
                                                                                                                AgentStatType.DIRECT_BUFFER.getChartType(), AgentStatField.DIRECT_BUFFER_DIRECT_COUNT.getFieldName(),
                                                                                                                directBufferBo.getDirectCount(), directBufferBo.getTimestamp());
                                                                        AgentStat directMemoryUsed = new AgentStat("defaultTenantId", "applicationName", directBufferBo.getAgentId(),
                                                                                                                    AgentStatType.DIRECT_BUFFER.getChartType(), AgentStatField.DIRECT_BUFFER_DIRECT_MEMORY_USED.getFieldName(),
                                                                                                                    directBufferBo.getDirectMemoryUsed(), directBufferBo.getTimestamp());
                                                                        AgentStat mappedCount = new AgentStat("defaultTenantId", "applicationName", directBufferBo.getAgentId(),
                                                                                                                AgentStatType.DIRECT_BUFFER.getChartType(), AgentStatField.DIRECT_BUFFER_MAPPED_COUNT.getFieldName(),
                                                                                                                directBufferBo.getMappedCount(), directBufferBo.getTimestamp());
                                                                        AgentStat mappedMemoryUsed = new AgentStat("defaultTenantId", "applicationName", directBufferBo.getAgentId(),
                                                                                                                    AgentStatType.DIRECT_BUFFER.getChartType(), AgentStatField.DIRECT_BUFFER_MAPPED_MEMORY_USED.getFieldName(),
                                                                                                                    directBufferBo.getMappedMemoryUsed(), directBufferBo.getTimestamp());

                                                                        fileDescriptorStatList.add(directCount);
                                                                        fileDescriptorStatList.add(directMemoryUsed);
                                                                        fileDescriptorStatList.add(mappedCount);
                                                                        fileDescriptorStatList.add(mappedMemoryUsed);

                                                                        return fileDescriptorStatList.stream();
                                                                    }
                                                            )
                                                            .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertTotalThreadCountToAgentStat(List<TotalThreadCountBo> totalThreadCountBoList) {
        List<AgentStat> agentStatList = totalThreadCountBoList.stream()
                                                                .flatMap(totalThreadCountBo -> {
                                                                            List<AgentStat> totalThreadCountList = new ArrayList<>();

                                                                            AgentStat totalThreadCount = new AgentStat("defaultTenantId", "applicationName", totalThreadCountBo.getAgentId(),
                                                                                                                        AgentStatType.TOTAL_THREAD.getChartType(), AgentStatField.TOTAL_THREAD_COUNT.getFieldName(),
                                                                                                                        totalThreadCountBo.getTotalThreadCount(), totalThreadCountBo.getTimestamp());

                                                                            totalThreadCountList.add(totalThreadCount);

                                                                            return totalThreadCountList.stream();
                                                                        }
                                                                    )
                                                                .collect(Collectors.toList());

        return agentStatList;
    }


    public static List<AgentStat> convertLoadedClassToAgentStat(List<LoadedClassBo> loadedClassBoList) {
        List<AgentStat> agentStatList = loadedClassBoList.stream()
                                                            .flatMap(loadedClassBo -> {
                                                                        List<AgentStat> loadedClassList = new ArrayList<>();

                                                                        AgentStat loadedClassCount = new AgentStat("defaultTenantId", "applicationName", loadedClassBo.getAgentId(),
                                                                                                                    AgentStatType.LOADED_CLASS.getChartType(), AgentStatField.CLASS_COUNT_LOAD.getFieldName(),
                                                                                                                    loadedClassBo.getLoadedClassCount(), loadedClassBo.getTimestamp());
                                                                        AgentStat unloadedClassCount = new AgentStat("defaultTenantId", "applicationName", loadedClassBo.getAgentId(),
                                                                                                                        AgentStatType.LOADED_CLASS.getChartType(), AgentStatField.CLASS_COUNT_UN_LOADED.getFieldName(),
                                                                                                                        loadedClassBo.getUnloadedClassCount(), loadedClassBo.getTimestamp());

                                                                        loadedClassList.add(loadedClassCount);
                                                                        loadedClassList.add(unloadedClassCount);

                                                                        return loadedClassList.stream();
                                                                    }
                                                                )
                                                            .collect(Collectors.toList());

        return agentStatList;
    }

    public static List<AgentStat> convertDataSourceToAgentStat(List<DataSourceListBo> dataSourceListBoList) {
        List<AgentStat> agentStatList = dataSourceListBoList.stream()
                .flatMap(dataSourceListBo -> {
                            List<AgentStat> dataSourceLists = new ArrayList<>();

                            for (DataSourceBo dataSourceBo : dataSourceListBo.getList()) {
                                Tags tags = new Tags();
                                tags.add("serviceTypeCode",String.valueOf(dataSourceBo.getServiceTypeCode()));
                                tags.add("databaseName",dataSourceBo.getDatabaseName());
                                tags.add("jdbcUrl",dataSourceBo.getJdbcUrl());

                                AgentStat activeConnectionSize = new AgentStat("defaultTenantId", "applicationName", dataSourceListBo.getAgentId(),
                                        AgentStatType.DATASOURCE.getChartType(), AgentStatField.DATASOURCE_ACTIVE_CONNECTION_SIZE.getFieldName(),
                                        dataSourceBo.getActiveConnectionSize(), dataSourceListBo.getTimestamp(), tags.getTags());

                                AgentStat maxConnectionSize = new AgentStat("defaultTenantId", "applicationName", dataSourceListBo.getAgentId(),
                                        AgentStatType.DATASOURCE.getChartType(), AgentStatField.DATASOURCE_MAX_CONNECTION_SIZE.getFieldName(),
                                        dataSourceBo.getMaxConnectionSize(), dataSourceListBo.getTimestamp(), tags.getTags());

                                dataSourceLists.add(activeConnectionSize);
                                dataSourceLists.add(maxConnectionSize);
                            }

                            return dataSourceLists.stream();
                        }
                    )
                .collect(Collectors.toList());
        return agentStatList;
    }

}
