/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.PinpointConstants;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author emeroad
 */
final class HBaseTables {

    public static final int APPLICATION_NAME_MAX_LEN = PinpointConstants.APPLICATION_NAME_MAX_LEN;
    public static final int AGENT_NAME_MAX_LEN = PinpointConstants.AGENT_NAME_MAX_LEN;

    // Time delta (in milliseconds) we can store in each row of AgentStatV2
    public static final int AGENT_STAT_TIMESPAN_MS = 5 * 60 * 1000;


    public static final String APPLICATION_TRACE_INDEX_STR = "ApplicationTraceIndex";
    @Deprecated
    public static final TableName APPLICATION_TRACE_INDEX = TableName.valueOf(APPLICATION_TRACE_INDEX_STR);
    public static final byte[] APPLICATION_TRACE_INDEX_CF_TRACE = Bytes.toBytes("I"); // applicationIndex
    public static final int APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

    public static final String AGENT_STAT_VER2_STR = "AgentStatV2";
    @Deprecated
    public static final TableName AGENT_STAT_VER2 = TableName.valueOf(AGENT_STAT_VER2_STR);
    public static final byte[] AGENT_STAT_CF_STATISTICS = Bytes.toBytes("S"); // agent statistics column family

    public static final String TRACE_V2_STR = "TraceV2";
    @Deprecated
    public static final TableName TRACE_V2 = TableName.valueOf(TRACE_V2_STR);
    public static final byte[] TRACE_V2_CF_SPAN = Bytes.toBytes("S");  //Span

    public static final String APPLICATION_INDEX_STR = "ApplicationIndex";
    @Deprecated
    public static final TableName APPLICATION_INDEX = TableName.valueOf(APPLICATION_INDEX_STR);
    public static final byte[] APPLICATION_INDEX_CF_AGENTS = Bytes.toBytes("Agents");

    public static final String AGENTINFO_STR = "AgentInfo";
    @Deprecated
    public static final TableName AGENTINFO = TableName.valueOf(AGENTINFO_STR);
    public static final byte[] AGENTINFO_CF_INFO = Bytes.toBytes("Info");
    public static final byte[] AGENTINFO_CF_INFO_IDENTIFIER = Bytes.toBytes("i");
    public static final byte[] AGENTINFO_CF_INFO_SERVER_META_DATA = Bytes.toBytes("m");
    public static final byte[] AGENTINFO_CF_INFO_JVM = Bytes.toBytes("j");

    public static final String AGENT_LIFECYCLE_STR = "AgentLifeCycle";
    @Deprecated
    public static final TableName AGENT_LIFECYCLE = TableName.valueOf(AGENT_LIFECYCLE_STR);
    public static final byte[] AGENT_LIFECYCLE_CF_STATUS = Bytes.toBytes("S"); // agent lifecycle column family
    public static final byte[] AGENT_LIFECYCLE_CF_STATUS_QUALI_STATES = Bytes.toBytes("states"); // qualifier for agent lifecycle states

    public static final String AGENT_EVENT_STR = "AgentEvent";
    @Deprecated
    public static final TableName AGENT_EVENT = TableName.valueOf(AGENT_EVENT_STR);
    public static final byte[] AGENT_EVENT_CF_EVENTS = Bytes.toBytes("E"); // agent events column family

    public static final String SQL_METADATA_VER2_STR = "SqlMetaData_Ver2";
    @Deprecated
    public static final TableName SQL_METADATA_VER2 = TableName.valueOf(SQL_METADATA_VER2_STR);
    public static final byte[] SQL_METADATA_VER2_CF_SQL = Bytes.toBytes("Sql");
    public static final byte[] SQL_METADATA_VER2_CF_SQL_QUALI_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");

    public static final String STRING_METADATA_STR = "StringMetaData";
    @Deprecated
    public static final TableName STRING_METADATA = TableName.valueOf(STRING_METADATA_STR);
    public static final byte[] STRING_METADATA_CF_STR = Bytes.toBytes("Str");
    public static final byte[] STRING_METADATA_CF_STR_QUALI_STRING = Bytes.toBytes("P_string");

    public static final String API_METADATA_STR = "ApiMetaData";
    @Deprecated
    public static final TableName API_METADATA = TableName.valueOf(API_METADATA_STR);
    public static final byte[] API_METADATA_CF_API = Bytes.toBytes("Api");
    public static final byte[] API_METADATA_CF_API_QUALI_SIGNATURE = Bytes.toBytes("P_api_signature");

    public static final String MAP_STATISTICS_CALLER_VER2_STR = "ApplicationMapStatisticsCaller_Ver2";
    @Deprecated
    public static final TableName MAP_STATISTICS_CALLER_VER2 = TableName.valueOf(MAP_STATISTICS_CALLER_VER2_STR);
    public static final byte[] MAP_STATISTICS_CALLER_VER2_CF_COUNTER = Bytes.toBytes("C");

    public static final String MAP_STATISTICS_CALLEE_VER2_STR = "ApplicationMapStatisticsCallee_Ver2";
    @Deprecated
    public static final TableName MAP_STATISTICS_CALLEE_VER2 = TableName.valueOf(MAP_STATISTICS_CALLEE_VER2_STR);
    public static final byte[] MAP_STATISTICS_CALLEE_VER2_CF_COUNTER = Bytes.toBytes("C");

    public static final String MAP_STATISTICS_SELF_VER2_STR = "ApplicationMapStatisticsSelf_Ver2";
    @Deprecated
    public static final TableName MAP_STATISTICS_SELF_VER2 = TableName.valueOf(MAP_STATISTICS_SELF_VER2_STR);
    public static final byte[] MAP_STATISTICS_SELF_VER2_CF_COUNTER = Bytes.toBytes("C");

    public static final String HOST_APPLICATION_MAP_VER2_STR = "HostApplicationMap_Ver2";
    @Deprecated
    public static final TableName HOST_APPLICATION_MAP_VER2 = TableName.valueOf(HOST_APPLICATION_MAP_VER2_STR);
    public static final byte[] HOST_APPLICATION_MAP_VER2_CF_MAP = Bytes.toBytes("M");

    public static final int APPLICATION_STAT_TIMESPAN_MS = 5 * 60 * 1000;
    public static final String APPLICATION_STAT_AGGRE_STR = "ApplicationStatAggre";
    @Deprecated
    public static final TableName APPLICATION_STAT_AGGRE = TableName.valueOf(APPLICATION_STAT_AGGRE_STR);
    public static final byte[] APPLICATION_STAT_CF_STATISTICS = Bytes.toBytes("S");

}
