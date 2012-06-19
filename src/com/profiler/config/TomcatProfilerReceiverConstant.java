package com.profiler.config;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class TomcatProfilerReceiverConstant {
	public final static DateFormat DATE_FORMAT_YMD=new SimpleDateFormat("yyyy_MM_dd");
	public final static DateFormat DATE_FORMAT_YMD_HMS=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	public final static DateFormat DATE_FORMAT_YMD_HM=new SimpleDateFormat("yyyy_MM_dd_HH_mm");
	public final static DateFormat DATE_FORMAT_SHOW_YMD_HM=new SimpleDateFormat("yyyy/MM/dd HH:mm");
	public final static DateFormat DATE_FORMAT_HMS_MS=new SimpleDateFormat("HH:mm:ss,SSS");
	public final static DecimalFormat DECIMAL_FORMAT_THOUSAND_COMMA=new DecimalFormat("#,###");
	public final static DecimalFormat DECIMAL_FORMAT_DOT=new DecimalFormat("#,##0.000");
	public static final long DATA_FETCH_INTERVAL=2000;
//	public static final String ID_ACTIVE_THREAD_COUNT="ATC";
//	public static final String ID_HOST_INFO="HI";
//	public static final String ID_GC_STAT="GS";
//	public static final String ID_HEAP_MEMORY_USAGE_STAT="HMU";
//	public static final String ID_NON_HEAP_MEMORY_USAGE_STAT="NHMU";
	
	
	public static final int DATA_TYPE_REQUEST=1;
	public static final int DATA_TYPE_RESPONSE=2;
	public static final int DATA_TYPE_UNCAUGHT_EXCEPTION=11;
	
//	public static final int DATA_TYPE_HOST_INFO=1000;
//	public static final int DATA_TYPE_ACTIVE_THREAD_COUNT=1100;
//	public static final int DATA_TYPE_GC_STAT=1500;
//	public static final int DATA_TYPE_HEAP_MEMORY_STAT=1510;
//	public static final int DATA_TYPE_NON_HEAP_MEMORY_STAT=1520;
	
	public static final String CLASS_NAME_REQUEST_TRACER="com.profiler.trace.RequestTransactionTracer";
	public static final String CLASS_NAME_REQUEST_THRIFT_DTO="com.profiler.dto.RequestThriftDTO";
	public static final String CLASS_NAME_REQUEST_DATA_TRACER="com.profiler.trace.RequestDataTracer";
	public static final String CLASS_NAME_AGENT_STATE_MANAGER="com.profiler.thread.AgentStateManager";
	
	public static final int REQ_DATA_TYPE_DB_GET_CONNECTION=1;
	public static final int REQ_DATA_TYPE_DB_CREATE_STATEMENT=11;
	public static final int REQ_DATA_TYPE_DB_GET_PREPARED_STATEMENT=12;
	public static final int REQ_DATA_TYPE_DB_QUERY=21;
	public static final int REQ_DATA_TYPE_DB_EXECUTE_QUERY=31;
	public static final int REQ_DATA_TYPE_DB_EXECUTE_UPDATE=32;
	public static final int REQ_DATA_TYPE_DB_FETCH=41;
	public static final int REQ_DATA_TYPE_DB_RESULTSET_CLOSE=42;
	public static final int REQ_DATA_TYPE_DB_PREPARED_STATEMENT_PARAM=51;
	public static final int REQ_DATA_TYPE_DB_CLOSE_CONNECTION=99;
	
	public static final String HBASE_SERVER_TABLE="Server";
	public static final ByteBuffer HBASE_SERVER_TABLE_BYTE_BUFFER=ByteBuffer.wrap(HBASE_SERVER_TABLE.getBytes());
	public static final String HBASE_JVM_TABLE="JVM";
	public static final String HBASE_TPS_TABLE="TPS";
	public static final String HBASE_REQUEST_TABLE="Request";
	public static final String HBASE_REQUEST_DATA_TABLE="RequestData";
	public static final String HBASE_DATABASE_TABLE="Database";
	public static final ByteBuffer HBASE_DATABASE_TABLE_BYTE_BUFFER=ByteBuffer.wrap(HBASE_DATABASE_TABLE.getBytes());
	
	public static final byte[] HBASE_SERVER_ROW_ROW_NAMES="rowNames".getBytes();
	public static final String HBASE_SERVER_COLUMN_ROW_NAMES="rowNames";
	public static final String HBASE_SERVER_COLUMN_IP="ip";
	public static final String HBASE_SERVER_COLUMN_PORTS="ports";
	public static final String HBASE_SERVER_COLUMN_IS_RUNNING="isRunning";
	public static final String HBASE_SERVER_COLUMN_SERVICE_NAME="serviceName";
	public static final String HBASE_SERVER_COLUMN_SERVER_GROUP_NAME="serverGroupName";
	public static final String HBASE_SERVER_COLUMN_INSTANCE_NAME="instanceName";
	
	public static final int HBASE_JVM_DATA_COUNT=10;
	public static final String HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT="activeThreadCount";
	public static final String HBASE_JVM_COLUMN_GC1_COUNT="gc1Count";
	public static final String HBASE_JVM_COLUMN_GC1_TIME="gc1Time";
	public static final String HBASE_JVM_COLUMN_GC2_COUNT="gc2Count";
	public static final String HBASE_JVM_COLUMN_GC2_TIME="gc2Time";
	public static final String HBASE_JVM_COLUMN_HEAP_USED="heapUsed";
	public static final String HBASE_JVM_COLUMN_HEAP_COMMITTED="heapCommitted";
	public static final String HBASE_JVM_COLUMN_NON_HEAP_USED="nonHeapUsed";
	public static final String HBASE_JVM_COLUMN_NON_HEAP_COMMITTED="nonHeapCommitted";
	public static final String HBASE_JVM_COLUMN_PROCESS_CPU_TIME="processCPUTime";
	/**
	 * This value is used at RGraph
	 */
	public static final String HBASE_JVM_DATA_GC_COUNT="gcCount";
	public static final String HBASE_JVM_DATA_HEAP="heap";
	/*
	public static final ArrayList<ByteBuffer> HBASE_JVM_COLUMN_LIST=new ArrayList<ByteBuffer>();
	static {
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_GC1_COUNT));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_GC1_TIME));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_GC2_COUNT));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_GC2_TIME));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_HEAP_USED));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_HEAP_COMMITTED));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_NON_HEAP_USED));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_NON_HEAP_COMMITTED));
		HBASE_JVM_COLUMN_LIST.add(Convertor.toByteBuffer(HBASE_JVM_COLUMN_PROCESS_CPU_TIME));
	}
	*/
	public static final String HBASE_TPS_COLUMN_REQUEST_TPS="requestTPS";
	public static final String HBASE_TPS_COLUMN_RESPONSE_TPS="responseTPS";
	
	public static final int HBASE_REQUEST_DATA_TOTAL_COUNT=6;
	public static final String HBASE_REQUEST_COLUMN_REQUEST_TIME="requestTime";
	public static final String HBASE_REQUEST_COLUMN_RESPONSE_TIME="responseTime";
	public static final String HBASE_REQUEST_COLUMN_ELAPSED_TIME="elapsedTime";
	public static final String HBASE_REQUEST_COLUMN_REQUEST_URL="requestURL";
	public static final String HBASE_REQUEST_COLUMN_CLIENT_IP="clientIP";
	public static final String HBASE_REQUEST_COLUMN_REQUEST_DATA="requestData";
	public static final String HBASE_REQUEST_COLUMN_REQUEST_PARAMS="requestParams";

	public static final String HBASE_DATABASE_COLUMN_CONNECTION_URL="connectionURL";
	public static final String HBASE_DATABASE_COLUMN_QUERY_STRING="queryString";
	public static final String HBASE_DATABASE_COLUMN_DB_ROW_NAMES="dbRowNames";
	public static final String HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES="queryRowNames";
	public static final ByteBuffer HBASE_DATABASE_ROW_DB_ROW_NAMES=ByteBuffer.wrap("dbRowNames".getBytes());
	public static final ByteBuffer HBASE_DATABASE_ROW_QUERY_ROW_NAMES=ByteBuffer.wrap("queryRowNames".getBytes());
}
