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
public final class HBaseTables {

    public static final int APPLICATION_NAME_MAX_LEN = PinpointConstants.APPLICATION_NAME_MAX_LEN;
    public static final int AGENT_NAME_MAX_LEN = PinpointConstants.AGENT_NAME_MAX_LEN;

    // Time delta (in milliseconds) we can store in each row of AgentStatV2
    public static final int AGENT_STAT_TIMESPAN_MS = 5 * 60 * 1000;

    public static final TableName APPLICATION_TRACE_INDEX = TableName.valueOf("ApplicationTraceIndex");
    public static final byte[] APPLICATION_TRACE_INDEX_CF_TRACE = Bytes.toBytes("I"); // applicationIndex
    public static final int APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

    @Deprecated public static final TableName AGENT_STAT = TableName.valueOf("AgentStat");
    public static final TableName AGENT_STAT_VER2 = TableName.valueOf("AgentStatV2");

    public static final byte[] AGENT_STAT_CF_STATISTICS = Bytes.toBytes("S"); // agent statistics column family
    // FIXME (2014.08) Legacy column for storing serialzied TAgentStat Thrift DTO.
    @Deprecated public static final byte[] AGENT_STAT_CF_STATISTICS_V1 = Bytes.toBytes("V1"); // qualifier
    // FIXME (2015.10) Legacy column for storing serialzied Bos separately.
    @Deprecated public static final byte[] AGENT_STAT_CF_STATISTICS_MEMORY_GC = Bytes.toBytes("Gc"); // qualifier for Heap Memory/Gc statistics
    @Deprecated public static final byte[] AGENT_STAT_CF_STATISTICS_CPU_LOAD = Bytes.toBytes("Cpu"); // qualifier for CPU load statistics
    // FIXME (2016.06) Legacy column for storing stat data directly to columns
    @Deprecated public static final byte[] AGENT_STAT_COL_INTERVAL = Bytes.toBytes("int"); // qualifier for collection interval
    @Deprecated public static final byte[] AGENT_STAT_COL_GC_TYPE = Bytes.toBytes("gcT"); // qualifier for GC type
    @Deprecated public static final byte[] AGENT_STAT_COL_GC_OLD_COUNT = Bytes.toBytes("gcOldC"); // qualifier for GC old count
    @Deprecated public static final byte[] AGENT_STAT_COL_GC_OLD_TIME = Bytes.toBytes("gcOldT"); // qualifier for GC old time
    @Deprecated public static final byte[] AGENT_STAT_COL_HEAP_USED = Bytes.toBytes("hpU"); // gualifier for heap used
    @Deprecated public static final byte[] AGENT_STAT_COL_HEAP_MAX = Bytes.toBytes("hpM"); // qualifier for heap max
    @Deprecated public static final byte[] AGENT_STAT_COL_NON_HEAP_USED = Bytes.toBytes("nHpU"); // qualifier for non-heap used
    @Deprecated public static final byte[] AGENT_STAT_COL_NON_HEAP_MAX = Bytes.toBytes("nHpM"); // qualifier for non-heap max
    @Deprecated public static final byte[] AGENT_STAT_COL_JVM_CPU = Bytes.toBytes("jvmCpu"); // qualifier for JVM CPU usage
    @Deprecated public static final byte[] AGENT_STAT_COL_SYS_CPU = Bytes.toBytes("sysCpu"); // qualifier for system CPU usage
    @Deprecated public static final byte[] AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW = Bytes.toBytes("tSN"); // qualifier for sampled new count
    @Deprecated public static final byte[] AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION = Bytes.toBytes("tSC"); // qualifier for sampled continuation count
    @Deprecated public static final byte[] AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW = Bytes.toBytes("tUnSN"); // qualifier for unsampled new count
    @Deprecated public static final byte[] AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION = Bytes.toBytes("tUnSC"); // qualifier for unsampled continuation count
    @Deprecated public static final byte[] AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM = Bytes.toBytes("aH"); // qualifier for active trace histogram

    @Deprecated
    public static final TableName TRACES = TableName.valueOf("Traces");
    @Deprecated
    public static final byte[] TRACES_CF_SPAN = Bytes.toBytes("S");  //Span
    @Deprecated
    public static final byte[] TRACES_CF_ANNOTATION = Bytes.toBytes("A");  //Annotation
    @Deprecated
    public static final byte[] TRACES_CF_TERMINALSPAN = Bytes.toBytes("T"); //SpanEvent

