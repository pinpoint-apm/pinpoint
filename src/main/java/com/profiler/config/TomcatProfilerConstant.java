package com.profiler.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TomcatProfilerConstant {
	public final static DateFormat DATE_FORMAT_YMD_HMS = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	public final static DateFormat DATE_FORMAT_HMS_MS = new SimpleDateFormat("HH:mm:ss,SSS");
	public static final long DATA_FETCH_INTERVAL = 2000;
	// public static final String ID_ACTIVE_THREAD_COUNT="ATC";
	// public static final String ID_HOST_INFO="HI";
	// public static final String ID_GC_STAT="GS";
	// public static final String ID_HEAP_MEMORY_USAGE_STAT="HMU";
	// public static final String ID_NON_HEAP_MEMORY_USAGE_STAT="NHMU";

	public static final int DATA_TYPE_REQUEST = 1;
	public static final int DATA_TYPE_RESPONSE = 2;
	public static final int DATA_TYPE_UNCAUGHT_EXCEPTION = 11;

	// public static final int DATA_TYPE_HOST_INFO=1000;
	// public static final int DATA_TYPE_ACTIVE_THREAD_COUNT=1100;
	// public static final int DATA_TYPE_GC_STAT=1500;
	// public static final int DATA_TYPE_HEAP_MEMORY_STAT=1510;
	// public static final int DATA_TYPE_NON_HEAP_MEMORY_STAT=1520;

//	public static final String CLASS_NAME_REQUEST_TRACER = "com.profiler.trace.RequestTransactionTracer";
	public static final String CLASS_NAME_REQUEST_THRIFT_DTO = "com.profiler.common.dto.thrift.RequestThriftDTO";
//	public static final String CLASS_NAME_REQUEST_DATA_TRACER = "com.profiler.trace.RequestDataTracer";
//	public static final String CLASS_NAME_AGENT_STATE_MANAGER = "com.profiler.thread.AgentStateManager";

	public static final int REQ_DATA_TYPE_DB_GET_CONNECTION = 1;
	public static final int REQ_DATA_TYPE_DB_CREATE_STATEMENT = 11;
	public static final int REQ_DATA_TYPE_DB_GET_PREPARED_STATEMENT = 12;
	public static final int REQ_DATA_TYPE_DB_QUERY = 21;
	public static final int REQ_DATA_TYPE_DB_EXECUTE_QUERY = 31;
	public static final int REQ_DATA_TYPE_DB_EXECUTE_UPDATE = 32;
	public static final int REQ_DATA_TYPE_DB_FETCH = 41;
	public static final int REQ_DATA_TYPE_DB_RESULTSET_CLOSE = 42;
	public static final int REQ_DATA_TYPE_DB_PREPARED_STATEMENT_PARAM = 51;
	public static final int REQ_DATA_TYPE_DB_CLOSE_CONNECTION = 99;
}
