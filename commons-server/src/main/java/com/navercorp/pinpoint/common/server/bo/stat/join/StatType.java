/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;

/**
 * @author minwoo.jung
 */
public enum StatType {
    UNKNOWN(0, "Unknown", null),

    APP_STST(1, "Application stat", null),
    APP_CPU_LOAD(2, "Application Cpu Usage", "cpuLoad"),
    APP_MEMORY_USED(3, "Application Memory Usage", "memory"),
    APP_TRANSACTION_COUNT(4, "Application Transaction Count", "transaction"),
    APP_ACTIVE_TRACE_COUNT(5, "Application Active trace Count", "activeTrace"),
    APP_RESPONSE_TIME(6, "Application Response Time", "responseTime"),
    APP_DATA_SOURCE(7, "Application data Source", "dataSource"),
    APP_FILE_DESCRIPTOR(8, "Application File Descriptor Count", "fileDescriptor"),
    APP_DIRECT_BUFFER(9, "Application Direct Buffer", "directBuffer"),
    APP_TOTAL_THREAD_COUNT(10, "Application Total Thread Count", "totalThreadCount"),
    APP_LOADED_CLASS(11, "Application Loaded Class", "loadedClass"),

    APP_STST_AGGRE(51, "Application stst aggregation", null),
    APP_CPU_LOAD_AGGRE(52, "Application Cpu Usage aggregation", "cpuLoad"),
    APP_MEMORY_USED_AGGRE(53, "Application Memory Usage aggregation", "memory"),
    APP_TRANSACTION_COUNT_AGGRE(54, "Application Transaction count aggregation", "transaction"),
    APP_ACTIVE_TRACE_COUNT_AGGRE(55, "Application Active trace count aggregation", "activeTrace"),
    APP_RESPONSE_TIME_AGGRE(56, "Application Response Time aggregation", "responseTime"),
    APP_DATA_SOURCE_AGGRE(57, "Application Data Source aggregation", "dataSource"),
    APP_FILE_DESCRIPTOR_AGGRE(58, "Application File Descriptor count aggregation", "fileDescriptor"),
    APP_DIRECT_BUFFER_AGGRE(59, "Application Direct Buffer aggregation", "directBuffer"),
    APP_TOTAL_THREAD_COUNT_AGGRE(60, "Application Total Thread count aggregation", "totalThreadCount"),
    APP_LOADED_CLASS_AGGRE(61, "Application Loaded Class aggregation", "loadedClass"),

    AGENT_STST_AGGRE(101, "Agent stst aggregation", null),
    AGENT_CPU_LOAD_AGGRE(102, "Agent Cpu Usage aggregation", "cpuLoad"),
    AGENT_MEMORY_USED_AGGRE(103, "Agent Memory Usage aggregation", "memory"),
    AGENT_TRANSACTION_COUNT_AGGRE(104, "Agent Transaction count aggregation", "transaction"),
    AGENT_ACTIVE_TRACE_COUNT_AGGRE(105, "Agent Active trace count aggregation", "activeTrace"),
    AGENT_RESPONSE_TIME_AGGRE(106, "Agent response time aggregation", "responseTime"),
    AGENT_DATA_SOURCE_AGGRE(107, "Agent data source aggregation", "dataSource"),
    AGENT_FILE_DESCRIPTOR_AGGRE(108, "Agent File Descriptor count aggregation", "fileDescriptor"),
    AGENT_DIRECT_BUFFER_AGGRE(109, "Agent Direct Buffer aggregation", "directBuffer"),
    AGENT_TOTAL_THREAD_AGGRE(110, "Agent Total Thread count aggregation", "totalThreadCount"),
    AGENT_LOADED_CLASS_AGGRE(111, "Agent Loaded Class aggregation", "loadedClass");

    public static final int TYPE_CODE_BYTE_LENGTH = 1;

    private final byte typeCode;
    private final String name;
    private final String chartType;

    private static final IntHashMap<StatType> STAT_TYPE_MAP = toStatTypeMap();

    StatType(int typeCode, String name, String chartType) {
        if (typeCode < 0 || typeCode > 255) {
            throw new IllegalArgumentException("type code out of range (0~255)");
        }
        this.typeCode = (byte) (typeCode & 0xFF);
        this.name = name;
        this.chartType = chartType;
    }

    public int getTypeCode() {
        return this.typeCode & 0xFF;
    }

    public byte getRawTypeCode() {
        return typeCode;
    }

    public String getName() {
        return name;
    }

    public String getChartType() {
        return chartType;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static StatType fromTypeCode(byte typeCode) {
        final StatType statType = STAT_TYPE_MAP.get(typeCode);
        if (statType == null) {
            return UNKNOWN;
        }
        return statType;
    }

    private static IntHashMap<StatType> toStatTypeMap() {
        final IntHashMap<StatType> map = new IntHashMap<>();
        for (StatType agentStatType : StatType.values()) {
            map.put(agentStatType.getTypeCode(), agentStatType);
        }
        return map;
    }



}
