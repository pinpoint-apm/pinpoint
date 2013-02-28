package com.profiler.common.util;

import static com.profiler.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.common.dto.thrift.SpanChunk;

public class SpanUtils {

	public static byte[] getAgentIdTraceIndexRowKey(String agentId, long acceptedTime) {
		return getTraceIndexRowKey(agentId, acceptedTime);
	}

	public static byte[] getApplicationTraceIndexRowKey(String applicationName, long acceptedTime) {
		return getTraceIndexRowKey(applicationName, acceptedTime);
	}

	public static byte[] getTraceIndexRowKey(byte[] agentId, long time) {
		return RowKeyUtils.concatFixedByteAndLong(agentId, AGENT_NAME_MAX_LEN, time);
	}

	public static byte[] getTraceIndexRowKey(String agentId, long time) {
		if (agentId == null) {
			throw new IllegalArgumentException("agentId must not null");
		}
		byte[] bAgentId = BytesUtils.getBytes(agentId);
		return getTraceIndexRowKey(bAgentId, time);
	}

	public static byte[] getTraceId(Span span) {
		return BytesUtils.longLongToBytes(span.getMostTraceId(), span.getLeastTraceId());
	}

	public static byte[] getTraceId(SubSpan subSpan) {
		return BytesUtils.longLongToBytes(subSpan.getMostTraceId(), subSpan.getLeastTraceId());
	}

	public static byte[] getTraceId(SpanChunk spanChunk) {
		return BytesUtils.longLongToBytes(spanChunk.getMostTraceId(), spanChunk.getLeastTraceId());
	}
}
