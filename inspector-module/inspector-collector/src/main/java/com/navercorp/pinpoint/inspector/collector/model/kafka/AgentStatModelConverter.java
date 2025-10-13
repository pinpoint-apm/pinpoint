/*
 * Copyright 2025 NAVER Corp.
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

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) tenantId must be entered
public class AgentStatModelConverter {

    public static final String DATASOUCE_TAG_ID_KEY = "id";
    public static final String DATASOUCE_TAG_SERVICE_TYPE_CODE_KEY = "serviceTypeCode";
    public static final String DATASOUCE_TAG_DATABASE_NAME_KEY = "databaseName";
    public static final String DATASOUCE_TAG_JDBC_URL_KEY = "jdbcUrl";

    public List<AgentStat> convertCpuLoad(List<CpuLoadBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size() * 2);
        for (CpuLoadBo cpuLoadBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, cpuLoadBo);

            builder.collect(AgentStatField.CPU_LOAD_JVM, cpuLoadBo.getJvmCpuLoad());
            builder.collect(AgentStatField.CPU_LOAD_SYSTEM, cpuLoadBo.getSystemCpuLoad());
        }
        return list.build();
    }

    public List<AgentStat> convertActiveTrace(List<ActiveTraceBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size() * 5);
        for (ActiveTraceBo activeTraceBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, activeTraceBo);

            builder.collect(AgentStatField.ACTIVE_TRACE_FAST_COUNT, activeTraceBo.getActiveTraceHistogram().getFastCount());
            builder.collect(AgentStatField.ACTIVE_TRACE_NORNAL_COUNT, activeTraceBo.getActiveTraceHistogram().getNormalCount());
            builder.collect(AgentStatField.ACTIVE_TRACE_SLOW_COUNT, activeTraceBo.getActiveTraceHistogram().getSlowCount());
            builder.collect(AgentStatField.ACTIVE_TRACE_VERY_SLOW_COUNT, activeTraceBo.getActiveTraceHistogram().getVerySlowCount());

            double calculatedTotalCount = activeTraceBo.getActiveTraceHistogram().getFastCount() +
                                          activeTraceBo.getActiveTraceHistogram().getNormalCount() +
                                          activeTraceBo.getActiveTraceHistogram().getSlowCount() +
                                          activeTraceBo.getActiveTraceHistogram().getVerySlowCount();
            builder.collect(AgentStatField.ACTIVE_TRACE_TOTAL_COUNT, calculatedTotalCount);
        }
        return list.build();
    }

    public List<AgentStat> convertJvmGc(List<JvmGcBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size() * 7);
        for (JvmGcBo jvmGcBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, jvmGcBo);

            builder.collect(AgentStatField.JVM_GC_TYPE, jvmGcBo.getGcType().getTypeCode());
            builder.collect(AgentStatField.JVM_GC_HEAP_USED, jvmGcBo.getHeapUsed());
            builder.collect(AgentStatField.JVM_GC_HEAP_MAX, jvmGcBo.getHeapMax());
            builder.collect(AgentStatField.JVM_GC_NONHEAP_USED, jvmGcBo.getNonHeapUsed());
            builder.collect(AgentStatField.JVM_GC_NONHEAP_MAX, jvmGcBo.getNonHeapMax());
            builder.collect(AgentStatField.JVM_GC_NONHEAP_GC_OLD_COUNT, jvmGcBo.getGcOldCount());
            builder.collect(AgentStatField.JVM_GC_NONHEAP_GC_OLD_TIME, jvmGcBo.getGcOldTime());
        }
        return list.build();
    }


    public List<AgentStat> convertJvmGCDetailed(List<JvmGcDetailedBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size() * 8);
        for (JvmGcDetailedBo jvmGcDetailedBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, jvmGcDetailedBo);

            builder.collect(AgentStatField.JVM_GC_DETAILED_GC_NEW_COUNT, jvmGcDetailedBo.getGcNewCount());
            builder.collect(AgentStatField.JVM_GC_DETAILED_GC_NEW_TIME, jvmGcDetailedBo.getGcNewTime());
            builder.collect(AgentStatField.JVM_GC_DETAILED_CODE_CACHE_USED, jvmGcDetailedBo.getCodeCacheUsed());
            builder.collect(AgentStatField.JVM_GC_DETAILED_NEW_GEN_USED, jvmGcDetailedBo.getNewGenUsed());
            builder.collect(AgentStatField.JVM_GC_DETAILED_OLD_GEN_USED, jvmGcDetailedBo.getOldGenUsed());
            builder.collect(AgentStatField.JVM_GC_DETAILED_SURVIVOR_SPACE_USED, jvmGcDetailedBo.getSurvivorSpaceUsed());
            builder.collect(AgentStatField.JVM_GC_DETAILED_PERM_GEN_USED, jvmGcDetailedBo.getPermGenUsed());
            builder.collect(AgentStatField.JVM_GC_DETAILED_METASPACE_USED, jvmGcDetailedBo.getMetaspaceUsed());
        }
        return list.build();
    }


    public List<AgentStat> convertTransaction(List<TransactionBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size() * 9);

        for (TransactionBo transactionBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, transactionBo);

            builder.collect(AgentStatField.TRANSACTION_COLLECT_INTERVAL, transactionBo.getCollectInterval());
            builder.collect(AgentStatField.TRANSACTION_SAMPLED_NEW_COUNT, transactionBo.getSampledNewCount());
            builder.collect(AgentStatField.TRANSACTION_SAMPLED_CONTINUATION_COUNT, transactionBo.getSampledContinuationCount());
            builder.collect(AgentStatField.TRANSACTION_UNSAMPLED_NEW_COUNT, transactionBo.getUnsampledNewCount());
            builder.collect(AgentStatField.TRANSACTION_UNSAMPLED_CONTINUATION_COUNT, transactionBo.getUnsampledContinuationCount());
            builder.collect(AgentStatField.TRANSACTION_SKIPPED_NEW_SKIP_COUNT, transactionBo.getSkippedNewSkipCount());
            builder.collect(AgentStatField.TRANSACTION_SKIPPED_CONTINUATION_COUNT, transactionBo.getSkippedContinuationCount());

            double calculatedTotal = transactionBo.getSampledNewCount() + transactionBo.getSampledContinuationCount() +
                                     transactionBo.getUnsampledNewCount() + transactionBo.getUnsampledContinuationCount() +
                                     transactionBo.getSkippedNewSkipCount() + transactionBo.getSkippedContinuationCount();
            builder.collect(AgentStatField.TRANSACTION_TOTAL_COUNT, calculatedTotal);

            double calculatedTotalCountPerMs = Precision.round(calculatedTotal / (transactionBo.getCollectInterval() / 1000D), 1);
            builder.collect(AgentStatField.TRANSACTION_TOTAL_COUNT_PER_MS, calculatedTotalCountPerMs);
        }
        return list.build();
    }

    public List<AgentStat> convertResponseTime(List<ResponseTimeBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size() * 2);
        for (ResponseTimeBo responseTimeBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, responseTimeBo);

            builder.collect(AgentStatField.RESPONSE_TIME_AVG, responseTimeBo.getAvg());
            builder.collect(AgentStatField.RESPONSE_TIME_MAX, responseTimeBo.getMax());
        }
        return list.build();
    }

    public List<AgentStat> convertDeadlockThreadCount(List<DeadlockThreadCountBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size());

        for (DeadlockThreadCountBo deadlockThreadCountBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, deadlockThreadCountBo);

            builder.collect(AgentStatField.DEADLOCK_THREAD_COUNT, deadlockThreadCountBo.getDeadlockedThreadCount());
        }
        return list.build();
    }

    public List<AgentStat> convertFileDescriptor(List<FileDescriptorBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size());

        for (FileDescriptorBo fileDescriptorBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, fileDescriptorBo);

            builder.collect(AgentStatField.OPEN_FILE_DESCRIPTOR_COUNT, fileDescriptorBo.getOpenFileDescriptorCount());
        }
        return list.build();
    }

    public List<AgentStat> convertDirectBuffer(List<DirectBufferBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size() * 4);

        for (DirectBufferBo directBufferBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, directBufferBo);

            builder.collect(AgentStatField.DIRECT_BUFFER_DIRECT_COUNT, directBufferBo.getDirectCount());
            builder.collect(AgentStatField.DIRECT_BUFFER_DIRECT_MEMORY_USED, directBufferBo.getDirectMemoryUsed());
            builder.collect(AgentStatField.DIRECT_BUFFER_MAPPED_COUNT, directBufferBo.getMappedCount());
            builder.collect(AgentStatField.DIRECT_BUFFER_MAPPED_MEMORY_USED, directBufferBo.getMappedMemoryUsed());
        }
        return list.build();
    }

    public List<AgentStat> convertTotalThreadCount(List<TotalThreadCountBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size());

        for (TotalThreadCountBo totalThreadCountBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, totalThreadCountBo);

            builder.collect(AgentStatField.TOTAL_THREAD_COUNT, totalThreadCountBo.getTotalThreadCount());
        }
        return list.build();
    }


    public List<AgentStat> convertLoadedClass(List<LoadedClassBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(boList.size() * 2);

        for (LoadedClassBo loadedClassBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, loadedClassBo);

            builder.collect(AgentStatField.CLASS_COUNT_LOAD, loadedClassBo.getLoadedClassCount());
            builder.collect(AgentStatField.CLASS_COUNT_UN_LOADED, loadedClassBo.getUnloadedClassCount());
        }
        return list.build();
    }

    public List<AgentStat> convertDataSource(List<DataSourceListBo> boList, String tenantId) {
        AgentStatList list = new AgentStatList(32);

        for (DataSourceListBo dataSourceListBo : boList) {
            final AgentStatList.Collector builder = list.newCollect(tenantId, dataSourceListBo);

            for (DataSourceBo dataSourceBo : dataSourceListBo.getList()) {
                List<Tag> tags = List.of(
                        new Tag(DATASOUCE_TAG_ID_KEY, String.valueOf(dataSourceBo.getId())),
                        new Tag(DATASOUCE_TAG_SERVICE_TYPE_CODE_KEY, String.valueOf(dataSourceBo.getServiceTypeCode())),
                        new Tag(DATASOUCE_TAG_DATABASE_NAME_KEY, dataSourceBo.getDatabaseName()),
                        new Tag(DATASOUCE_TAG_JDBC_URL_KEY, dataSourceBo.getJdbcUrl())
                );

                builder.collect(AgentStatField.DATASOURCE_ACTIVE_CONNECTION_SIZE, dataSourceBo.getActiveConnectionSize(), tags);
                builder.collect(AgentStatField.DATASOURCE_MAX_CONNECTION_SIZE, dataSourceBo.getMaxConnectionSize(), tags);
            }
        }
        return list.build();
    }


}