    public static final TableName TRACE_V2 = TableName.valueOf("TraceV2");
    public static final byte[] TRACE_V2_CF_SPAN = Bytes.toBytes("S");  //Span

    public static final TableName APPLICATION_INDEX = TableName.valueOf("ApplicationIndex");
    public static final byte[] APPLICATION_INDEX_CF_AGENTS = Bytes.toBytes("Agents");

    public static final TableName AGENTINFO = TableName.valueOf("AgentInfo");
    public static final byte[] AGENTINFO_CF_INFO = Bytes.toBytes("Info");
    public static final byte[] AGENTINFO_CF_INFO_IDENTIFIER = Bytes.toBytes("i");
    public static final byte[] AGENTINFO_CF_INFO_SERVER_META_DATA = Bytes.toBytes("m");
    public static final byte[] AGENTINFO_CF_INFO_JVM = Bytes.toBytes("j");

    public static final TableName AGENT_LIFECYCLE = TableName.valueOf("AgentLifeCycle");
    public static final byte[] AGENT_LIFECYCLE_CF_STATUS = Bytes.toBytes("S"); // agent lifecycle column family
    public static final byte[] AGENT_LIFECYCLE_CF_STATUS_QUALI_STATES = Bytes.toBytes("states"); // qualifier for agent lifecycle states

    public static final TableName AGENT_EVENT = TableName.valueOf("AgentEvent");
    public static final byte[] AGENT_EVENT_CF_EVENTS = Bytes.toBytes("E"); // agent events column family

    @Deprecated
    public static final TableName AGENTID_APPLICATION_INDEX = TableName.valueOf("AgentIdApplicationIndex");
    @Deprecated
    public static final byte[] AGENTID_APPLICATION_INDEX_CF_APPLICATION = Bytes.toBytes("Application");


    public static final TableName SQL_METADATA_VER2 = TableName.valueOf("SqlMetaData_Ver2");
    public static final byte[] SQL_METADATA_VER2_CF_SQL = Bytes.toBytes("Sql");
    public static final byte[] SQL_METADATA_VER2_CF_SQL_QUALI_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");

    public static final TableName STRING_METADATA = TableName.valueOf("StringMetaData");
    public static final byte[] STRING_METADATA_CF_STR = Bytes.toBytes("Str");
    public static final byte[] STRING_METADATA_CF_STR_QUALI_STRING = Bytes.toBytes("P_string");

    public static final TableName API_METADATA = TableName.valueOf("ApiMetaData");
    public static final byte[] API_METADATA_CF_API = Bytes.toBytes("Api");
    public static final byte[] API_METADATA_CF_API_QUALI_SIGNATURE = Bytes.toBytes("P_api_signature");

    public static final TableName MAP_STATISTICS_CALLER_VER2 = TableName.valueOf("ApplicationMapStatisticsCaller_Ver2");
    public static final byte[] MAP_STATISTICS_CALLER_VER2_CF_COUNTER = Bytes.toBytes("C");

    public static final TableName MAP_STATISTICS_CALLEE_VER2 = TableName.valueOf("ApplicationMapStatisticsCallee_Ver2");
    public static final byte[] MAP_STATISTICS_CALLEE_VER2_CF_COUNTER = Bytes.toBytes("C");

    public static final TableName MAP_STATISTICS_SELF_VER2 = TableName.valueOf("ApplicationMapStatisticsSelf_Ver2");
    public static final byte[] MAP_STATISTICS_SELF_VER2_CF_COUNTER = Bytes.toBytes("C");

    public static final TableName HOST_APPLICATION_MAP_VER2 = TableName.valueOf("HostApplicationMap_Ver2");
    public static final byte[] HOST_APPLICATION_MAP_VER2_CF_MAP = Bytes.toBytes("M");

    public static final int APPLICATION_STAT_TIMESPAN_MS = 5 * 60 * 1000;
    public static final TableName APPLICATION_STAT_AGGRE = TableName.valueOf("ApplicationStatAggre");
    public static final byte[] APPLICATION_STAT_CF_STATISTICS = Bytes.toBytes("S");

}
