package com.profiler.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ProfilerConstant {
	public final static DateFormat DATE_FORMAT_YMD_HMS = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	public final static DateFormat DATE_FORMAT_HMS_MS = new SimpleDateFormat("HH:mm:ss,SSS");
	public static final long DATA_FETCH_INTERVAL = 2000;

	public static final int DATA_TYPE_REQUEST = 1;
	public static final int DATA_TYPE_RESPONSE = 2;
	public static final int DATA_TYPE_UNCAUGHT_EXCEPTION = 11;


	public static final String CLASS_NAME_REQUEST_THRIFT_DTO = "RequestThriftDTO";


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
