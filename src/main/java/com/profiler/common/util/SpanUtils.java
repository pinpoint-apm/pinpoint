package com.profiler.common.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.common.dto.thrift.Span;

public class SpanUtils {

	public static byte[] getTraceIndexRowKey(Span span) {
		byte[] agentId = Bytes.toBytes(span.getAgentId());
		byte[] time = Bytes.toBytes(span.getTimestamp());

		return ArrayUtils.addAll(agentId, time);
	}

	public static byte[] getTracesRowkey(Span span) {
        return BytesUtils.longLongToBytes(span.getMostTraceId(), span.getLeastTraceId());
	}

	public static byte[] getTraceId(Span span) {
        return BytesUtils.longLongToBytes(span.getMostTraceId(), span.getLeastTraceId());
	}
}
