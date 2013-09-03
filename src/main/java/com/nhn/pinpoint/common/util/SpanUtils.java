package com.nhn.pinpoint.common.util;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;

import com.nhn.pinpoint.common.dto2.thrift.Span;
import com.nhn.pinpoint.common.dto2.thrift.SpanChunk;
import com.nhn.pinpoint.common.dto2.thrift.SpanEvent;

public class SpanUtils {

	public static byte[] getAgentIdTraceIndexRowKey(String agentId, long timestamp) {
		if (agentId == null) {
			throw new IllegalArgumentException("agentId must not null");
		}
		byte[] bAgentId = BytesUtils.getBytes(agentId);
		return RowKeyUtils.concatFixedByteAndLong(bAgentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseCurrentTimeMillis(timestamp));
	}

	public static byte[] getApplicationTraceIndexRowKey(String applicationName, long timestamp) {
		if (applicationName == null) {
			throw new IllegalArgumentException("agentId must not null");
		}
		byte[] bApplicationName = BytesUtils.getBytes(applicationName);
		return RowKeyUtils.concatFixedByteAndLong(bApplicationName, AGENT_NAME_MAX_LEN, TimeUtils.reverseCurrentTimeMillis(timestamp));
	}
	
//	public static byte[] getTraceIndexRowKey(String agentId, long timestamp) {
//		if (agentId == null) {
//			throw new IllegalArgumentException("agentId must not null");
//		}
//		byte[] bAgentId = BytesUtils.getBytes(agentId);
//		return RowKeyUtils.concatFixedByteAndLong(bAgentId, AGENT_NAME_MAX_LEN, timestamp);
//	}

	public static byte[] getTraceIndexRowKey(byte[] agentId, long timestamp) {
		return RowKeyUtils.concatFixedByteAndLong(agentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseCurrentTimeMillis(timestamp));
	}

	public static byte[] getTraceId(Span span) {
		return BytesUtils.longLongToBytes(span.getMostTraceId(), span.getLeastTraceId());
	}

	public static byte[] getTraceId(SpanEvent spanEvent) {
		return BytesUtils.longLongToBytes(spanEvent.getMostTraceId(), spanEvent.getLeastTraceId());
	}

	public static byte[] getTraceId(SpanChunk spanChunk) {
		return BytesUtils.longLongToBytes(spanChunk.getMostTraceId(), spanChunk.getLeastTraceId());
	}
}
