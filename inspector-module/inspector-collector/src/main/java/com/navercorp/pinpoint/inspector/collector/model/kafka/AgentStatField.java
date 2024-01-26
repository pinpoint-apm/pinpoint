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

import java.util.EnumSet;
import java.util.Set;

/**
 * @author minwoo.jung
 */
public enum AgentStatField {

    UNKNOWN(0, "Unknown", null),
    CPU_LOAD_JVM(1, "cpuLoad", "jvm"),
    CPU_LOAD_SYSTEM(2, "cpuLoad", "system"),

    ACTIVE_TRACE_FAST_COUNT(10, "activeTrace", "fastCount"),
    ACTIVE_TRACE_NORNAL_COUNT(11, "activeTrace", "normalCount"),
    ACTIVE_TRACE_SLOW_COUNT(12, "activeTrace", "slowCount"),
    ACTIVE_TRACE_VERY_SLOW_COUNT(13, "activeTrace", "verySlowCount"),
    ACTIVE_TRACE_TOTAL_COUNT(14, "activeTrace", "totalCount"),

    // TODO : (minwoo) This is split into two metrics. How about specifying a separate metricName?
    JVM_GC_TYPE(20, "jvmGC", "GCType"),
    JVM_GC_HEAP_USED(21, "jvmGC", "heapUsed"),
    JVM_GC_HEAP_MAX(22, "jvmGC", "heapMax"),
    JVM_GC_NONHEAP_USED(23, "jvmGC", "nonHeapUsed"),
    JVM_GC_NONHEAP_MAX(24, "jvmGC", "nonHeapMax"),
    JVM_GC_NONHEAP_GC_OLD_COUNT(25, "jvmGC", "gcOldCount"),
    JVM_GC_NONHEAP_GC_OLD_TIME(26, "jvmGC", "gcOldTime"),

    JVM_GC_DETAILED_GC_NEW_COUNT(40, "jvmGCDetailed", "newGcCount"),
    JVM_GC_DETAILED_GC_NEW_TIME(41, "jvmGCDetailed", "newGcTime"),
    JVM_GC_DETAILED_CODE_CACHE_USED(42, "jvmGCDetailed", "codeCacheUsed"),
    JVM_GC_DETAILED_NEW_GEN_USED(43, "jvmGCDetailed", "newGenUsed"),
    JVM_GC_DETAILED_OLD_GEN_USED(44, "jvmGCDetailed", "oldGenUsed"),
    JVM_GC_DETAILED_SURVIVOR_SPACE_USED(45, "jvmGCDetailed", "survivorSpaceUsed"),
    JVM_GC_DETAILED_PERM_GEN_USED(46, "jvmGCDetailed", "permGenUsed"),
    JVM_GC_DETAILED_METASPACE_USED(47, "jvmGCDetailed", "metaspaceUsed"),

    TRANSACTION_COLLECT_INTERVAL(60, "transaction", "collectInterval"),
    TRANSACTION_SAMPLED_NEW_COUNT(61, "transaction", "sampledNewCount"),
    TRANSACTION_SAMPLED_CONTINUATION_COUNT(62, "transaction", "sampledContinuationCount"),
    TRANSACTION_UNSAMPLED_NEW_COUNT(63, "transaction", "unsampledNewCount"),
    TRANSACTION_UNSAMPLED_CONTINUATION_COUNT(64, "transaction", "unsampledContinuationCount"),
    TRANSACTION_SKIPPED_NEW_SKIP_COUNT(65, "transaction", "skippedNewSkipCount"),
    TRANSACTION_SKIPPED_CONTINUATION_COUNT(66, "transaction", "skippedContinuationCount"),
    TRANSACTION_TOTAL_COUNT(67, "transaction", "totalCount"),
    TRANSACTION_TOTAL_COUNT_PER_MS(68, "transaction", "totalCountPerMs"),

    RESPONSE_TIME_AVG(80, "responseTime", "avg"),
    RESPONSE_TIME_MAX(81, "responseTime", "max"),

    DEADLOCK_THREAD_COUNT(90, "deadlockedThreadCount", "deadlockedThreadCount"),
    OPEN_FILE_DESCRIPTOR_COUNT(91, "fileDescriptor", "openFileDescriptorCount"),

    DIRECT_BUFFER_DIRECT_COUNT(100, "directBuffer", "directCount"),
    DIRECT_BUFFER_DIRECT_MEMORY_USED(101, "directBuffer", "directMemoryUsed"),
    DIRECT_BUFFER_MAPPED_COUNT(102, "directBuffer", "mappedCount"),
    DIRECT_BUFFER_MAPPED_MEMORY_USED(103, "directBuffer", "mappedMemoryUsed"),

    TOTAL_THREAD_COUNT(120, "totalThreadCount", "totalThreadCount"),

    CLASS_COUNT_LOAD(130, "loadedClass", "loaded"),
    CLASS_COUNT_UN_LOADED(131, "loadedClass", "unLoaded"),

    DATASOURCE_ACTIVE_CONNECTION_SIZE(140, "dataSource", "activeConnectionSize"),
    DATASOURCE_MAX_CONNECTION_SIZE(141, "dataSource", "maxConnectionSize");

    private final byte typeCode;
    private final String metricName;
    private final String fieldName;

    private static final Set<AgentStatField> AGENT_STAT_TYPES = EnumSet.allOf(AgentStatField.class);

    AgentStatField(int typeCode, String metricName, String fieldName) {
        if (typeCode < 0 || typeCode > 255) {
            throw new IllegalArgumentException("type code out of range (0~255)");
        }
        this.typeCode = (byte) (typeCode & 0xFF);
        this.metricName = metricName;
        this.fieldName = fieldName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static AgentStatField fromTypeCode(byte typeCode) {

        for (AgentStatField agentStatField : AGENT_STAT_TYPES) {
            if (agentStatField.typeCode == typeCode) {
                return agentStatField;
            }
        }
        return UNKNOWN;
    }

}
