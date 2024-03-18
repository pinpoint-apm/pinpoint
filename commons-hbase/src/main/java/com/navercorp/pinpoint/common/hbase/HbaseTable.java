/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

/**
 * @author emeroad
 * @author Taejin Koo
 */
public enum HbaseTable {

    AGENTINFO("AgentInfo"),
    AGENT_EVENT("AgentEvent"),
    AGENT_LIFECYCLE("AgentLifeCycle"),
    AGENT_STAT_VER2("AgentStatV2"),
    AGENT_URI_STAT("AgentUriStat"),
    API_METADATA("ApiMetaData"),
    APPLICATION_INDEX("ApplicationIndex"),
    APPLICATION_INDEX_VER2("ApplicationIndex_Ver2"),
    APPLICATION_ID("ApplicationId"),
    SERVICE_ID("ServiceId"),
    APPLICATION_STAT_AGGRE("ApplicationStatAggre"),
    APPLICATION_TRACE_INDEX("ApplicationTraceIndex"),
    APPLICATION_TRACE_INDEX_VER2("ApplicationTraceIndex_Ver2"),
    HOST_APPLICATION_MAP_VER2("HostApplicationMap_Ver2"),
    MAP_STATISTICS_CALLEE_VER2("ApplicationMapStatisticsCallee_Ver2"),
    MAP_STATISTICS_CALLER_VER2("ApplicationMapStatisticsCaller_Ver2"),
    MAP_STATISTICS_CALLEE_VER3("ApplicationMapStatisticsCallee_Ver3"),
    MAP_STATISTICS_SELF_VER2("ApplicationMapStatisticsSelf_Ver2"),
    SQL_METADATA_VER2("SqlMetaData_Ver2"),
    SQL_UID_METADATA("SqlUidMetaData"),
    STRING_METADATA("StringMetaData"),
    TRACE_V2("TraceV2");

    private final String name;

    HbaseTable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
