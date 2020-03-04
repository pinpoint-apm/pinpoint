/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;


import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TableDescriptors {
    private final TableNameProvider tableNameProvider;

    public TableDescriptors(TableNameProvider tableNameProvider) {
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    public TableDescriptor<HbaseColumnFamily.AgentInfo> getAgentInfo() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.AGENTINFO_INFO);
    }

    public TableDescriptor<HbaseColumnFamily.AgentEvent> getAgentEvent() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.AGENT_EVENT_EVENTS);
    }

    public TableDescriptor<HbaseColumnFamily.AgentLifeCycleStatus> getAgentLifeCycleStatus() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.AGENT_LIFECYCLE_STATUS);
    }

    public TableDescriptor<HbaseColumnFamily.AgentStatStatistics> getAgentStatStatus() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.AGENT_STAT_STATISTICS);
    }

    public TableDescriptor<HbaseColumnFamily.ApiMetadata> getApiMetadata() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.API_METADATA_API);
    }

    public TableDescriptor<HbaseColumnFamily.ApplicationIndex> getApplicationIndex() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.APPLICATION_INDEX_AGENTS);
    }

    public TableDescriptor<HbaseColumnFamily.ApplicationStatStatistics> getApplicationStatStatistics() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.APPLICATION_STAT_STATISTICS);
    }

    public TableDescriptor<HbaseColumnFamily.ApplicationTraceIndexTrace> getApplicationTraceIndexTrace() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE);
    }

    public TableDescriptor<HbaseColumnFamily.HostStatMap> getHostStatMap() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.HOST_APPLICATION_MAP_VER2_MAP);
    }


    public TableDescriptor<HbaseColumnFamily.CalleeStatMap> getCalleeStatMap() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER);
    }

    public TableDescriptor<HbaseColumnFamily.CallerStatMap> getCallerStatMap() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.MAP_STATISTICS_CALLER_VER2_COUNTER);
    }

    public TableDescriptor<HbaseColumnFamily.SelfStatMap> getSelfStatMap() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER);
    }

    public TableDescriptor<HbaseColumnFamily.SqlMetadataV2> getSqlMetadataV2() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.SQL_METADATA_VER2_SQL);
    }

    public TableDescriptor<HbaseColumnFamily.StringMetadataStr> getStringMetadataStr() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.STRING_METADATA_STR);
    }

    public TableDescriptor<HbaseColumnFamily.Trace> getTrace() {
        return new DefaultTableDescriptor<>(tableNameProvider, HbaseColumnFamily.TRACE_V2_SPAN);
    }

}
