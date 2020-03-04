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

import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class HbaseTableTest {

    @Test
    public void agentInfoInfoTest() {
        HbaseColumnFamily.AgentInfo agentinfoInfo = HbaseColumnFamily.AGENTINFO_INFO;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("Info"), agentinfoInfo.getName()));
        Assert.assertEquals("AgentInfo", agentinfoInfo.getTable().getName());
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("m"), agentinfoInfo.QUALIFIER_SERVER_META_DATA));
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("i"), agentinfoInfo.QUALIFIER_IDENTIFIER));
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("j"), agentinfoInfo.QUALIFIER_JVM));
    }

    @Test
    public void agentEventEventsTest() {
        HbaseColumnFamily agentEventEvents = HbaseColumnFamily.AGENT_EVENT_EVENTS;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("E"), agentEventEvents.getName()));
        Assert.assertEquals("AgentEvent", agentEventEvents.getTable().getName());
    }

    @Test
    public void agentLifecycleStatusTest() {
        HbaseColumnFamily.AgentLifeCycleStatus agentLifecycleStatus = HbaseColumnFamily.AGENT_LIFECYCLE_STATUS;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("S"), agentLifecycleStatus.getName()));
        Assert.assertEquals("AgentLifeCycle", agentLifecycleStatus.getTable().getName());
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("states"), agentLifecycleStatus.QUALIFIER_STATES));
    }

    @Test
    public void agentStatStatisticsTest() {
        HbaseColumnFamily.AgentStatStatistics agentStatStatistics = HbaseColumnFamily.AGENT_STAT_STATISTICS;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("S"), agentStatStatistics.getName()));
        Assert.assertEquals("AgentStatV2", agentStatStatistics.getTable().getName());
        Assert.assertEquals(5 * 60 * 1000, agentStatStatistics.TIMESPAN_MS);
    }

    @Test
    public void apiMetadataApiTest() {
        HbaseColumnFamily.ApiMetadata apiMetadataApi = HbaseColumnFamily.API_METADATA_API;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("Api"), apiMetadataApi.getName()));
        Assert.assertEquals("ApiMetaData", apiMetadataApi.getTable().getName());
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("P_api_signature"), apiMetadataApi.QUALIFIER_SIGNATURE));
    }

    @Test
    public void applicationIndexAgentsTest() {
        HbaseColumnFamily applicationIndexAgents = HbaseColumnFamily.APPLICATION_INDEX_AGENTS;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("Agents"), applicationIndexAgents.getName()));
        Assert.assertEquals("ApplicationIndex", applicationIndexAgents.getTable().getName());
    }

    @Test
    public void applicationStatStatisticsTest() {
        HbaseColumnFamily.ApplicationStatStatistics applicationStatStatistics = HbaseColumnFamily.APPLICATION_STAT_STATISTICS;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("S"), applicationStatStatistics.getName()));
        Assert.assertEquals("ApplicationStatAggre", applicationStatStatistics.getTable().getName());
        Assert.assertEquals(5 * 60 * 1000, applicationStatStatistics.TIMESPAN_MS);
    }

    @Test
    public void applicationTraceIndexTraceTest() {
        HbaseColumnFamily.ApplicationTraceIndexTrace applicationTraceIndexTrace = HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("I"), applicationTraceIndexTrace.getName()));
        Assert.assertEquals("ApplicationTraceIndex", applicationTraceIndexTrace.getTable().getName());
        Assert.assertEquals(1, applicationTraceIndexTrace.ROW_DISTRIBUTE_SIZE);
    }

    @Test
    public void hostApplicationMapVer2MapTest() {
        HbaseColumnFamily hostApplicationMapVer2Map = HbaseColumnFamily.HOST_APPLICATION_MAP_VER2_MAP;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("M"), hostApplicationMapVer2Map.getName()));
        Assert.assertEquals("HostApplicationMap_Ver2", hostApplicationMapVer2Map.getTable().getName());
    }

    @Test
    public void mapStatisticsCalleeVer2CounterTest() {
        HbaseColumnFamily mapStatisticsCalleeVer2Counter = HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("C"), mapStatisticsCalleeVer2Counter.getName()));
        Assert.assertEquals("ApplicationMapStatisticsCallee_Ver2", mapStatisticsCalleeVer2Counter.getTable().getName());
    }

    @Test
    public void mapStatisticsCallerVer2CounterTest() {
        HbaseColumnFamily mapStatisticsCallerVer2Counter = HbaseColumnFamily.MAP_STATISTICS_CALLER_VER2_COUNTER;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("C"), mapStatisticsCallerVer2Counter.getName()));
        Assert.assertEquals("ApplicationMapStatisticsCaller_Ver2", mapStatisticsCallerVer2Counter.getTable().getName());
    }

    @Test
    public void mapStatisticsSelfVer2CounterTest() {
        HbaseColumnFamily mapStatisticsSelfVer2Counter = HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("C"), mapStatisticsSelfVer2Counter.getName()));
        Assert.assertEquals("ApplicationMapStatisticsSelf_Ver2", mapStatisticsSelfVer2Counter.getTable().getName());
    }

    @Test
    public void sqlMetadataVer2SqlTest() {
        HbaseColumnFamily.SqlMetadataV2 sqlMetadataVer2Sql = HbaseColumnFamily.SQL_METADATA_VER2_SQL;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("Sql"), sqlMetadataVer2Sql.getName()));
        Assert.assertEquals("SqlMetaData_Ver2", sqlMetadataVer2Sql.getTable().getName());
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("P_sql_statement"), sqlMetadataVer2Sql.QUALIFIER_SQLSTATEMENT));
    }

    @Test
    public void stringMetadataStrTest() {
        HbaseColumnFamily.StringMetadataStr stringMetadataStr = HbaseColumnFamily.STRING_METADATA_STR;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("Str"), stringMetadataStr.getName()));
        Assert.assertEquals("StringMetaData", stringMetadataStr.getTable().getName());
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("P_string"), stringMetadataStr.QUALIFIER_STRING));
    }

    @Test
    public void traceV2SpanTest() {
        HbaseColumnFamily traceV2Span = HbaseColumnFamily.TRACE_V2_SPAN;
        Assert.assertTrue(Arrays.equals(Bytes.toBytes("S"), traceV2Span.getName()));
        Assert.assertEquals("TraceV2", traceV2Span.getTable().getName());
    }

}
