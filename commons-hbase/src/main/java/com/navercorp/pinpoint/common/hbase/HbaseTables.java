/*
 * Copyright 2025 NAVER Corp.
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

import org.apache.hadoop.hbase.util.Bytes;

public class HbaseTables {

    public static final AgentInfo AGENTINFO_INFO = new AgentInfo(HbaseTableV2.AGENTINFO, Bytes.toBytes("Info"));
    public static class AgentInfo extends HbaseColumnFamily {
        public byte[] QUALIFIER_IDENTIFIER = Bytes.toBytes("i");
        public byte[] QUALIFIER_SERVER_META_DATA = Bytes.toBytes("m");
        public byte[] QUALIFIER_JVM = Bytes.toBytes("j");

        private AgentInfo(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily AGENT_EVENT_EVENTS = new HbaseColumnFamily(HbaseTableV2.AGENT_EVENT, Bytes.toBytes("E"));

    public static final AgentLifeCycleStatus AGENT_LIFECYCLE_STATUS = new AgentLifeCycleStatus(HbaseTableV2.AGENT_LIFECYCLE, Bytes.toBytes("S"));
    public static class AgentLifeCycleStatus extends HbaseColumnFamily {
        public static byte[] QUALIFIER_STATES = Bytes.toBytes("states");

        private AgentLifeCycleStatus(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final ApiMetadata API_METADATA_API = new ApiMetadata(HbaseTableV2.API_METADATA, Bytes.toBytes("Api"));
    public static class ApiMetadata extends HbaseColumnFamily {
        public byte[] QUALIFIER_SIGNATURE = Bytes.toBytes("P_api_signature");

        private ApiMetadata(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily APPLICATION_UID = new HbaseColumnFamily(HbaseTableV2.APPLICATION_UID, Bytes.toBytes("U"));

    public static final HbaseColumnFamily APPLICATION_UID_ATTR = new HbaseColumnFamily(HbaseTableV2.APPLICATION_UID_ATTR, Bytes.toBytes("A"));

    public static final HbaseColumnFamily AGENT_ID = new HbaseColumnFamily(HbaseTableV2.AGENT_ID, Bytes.toBytes("A"));

    public static final HbaseColumnFamily APPLICATION_INDEX_AGENTS = new HbaseColumnFamily(HbaseTableV2.APPLICATION_INDEX, Bytes.toBytes("Agents"));

    public static final ApplicationTraceIndexTrace APPLICATION_TRACE_INDEX_TRACE = new ApplicationTraceIndexTrace(HbaseTableV2.APPLICATION_TRACE_INDEX, Bytes.toBytes("I"));
    public static final ApplicationTraceIndexTrace APPLICATION_TRACE_INDEX_META = new ApplicationTraceIndexTrace(HbaseTableV2.APPLICATION_TRACE_INDEX, Bytes.toBytes("M"));
    public static class ApplicationTraceIndexTrace extends HbaseColumnFamily {
        public static final int ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

        private ApplicationTraceIndexTrace(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily HOST_APPLICATION_MAP_VER2_MAP = new HbaseColumnFamily(HbaseTableV2.HOST_APPLICATION_MAP_VER2, Bytes.toBytes("M"));

    public static final HbaseColumnFamily MAP_STATISTICS_CALLEE_VER2_COUNTER = new HbaseColumnFamily(HbaseTableV2.MAP_STATISTICS_CALLEE_VER2, Bytes.toBytes("C"));

    public static final HbaseColumnFamily MAP_STATISTICS_CALLER_VER2_COUNTER = new HbaseColumnFamily(HbaseTableV2.MAP_STATISTICS_CALLER_VER2, Bytes.toBytes("C"));

    public static final HbaseColumnFamily MAP_STATISTICS_SELF_VER2_COUNTER = new HbaseColumnFamily(HbaseTableV2.MAP_STATISTICS_SELF_VER2, Bytes.toBytes("C"));

    public static final SqlMetadataV2 SQL_METADATA_VER2_SQL = new SqlMetadataV2(HbaseTableV2.SQL_METADATA_VER2, Bytes.toBytes("Sql"));

    public static class SqlMetadataV2 extends HbaseColumnFamily {
        public byte[] QUALIFIER_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");

        private SqlMetadataV2(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final SqlUidMetaData SQL_UID_METADATA_SQL = new SqlUidMetaData(HbaseTableV2.SQL_UID_METADATA, Bytes.toBytes("Sql"));

    public static class SqlUidMetaData extends HbaseColumnFamily {
        public byte[] QUALIFIER_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");

        private SqlUidMetaData(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final StringMetadataStr STRING_METADATA_STR = new StringMetadataStr(HbaseTableV2.STRING_METADATA, Bytes.toBytes("Str"));

    public static class StringMetadataStr extends HbaseColumnFamily {
        public byte[] QUALIFIER_STRING = Bytes.toBytes("P_string");

        private StringMetadataStr(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily TRACE_V2_SPAN = new HbaseColumnFamily(HbaseTableV2.TRACE_V2, Bytes.toBytes("S"));
}
