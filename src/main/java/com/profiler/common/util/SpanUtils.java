package com.profiler.common.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.common.dto.thrift.Span;

public class SpanUtils {

	public static byte[] getTraceIndexRowKey(Span span) {
		byte[] agentId = Bytes.toBytes(span.getAgentID());
		byte[] time = Bytes.toBytes(span.getTimestamp());

		return ArrayUtils.addAll(agentId, time);
	}

	public static byte[] getTracesRowkey(Span span) {
		return ArrayUtils.addAll(Bytes.toBytes(span.getMostTraceID()), Bytes.toBytes(span.getLeastTraceID()));
	}

	public static byte[] getTraceId(Span span) {
		byte[] mostTid = Bytes.toBytes(span.getMostTraceID());
		byte[] leastTid = Bytes.toBytes(span.getLeastTraceID());

		return ArrayUtils.addAll(mostTid, leastTid);
	}
}
