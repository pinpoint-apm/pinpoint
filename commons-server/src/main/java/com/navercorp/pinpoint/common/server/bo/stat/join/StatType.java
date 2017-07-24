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
    UNKNOWN(0, "Unknown"),

    APP_STST(1, "Application stat"),
    APP_CPU_LOAD(2, "Application Cpu Usage"),
    APP_MEMORY_USED(3, "Application Memory Usage"),
    APP_TRANSACTION_COUNT(4, "Application Transaction Count"),


    APP_STST_AGGRE(51, "Application stst aggregation"),
    APP_CPU_LOAD_AGGRE(52, "Application Cpu Usage aggregation"),
    APP_MEMORY_USED_AGGRE(53, "Application Memory Usage aggregation"),
    APP_TRANSACTION_COUNT_AGGRE(54, "Application Transaction Count aggregation"),

    AGENT_STST_AGGRE(101, "Agent stst aggregation"),
    AGENT_CPU_LOAD_AGGRE(102, "Agent Cpu Usage aggregation"),
    AGENT_MEMORY_USED_AGGRE(103, "Agent Memory Usage aggregation"),
    AGENT_TRANSACTION_COUNT_AGGRE(104, "Agent Transaction Count aggregation");

    public static final int TYPE_CODE_BYTE_LENGTH = 1;

    private final byte typeCode;
    private final String name;

    private static final IntHashMap<StatType> STAT_TYPE_MAP = toStatTypeMap();

    private StatType(int typeCode, String name) {
        if (typeCode < 0 || typeCode > 255) {
            throw new IllegalArgumentException("type code out of range (0~255)");
        }
        this.typeCode = (byte) (typeCode & 0xFF);
        this.name = name;
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
        final IntHashMap<StatType> map = new IntHashMap<StatType>();
        for (StatType agentStatType : StatType.values()) {
            map.put(agentStatType.getTypeCode(), agentStatType);
        }
        return map;
    }



}
