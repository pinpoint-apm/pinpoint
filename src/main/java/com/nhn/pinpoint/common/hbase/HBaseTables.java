package com.nhn.pinpoint.common.hbase;

import com.nhn.pinpoint.common.PinpointConstants;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author emeroad
 */
public class HBaseTables {
	
	public static final int APPLICATION_NAME_MAX_LEN = PinpointConstants.APPLICATION_NAME_MAX_LEN;
    public static final int AGENT_NAME_MAX_LEN = PinpointConstants.AGENT_NAME_MAX_LEN;
	

    public static final String APPLICATION_TRACE_INDEX = "ApplicationTraceIndex";
    public static final byte[] APPLICATION_TRACE_INDEX_CF_TRACE = Bytes.toBytes("I"); // applicationIndex
    public static final int APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

    public static final String AGENT_STAT = "AgentStat";
    public static final byte[] AGENT_STAT_CF_STATISTICS = Bytes.toBytes("S"); // agent statistics
    public static final byte[] AGENT_STAT_CF_STATISTICS_V1 = Bytes.toBytes("V1"); // qualifier
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

    public static final String AGENTID_APPLICATION_INDEX = "AgentIdApplicationIndex";
    public static final byte[] AGENTID_APPLICATION_INDEX_CF_APPLICATION = Bytes.toBytes("Application");


    public static final String SQL_METADATA = "SqlMetaData";
    public static final byte[] SQL_METADATA_CF_SQL = Bytes.toBytes("Sql");

    public static final String STRING_METADATA = "StringMetaData";
    public static final byte[] STRING_METADATA_CF_STR = Bytes.toBytes("Str");

    public static final String API_METADATA = "ApiMetaData";
    public static final byte[] API_METADATA_CF_API = Bytes.toBytes("Api");

	public static final String APPLICATION_MAP_STATISTICS_CALLER = "ApplicationMapStatisticsCaller";
	public static final byte[] APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER = Bytes.toBytes("C");

	public static final String APPLICATION_MAP_STATISTICS_CALLEE = "ApplicationMapStatisticsCallee";
	public static final byte[] APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER = Bytes.toBytes("C");
	
	public static final String HOST_APPLICATION_MAP = "HostApplicationMap";
	public static final byte[] HOST_APPLICATION_MAP_CF_MAP = Bytes.toBytes("M");

    public static final short STATISTICS_CQ_ERROR_SLOT_NUMBER = -1;
    public static final byte[] STATISTICS_CQ_ERROR_SLOT = Bytes.toBytes(STATISTICS_CQ_ERROR_SLOT_NUMBER);
}
