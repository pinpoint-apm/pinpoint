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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class HbaseTableTest {

//    public static final HbaseColumnFamily.AgentInfo AGENTINFO_INFO = new HbaseColumnFamily.AgentInfo(HbaseTable.AGENTINFO, Bytes.toBytes("Info"));
//        public static final byte[] QUALIFIER_IDENTIFIER = Bytes.toBytes("i");
//        public static final byte[] QUALIFIER_SERVER_META_DATA = Bytes.toBytes("m");
//        public static final byte[] QUALIFIER_JVM = Bytes.toBytes("j");
    @Test
    public void agentInfoInfoTest() {
        HbaseColumnFamily.AgentInfo agentinfoInfo = HbaseColumnFamily.AGENTINFO_INFO;
        Assert.assertTrue(Arrays.equals(HBaseTables.AGENTINFO_CF_INFO, agentinfoInfo.getName()));
        Assert.assertEquals(HBaseTables.AGENTINFO_STR, agentinfoInfo.getTable().getName());
        Assert.assertTrue(Arrays.equals(HBaseTables.AGENTINFO_CF_INFO_SERVER_META_DATA, agentinfoInfo.QUALIFIER_SERVER_META_DATA));
        Assert.assertTrue(Arrays.equals(HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER, agentinfoInfo.QUALIFIER_IDENTIFIER));
        Assert.assertTrue(Arrays.equals(HBaseTables.AGENTINFO_CF_INFO_JVM, agentinfoInfo.QUALIFIER_JVM));
    }

//    public static final HbaseColumnFamily AGENT_EVENT_EVENTS = HbaseTable.AGENT_EVENT.createColumnFamily(Bytes.toBytes("E"));
    @Test
    public void agentEventEventsTest() {
        HbaseColumnFamily agentEventEvents = HbaseColumnFamily.AGENT_EVENT_EVENTS;
        Assert.assertTrue(Arrays.equals(HBaseTables.AGENT_EVENT_CF_EVENTS, agentEventEvents.getName()));
        Assert.assertEquals(HBaseTables.AGENT_EVENT_STR, agentEventEvents.getTable().getName());
    }

//    public static final HbaseColumnFamily.AgentLifeCycleStatus AGENT_LIFECYCLE_STATUS = new HbaseColumnFamily.AgentLifeCycleStatus(HbaseTable.AGENT_LIFECYCLE, Bytes.toBytes("S"));
//        public static final byte[] QUALIFIER_STATES = Bytes.toBytes("states");
    @Test
    public void agentLifecycleStatusTest() {
        HbaseColumnFamily.AgentLifeCycleStatus agentLifecycleStatus = HbaseColumnFamily.AGENT_LIFECYCLE_STATUS;
        Assert.assertTrue(Arrays.equals(HBaseTables.AGENT_LIFECYCLE_CF_STATUS, agentLifecycleStatus.getName()));
        Assert.assertEquals(HBaseTables.AGENT_LIFECYCLE_STR, agentLifecycleStatus.getTable().getName());
        Assert.assertTrue(Arrays.equals(HBaseTables.AGENT_LIFECYCLE_CF_STATUS_QUALI_STATES, agentLifecycleStatus.QUALIFIER_STATES));
    }

//    public static final HbaseColumnFamily.AgentStatStatistics AGENT_STAT_STATISTICS = new HbaseColumnFamily.AgentStatStatistics(HbaseTable.AGENT_STAT_VER2, Bytes.toBytes("S"));
//        public static final int TIMESPAN_MS = 5 * 60 * 1000;
    @Test
    public void agentStatStatisticsTest() {
        HbaseColumnFamily.AgentStatStatistics agentStatStatistics = HbaseColumnFamily.AGENT_STAT_STATISTICS;
        Assert.assertTrue(Arrays.equals(HBaseTables.AGENT_STAT_CF_STATISTICS, agentStatStatistics.getName()));
        Assert.assertEquals(HBaseTables.AGENT_STAT_VER2_STR, agentStatStatistics.getTable().getName());
        Assert.assertEquals(HBaseTables.AGENT_STAT_TIMESPAN_MS, agentStatStatistics.TIMESPAN_MS);
    }

//    public static final HbaseColumnFamily.ApiMetadata API_METADATA_API = new HbaseColumnFamily.ApiMetadata(HbaseTable.API_METADATA, Bytes.toBytes("Api"));
//        public static final byte[] QUALIFIER_SIGNATURE = Bytes.toBytes("P_api_signature");
    @Test
    public void apiMetadataApiTest() {
        HbaseColumnFamily.ApiMetadata apiMetadataApi = HbaseColumnFamily.API_METADATA_API;
        Assert.assertTrue(Arrays.equals(HBaseTables.API_METADATA_CF_API, apiMetadataApi.getName()));
        Assert.assertEquals(HBaseTables.API_METADATA_STR, apiMetadataApi.getTable().getName());
        Assert.assertTrue(Arrays.equals(HBaseTables.API_METADATA_CF_API_QUALI_SIGNATURE, apiMetadataApi.QUALIFIER_SIGNATURE));
    }

//    public static final HbaseColumnFamily APPLICATION_INDEX_AGENTS = HbaseTable.APPLICATION_INDEX.createColumnFamily(Bytes.toBytes("Agents"));
    @Test
    public void applicationIndexAgentsTest() {
        HbaseColumnFamily applicationIndexAgents = HbaseColumnFamily.APPLICATION_INDEX_AGENTS;
        Assert.assertTrue(Arrays.equals(HBaseTables.APPLICATION_INDEX_CF_AGENTS, applicationIndexAgents.getName()));
        Assert.assertEquals(HBaseTables.APPLICATION_INDEX_STR, applicationIndexAgents.getTable().getName());
    }

//    public static final HbaseColumnFamily.ApplicationStatStatistics APPLICATION_STAT_STATISTICS = new HbaseColumnFamily.ApplicationStatStatistics(HbaseTable.APPLICATION_STAT_AGGRE, Bytes.toBytes("S"));
//        public static final int TIMESPAN_MS = 5 * 60 * 1000;
    @Test
    public void applicationStatStatisticsTest() {
        HbaseColumnFamily.ApplicationStatStatistics applicationStatStatistics = HbaseColumnFamily.APPLICATION_STAT_STATISTICS;
        Assert.assertTrue(Arrays.equals(HBaseTables.APPLICATION_STAT_CF_STATISTICS, applicationStatStatistics.getName()));
        Assert.assertEquals(HBaseTables.APPLICATION_STAT_AGGRE_STR, applicationStatStatistics.getTable().getName());
        Assert.assertEquals(HBaseTables.APPLICATION_STAT_TIMESPAN_MS, applicationStatStatistics.TIMESPAN_MS);
    }

//    public static final HbaseColumnFamily.ApplicationTraceIndexTrace APPLICATION_TRACE_INDEX_TRACE = new HbaseColumnFamily.ApplicationTraceIndexTrace(HbaseTable.APPLICATION_TRACE_INDEX, Bytes.toBytes("I"));
//        public static final int ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size
    @Test
    public void applicationTraceIndexTraceTest() {
        HbaseColumnFamily.ApplicationTraceIndexTrace applicationTraceIndexTrace = HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE;
        Assert.assertTrue(Arrays.equals(HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE, applicationTraceIndexTrace.getName()));
        Assert.assertEquals(HBaseTables.APPLICATION_TRACE_INDEX_STR, applicationTraceIndexTrace.getTable().getName());
        Assert.assertEquals(HBaseTables.APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE, applicationTraceIndexTrace.ROW_DISTRIBUTE_SIZE);
    }

//    public static final HbaseColumnFamily HOST_APPLICATION_MAP_VER2_MAP = HbaseTable.HOST_APPLICATION_MAP_VER2.createColumnFamily(Bytes.toBytes("M"));
    @Test
    public void hostApplicationMapVer2MapTest() {
        HbaseColumnFamily hostApplicationMapVer2Map = HbaseColumnFamily.HOST_APPLICATION_MAP_VER2_MAP;
        Assert.assertTrue(Arrays.equals(HBaseTables.HOST_APPLICATION_MAP_VER2_CF_MAP, hostApplicationMapVer2Map.getName()));
        Assert.assertEquals(HBaseTables.HOST_APPLICATION_MAP_VER2_STR, hostApplicationMapVer2Map.getTable().getName());
    }

//    public static final HbaseColumnFamily MAP_STATISTICS_CALLEE_VER2_COUNTER = HbaseTable.MAP_STATISTICS_CALLEE_VER2.createColumnFamily(Bytes.toBytes("C"));
    @Test
    public void mapStatisticsCalleeVer2CounterTest() {
        HbaseColumnFamily mapStatisticsCalleeVer2Counter = HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        Assert.assertTrue(Arrays.equals(HBaseTables.MAP_STATISTICS_CALLEE_VER2_CF_COUNTER, mapStatisticsCalleeVer2Counter.getName()));
        Assert.assertEquals(HBaseTables.MAP_STATISTICS_CALLEE_VER2_STR, mapStatisticsCalleeVer2Counter.getTable().getName());
    }

//    public static final HbaseColumnFamily MAP_STATISTICS_CALLER_VER2_COUNTER = HbaseTable.MAP_STATISTICS_CALLER_VER2.createColumnFamily(Bytes.toBytes("C"));
    @Test
    public void mapStatisticsCallerVer2CounterTest() {
        HbaseColumnFamily mapStatisticsCallerVer2Counter = HbaseColumnFamily.MAP_STATISTICS_CALLER_VER2_COUNTER;
        Assert.assertTrue(Arrays.equals(HBaseTables.MAP_STATISTICS_CALLER_VER2_CF_COUNTER, mapStatisticsCallerVer2Counter.getName()));
        Assert.assertEquals(HBaseTables.MAP_STATISTICS_CALLER_VER2_STR, mapStatisticsCallerVer2Counter.getTable().getName());
    }

//    public static final HbaseColumnFamily MAP_STATISTICS_SELF_VER2_COUNTER = HbaseTable.MAP_STATISTICS_SELF_VER2.createColumnFamily(Bytes.toBytes("C"));
    @Test
    public void mapStatisticsSelfVer2CounterTest() {
        HbaseColumnFamily mapStatisticsSelfVer2Counter = HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER;
        Assert.assertTrue(Arrays.equals(HBaseTables.MAP_STATISTICS_SELF_VER2_CF_COUNTER, mapStatisticsSelfVer2Counter.getName()));
        Assert.assertEquals(HBaseTables.MAP_STATISTICS_SELF_VER2_STR, mapStatisticsSelfVer2Counter.getTable().getName());
    }

//    public static final HbaseColumnFamily.SqlMetadataV2 SQL_METADATA_VER2_SQL = new HbaseColumnFamily.SqlMetadataV2(HbaseTable.SQL_METADATA_VER2, Bytes.toBytes("Sql"));
//        public static final byte[] QUALIFIER_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");
    @Test
    public void sqlMetadataVer2SqlTest() {
        HbaseColumnFamily.SqlMetadataV2 sqlMetadataVer2Sql = HbaseColumnFamily.SQL_METADATA_VER2_SQL;
        Assert.assertTrue(Arrays.equals(HBaseTables.SQL_METADATA_VER2_CF_SQL, sqlMetadataVer2Sql.getName()));
        Assert.assertEquals(HBaseTables.SQL_METADATA_VER2_STR, sqlMetadataVer2Sql.getTable().getName());
        Assert.assertTrue(Arrays.equals(HBaseTables.SQL_METADATA_VER2_CF_SQL_QUALI_SQLSTATEMENT, sqlMetadataVer2Sql.QUALIFIER_SQLSTATEMENT));
    }

//    public static final HbaseColumnFamily.StringMetadataStr STRING_METADATA_STR = new HbaseColumnFamily.StringMetadataStr(HbaseTable.STRING_METADATA, Bytes.toBytes("Str"));
//        public static final byte[] QUALIFIER_STRING = Bytes.toBytes("P_string");
    @Test
    public void stringMetadataStrTest() {
        HbaseColumnFamily.StringMetadataStr stringMetadataStr = HbaseColumnFamily.STRING_METADATA_STR;
        Assert.assertTrue(Arrays.equals(HBaseTables.STRING_METADATA_CF_STR, stringMetadataStr.getName()));
        Assert.assertEquals(HBaseTables.STRING_METADATA_STR, stringMetadataStr.getTable().getName());
        Assert.assertTrue(Arrays.equals(HBaseTables.STRING_METADATA_CF_STR_QUALI_STRING, stringMetadataStr.QUALIFIER_STRING));
    }

//    public static final HbaseColumnFamily TRACE_V2_SPAN = HbaseTable.TRACE_V2.createColumnFamily(Bytes.toBytes("S"));
    @Test
    public void traceV2SpanTest() {
        HbaseColumnFamily traceV2Span = HbaseColumnFamily.TRACE_V2_SPAN;
        Assert.assertTrue(Arrays.equals(HBaseTables.TRACE_V2_CF_SPAN, traceV2Span.getName()));
        Assert.assertEquals(HBaseTables.TRACE_V2_STR, traceV2Span.getTable().getName());
    }

}
