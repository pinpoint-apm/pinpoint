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

import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author emeroad
 */
public final class HBaseTables {

    public static final int APPLICATION_NAME_MAX_LEN = PinpointConstants.APPLICATION_NAME_MAX_LEN;
    public static final int AGENT_NAME_MAX_LEN = PinpointConstants.AGENT_NAME_MAX_LEN;


    public static final String APPLICATION_TRACE_INDEX = "ApplicationTraceIndex";
    public static final byte[] APPLICATION_TRACE_INDEX_CF_TRACE = Bytes.toBytes("I"); // applicationIndex
    public static final int APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

    public static final String AGENT_STAT = "AgentStat";
    public static final byte[] AGENT_STAT_CF_STATISTICS = Bytes.toBytes("S"); // agent statistics column family
    // FIXME (2014.08) Legacy column for storing serialzied TAgentStat Thrift DTO.
    @Deprecated public static final byte[] AGENT_STAT_CF_STATISTICS_V1 = Bytes.toBytes("V1"); // qualifier
    // FIXME (2015.10) Legacy column for storing serialzied Bos separately.
    @Deprecated public static final byte[] AGENT_STAT_CF_STATISTICS_MEMORY_GC = Bytes.toBytes("Gc"); // qualifier for Heap Memory/Gc statistics
    @Deprecated public static final byte[] AGENT_STAT_CF_STATISTICS_CPU_LOAD = Bytes.toBytes("Cpu"); // qualifier for CPU load statistics
    public static final byte[] AGENT_STAT_COL_INTERVAL = Bytes.toBytes("int"); // qualifier for collection interval
    public static final byte[] AGENT_STAT_COL_GC_TYPE = Bytes.toBytes("gcT"); // qualifier for GC type
    public static final byte[] AGENT_STAT_COL_GC_OLD_COUNT = Bytes.toBytes("gcOldC"); // qualifier for GC old count
    public static final byte[] AGENT_STAT_COL_GC_OLD_TIME = Bytes.toBytes("gcOldT"); // qualifier for GC old time
    public static final byte[] AGENT_STAT_COL_HEAP_USED = Bytes.toBytes("hpU"); // gualifier for heap used
    public static final byte[] AGENT_STAT_COL_HEAP_MAX = Bytes.toBytes("hpM"); // qualifier for heap max
    public static final byte[] AGENT_STAT_COL_NON_HEAP_USED = Bytes.toBytes("nHpU"); // qualifier for non-heap used
    public static final byte[] AGENT_STAT_COL_NON_HEAP_MAX = Bytes.toBytes("nHpM"); // qualifier for non-heap max
    public static final byte[] AGENT_STAT_COL_JVM_CPU = Bytes.toBytes("jvmCpu"); // qualifier for JVM CPU usage
    public static final byte[] AGENT_STAT_COL_SYS_CPU = Bytes.toBytes("sysCpu"); // qualifier for system CPU usage
    public static final byte[] AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW = Bytes.toBytes("tSN"); // qualifier for sampled new count
    public static final byte[] AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION = Bytes.toBytes("tSC"); // qualifier for sampled continuation count
    public static final byte[] AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW = Bytes.toBytes("tUnSN"); // qualifier for unsampled new count
    public static final byte[] AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION = Bytes.toBytes("tUnSC"); // qualifier for unsampled continuation count
    public static final byte[] AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM = Bytes.toBytes("aH"); // qualifier for active trace histogram
    public static final int AGENT_STAT_ROW_DISTRIBUTE_SIZE = 1; // agent statistics hash size

    public static final String TRACES = "Traces";
    public static final byte[] TRACES_CF_SPAN = Bytes.toBytes("S");  //Span
    public static final byte[] TRACES_CF_ANNOTATION = Bytes.toBytes("A");  //Annotation
    public static final byte[] TRACES_CF_TERMINALSPAN = Bytes.toBytes("T"); //TerminalSpan

    public static final String APPLICATION_INDEX = "ApplicationIndex";
    public static final byte[] APPLICATION_INDEX_CF_AGENTS = Bytes.toBytes("Agents");

    public static final String AGENTINFO = "AgentInfo";
    public static final byte[] AGENTINFO_CF_INFO = Bytes.toBytes("Info");
    public static final byte[] AGENTINFO_CF_INFO_IDENTIFIER = Bytes.toBytes("i");
    public static final byte[] AGENTINFO_CF_INFO_SERVER_META_DATA = Bytes.toBytes("m");
    public static final byte[] AGENTINFO_CF_INFO_JVM = Bytes.toBytes("j");

    public static final String AGENT_LIFECYCLE = "AgentLifeCycle";
    public static final byte[] AGENT_LIFECYCLE_CF_STATUS = Bytes.toBytes("S"); // agent lifecycle column family
    public static final byte[] AGENT_LIFECYCLE_CF_STATUS_QUALI_STATES = Bytes.toBytes("states"); // qualifier for agent lifecycle states

    public static final String AGENT_EVENT = "AgentEvent";
    public static final byte[] AGENT_EVENT_CF_EVENTS = Bytes.toBytes("E"); // agent events column family

    @Deprecated
    public static final String AGENTID_APPLICATION_INDEX = "AgentIdApplicationIndex";
    @Deprecated
    public static final byte[] AGENTID_APPLICATION_INDEX_CF_APPLICATION = Bytes.toBytes("Application");


    public static final String SQL_METADATA_VER2 = "SqlMetaData_Ver2";
    public static final byte[] SQL_METADATA_VER2_CF_SQL = Bytes.toBytes("Sql");
    public static final byte[] SQL_METADATA_VER2_CF_SQL_QUALI_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");

    public static final String STRING_METADATA = "StringMetaData";
    public static final byte[] STRING_METADATA_CF_STR = Bytes.toBytes("Str");
    public static final byte[] STRING_METADATA_CF_STR_QUALI_STRING = Bytes.toBytes("P_string");

    public static final String API_METADATA = "ApiMetaData";
    public static final byte[] API_METADATA_CF_API = Bytes.toBytes("Api");
    public static final byte[] API_METADATA_CF_API_QUALI_SIGNATURE = Bytes.toBytes("P_api_signature");

    public static final String MAP_STATISTICS_CALLER_VER2 = "ApplicationMapStatisticsCaller_Ver2";
    public static final byte[] MAP_STATISTICS_CALLER_VER2_CF_COUNTER = Bytes.toBytes("C");

    public static final String MAP_STATISTICS_CALLEE_VER2 = "ApplicationMapStatisticsCallee_Ver2";
    public static final byte[] MAP_STATISTICS_CALLEE_VER2_CF_COUNTER = Bytes.toBytes("C");


    public static final String MAP_STATISTICS_SELF_VER2 = "ApplicationMapStatisticsSelf_Ver2";
    public static final byte[] MAP_STATISTICS_SELF_VER2_CF_COUNTER = Bytes.toBytes("C");

    public static final String HOST_APPLICATION_MAP_VER2 = "HostApplicationMap_Ver2";
    public static final byte[] HOST_APPLICATION_MAP_VER2_CF_MAP = Bytes.toBytes("M");

}
