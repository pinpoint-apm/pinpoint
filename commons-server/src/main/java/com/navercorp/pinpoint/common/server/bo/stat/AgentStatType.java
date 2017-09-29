/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.stat;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public enum AgentStatType {
    UNKNOWN(0, "Unknown"),
    JVM_GC(1, "JVM GC"),
    JVM_GC_DETAILED(2, "JVM GC Detailed"),
    CPU_LOAD(3, "Cpu Usage"),
    TRANSACTION((byte) 4, "Transaction"),
    ACTIVE_TRACE((byte) 5, "Active Trace"),
    DATASOURCE((byte) 6, "DataSource"),
    RESPONSE_TIME((byte) 7, "Response Time"),
    DEADLOCK((byte) 8, "Deadlock");

    public static final int TYPE_CODE_BYTE_LENGTH = 1;

    private final byte typeCode;
    private final String name;

    private static final Set<AgentStatType> AGENT_STAT_TYPES = EnumSet.allOf(AgentStatType.class);

    AgentStatType(int typeCode, String name) {
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

    public static AgentStatType fromTypeCode(byte typeCode) {

        for (AgentStatType agentStatType : AGENT_STAT_TYPES) {
            if (agentStatType.typeCode == typeCode) {
                return agentStatType;
            }
        }
        return UNKNOWN;
    }
}
