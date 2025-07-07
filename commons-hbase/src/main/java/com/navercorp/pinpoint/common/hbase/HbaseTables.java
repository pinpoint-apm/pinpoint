package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.util.Bytes;

public class HbaseTables {

    public static final AgentInfo AGENTINFO_INFO = new AgentInfo(HbaseTable.AGENTINFO, Bytes.toBytes("Info"));
    public static class AgentInfo extends HbaseColumnFamily {
        public byte[] QUALIFIER_IDENTIFIER = Bytes.toBytes("i");
        public byte[] QUALIFIER_SERVER_META_DATA = Bytes.toBytes("m");
        public byte[] QUALIFIER_JVM = Bytes.toBytes("j");

        private AgentInfo(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily AGENT_EVENT_EVENTS = new HbaseColumnFamily(HbaseTable.AGENT_EVENT, Bytes.toBytes("E"));

    public static final AgentLifeCycleStatus AGENT_LIFECYCLE_STATUS = new AgentLifeCycleStatus(HbaseTable.AGENT_LIFECYCLE, Bytes.toBytes("S"));
    public static class AgentLifeCycleStatus extends HbaseColumnFamily {
        public static byte[] QUALIFIER_STATES = Bytes.toBytes("states");

        private AgentLifeCycleStatus(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final ApiMetadata API_METADATA_API = new ApiMetadata(HbaseTable.API_METADATA, Bytes.toBytes("Api"));
    public static class ApiMetadata extends HbaseColumnFamily {
        public byte[] QUALIFIER_SIGNATURE = Bytes.toBytes("P_api_signature");

        private ApiMetadata(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily APPLICATION_UID = new HbaseColumnFamily(HbaseTable.APPLICATION_UID, Bytes.toBytes("I"));

    public static final HbaseColumnFamily APPLICATION_NAME = new HbaseColumnFamily(HbaseTable.APPLICATION_NAME, Bytes.toBytes("N"));

    public static final HbaseColumnFamily AGENT_NAME = new HbaseColumnFamily(HbaseTable.AGENT_NAME, Bytes.toBytes("A"));

    public static final HbaseColumnFamily APPLICATION_INDEX_AGENTS = new HbaseColumnFamily(HbaseTable.APPLICATION_INDEX, Bytes.toBytes("Agents"));

    public static final ApplicationTraceIndexTrace APPLICATION_TRACE_INDEX_TRACE = new ApplicationTraceIndexTrace(HbaseTable.APPLICATION_TRACE_INDEX, Bytes.toBytes("I"));
    public static final ApplicationTraceIndexTrace APPLICATION_TRACE_INDEX_META = new ApplicationTraceIndexTrace(HbaseTable.APPLICATION_TRACE_INDEX, Bytes.toBytes("M"));
    public static class ApplicationTraceIndexTrace extends HbaseColumnFamily {
        public static final int ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

        private ApplicationTraceIndexTrace(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily HOST_APPLICATION_MAP_VER2_MAP = new HbaseColumnFamily(HbaseTable.HOST_APPLICATION_MAP_VER2, Bytes.toBytes("M"));

    public static final HbaseColumnFamily MAP_STATISTICS_CALLEE_VER2_COUNTER = new HbaseColumnFamily(HbaseTable.MAP_STATISTICS_CALLEE_VER2, Bytes.toBytes("C"));

    public static final HbaseColumnFamily MAP_STATISTICS_CALLER_VER2_COUNTER = new HbaseColumnFamily(HbaseTable.MAP_STATISTICS_CALLER_VER2, Bytes.toBytes("C"));

    public static final HbaseColumnFamily MAP_STATISTICS_SELF_VER2_COUNTER = new HbaseColumnFamily(HbaseTable.MAP_STATISTICS_SELF_VER2, Bytes.toBytes("C"));

    public static final SqlMetadataV2 SQL_METADATA_VER2_SQL = new SqlMetadataV2(HbaseTable.SQL_METADATA_VER2, Bytes.toBytes("Sql"));

    public static class SqlMetadataV2 extends HbaseColumnFamily {
        public byte[] QUALIFIER_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");

        private SqlMetadataV2(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final SqlUidMetaData SQL_UID_METADATA_SQL = new SqlUidMetaData(HbaseTable.SQL_UID_METADATA, Bytes.toBytes("Sql"));

    public static class SqlUidMetaData extends HbaseColumnFamily {
        public byte[] QUALIFIER_SQLSTATEMENT = Bytes.toBytes("P_sql_statement");

        private SqlUidMetaData(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final StringMetadataStr STRING_METADATA_STR = new StringMetadataStr(HbaseTable.STRING_METADATA, Bytes.toBytes("Str"));

    public static class StringMetadataStr extends HbaseColumnFamily {
        public byte[] QUALIFIER_STRING = Bytes.toBytes("P_string");

        private StringMetadataStr(HbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final HbaseColumnFamily TRACE_V2_SPAN = new HbaseColumnFamily(HbaseTable.TRACE_V2, Bytes.toBytes("S"));
}
