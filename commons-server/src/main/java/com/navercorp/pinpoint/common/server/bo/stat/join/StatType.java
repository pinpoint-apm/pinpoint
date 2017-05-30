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

/**
 * @author minwoo.jung
 */
public enum StatType {
    UNKNOWN(0, "Unknown"),
    APP_CPU_LOAD(1, "Application Cpu Usage"),
    APP_CPU_LOAD_AGGRE(2, "Application Cpu Usage aggregation"),
    AGENT_CPU_LOAD_AGGRE(10, "Agent Cpu Usage aggregation");

    public static final int TYPE_CODE_BYTE_LENGTH = 1;

    private final byte typeCode;
    private final String name;

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
        for (StatType agentStatType : StatType.values()) {
            if (agentStatType.typeCode == typeCode) {
                return agentStatType;
            }
        }
        return UNKNOWN;
    }



}
