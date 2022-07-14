/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AgentLifeCycleState {
    RUNNING((short) 100, "Running"),
    SHUTDOWN((short) 200, "Shutdown"),
    UNEXPECTED_SHUTDOWN((short) 201, "Unexpected Shutdown"),
    DISCONNECTED((short) 300, "Disconnected"),
    UNKNOWN((short) -1, "Unknown");

    private static final IntHashMap<AgentLifeCycleState> MAPPING = initializeCodeMapping();

    private final short code;
    private final String desc;

    AgentLifeCycleState(short code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public short getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }

    private static IntHashMap<AgentLifeCycleState> initializeCodeMapping() {
        IntHashMap<AgentLifeCycleState> codeMap = new IntHashMap<>();
        for (AgentLifeCycleState state : AgentLifeCycleState.values()) {
            codeMap.put(state.getCode(), state);
        }
        return codeMap;
    }

    public static AgentLifeCycleState getStateByCode(short code) {
        AgentLifeCycleState state = MAPPING.get(code);
        if (state != null) {
            return state;
        }
        return UNKNOWN;
    }
}