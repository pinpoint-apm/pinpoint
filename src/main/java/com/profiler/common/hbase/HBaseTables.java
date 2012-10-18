package com.profiler.common.hbase;

import org.apache.hadoop.hbase.util.Bytes;

public class HBaseTables {

    public static final String TRACE_INDEX = "TraceIndex";
    public static final byte[] TRACE_INDEX_CF_TRACE = Bytes.toBytes("Trace");
    public static final byte[] TRACE_INDEX_CN_ID = Bytes.toBytes("ID");

    public static final String TRACES = "Traces";
    public static final byte[] TRACES_CF_SPAN = Bytes.toBytes("Span");
    public static final byte[] TRACES_CF_ANNOTATION = Bytes.toBytes("Annotation");

    public static final String SERVERS = "Servers";
}
