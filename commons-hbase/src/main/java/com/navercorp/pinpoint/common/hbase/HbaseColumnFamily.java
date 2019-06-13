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

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author Taejin Koo
 */
public class HbaseColumnFamily {

    public static final AgentInfo AGENTINFO_INFO = new AgentInfo(HbaseTable.AGENTINFO, Bytes.toBytes("Info"));
    public static class AgentInfo extends HbaseColumnFamily {
        public static final byte[] QUALIFIER_IDENTIFIER = Bytes.toBytes("i");
        public static final byte[] QUALIFIER_SERVER_META_DATA = Bytes.toBytes("m");
        public static final byte[] QUALIFIER_JVM = Bytes.toBytes("j");

        public AgentInfo(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily AGENT_EVENT_EVENTS = HbaseTable.AGENT_EVENT.createColumnFamily(Bytes.toBytes("E"));

    public static final AgentLifeCycleStatus AGENT_LIFECYCLE_STATUS = new AgentLifeCycleStatus(HbaseTable.AGENT_LIFECYCLE, Bytes.toBytes("S"));
    public static class AgentLifeCycleStatus extends HbaseColumnFamily {
        public static final byte[] QUALIFIER_STATES = Bytes.toBytes("states");

        public AgentLifeCycleStatus(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final AgentStatStatistics AGENT_STAT_STATISTICS = new AgentStatStatistics(HbaseTable.AGENT_STAT_VER2, Bytes.toBytes("S"));
    public static class AgentStatStatistics extends HbaseColumnFamily {
        public static final int TIMESPAN_MS = 5 * 60 * 1000;

        public AgentStatStatistics(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final ApiMetadata API_METADATA_API = new ApiMetadata(HbaseTable.API_METADATA, Bytes.toBytes("Api"));
    public static class ApiMetadata extends HbaseColumnFamily {
        public static final byte[] QUALIFIER_SIGNATURE = Bytes.toBytes("P_api_signature");

        public ApiMetadata(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily APPLICATION_INDEX_AGENTS = HbaseTable.APPLICATION_INDEX.createColumnFamily(Bytes.toBytes("Agents"));

    public static final ApplicationStatStatistics APPLICATION_STAT_STATISTICS = new ApplicationStatStatistics(HbaseTable.APPLICATION_STAT_AGGRE, Bytes.toBytes("S"));
    public static class ApplicationStatStatistics extends HbaseColumnFamily {
        public static final int TIMESPAN_MS = 5 * 60 * 1000;

        public ApplicationStatStatistics(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final ApplicationTraceIndexTrace APPLICATION_TRACE_INDEX_TRACE = new ApplicationTraceIndexTrace(HbaseTable.APPLICATION_TRACE_INDEX, Bytes.toBytes("I"));
    public static class ApplicationTraceIndexTrace extends HbaseColumnFamily {
        public static final int ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

        public ApplicationTraceIndexTrace(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily HOST_APPLICATION_MAP_VER2_MAP = HbaseTable.HOST_APPLICATION_MAP_VER2.createColumnFamily(Bytes.toBytes("M"));

    public static final HbaseColumnFamily MAP_STATISTICS_CALLEE_VER2_COUNTER = HbaseTable.MAP_STATISTICS_CALLEE_VER2.createColumnFamily(Bytes.toBytes("C"));

    public static final HbaseColumnFamily MAP_STATISTICS_CALLER_VER2_COUNTER = HbaseTable.MAP_STATISTICS_CALLER_VER2.createColumnFamily(Bytes.toBytes("C"));

    public static final HbaseColumnFamily MAP_STATISTICS_SELF_VER2_COUNTER = HbaseTable.MAP_STATISTICS_SELF_VER2.createColumnFamily(Bytes.toBytes("C"));

    public static final SqlMetadataV2 SQL_METADATA_VER2_SQL = new SqlMetadataV2(HbaseTable.SQL_METADATA_VER2, Bytes.toBytes("Sql"));
    public static class SqlMetadataV2 extends HbaseColumnFamily {
        public static final byte[] QUALIFIER_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");

        public SqlMetadataV2(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final StringMetadataStr STRING_METADATA_STR = new StringMetadataStr(HbaseTable.STRING_METADATA, Bytes.toBytes("Str"));
    public static class StringMetadataStr extends HbaseColumnFamily {
        public static final byte[] QUALIFIER_STRING = Bytes.toBytes("P_string");

        public StringMetadataStr(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily TRACE_V2_SPAN = HbaseTable.TRACE_V2.createColumnFamily(Bytes.toBytes("S"));


    private final HbaseTable hBaseTable;
    private final byte[] columnFamilyName;

    HbaseColumnFamily(HbaseTable hBaseTable, byte[] columnFamilyName) {
        this.hBaseTable = Assert.requireNonNull(hBaseTable, "hBaseTable must not be null");
        Assert.isTrue(ArrayUtils.hasLength(columnFamilyName), "columnFamilyName must not be empty");
        this.columnFamilyName = columnFamilyName;
    }

    public HbaseTable getTable() {
        return hBaseTable;
    }

    public byte[] getName() {
        return columnFamilyName;
    }
}
