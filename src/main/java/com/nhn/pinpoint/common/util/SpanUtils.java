package com.nhn.pinpoint.common.util;

import static com.nhn.pinpoint.common.PinpointConstants.AGENT_NAME_MAX_LEN;

import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

public class SpanUtils {
    @Deprecated
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
	
	public static byte[] getTraceIndexRowKey(byte[] agentId, long timestamp) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        return RowKeyUtils.concatFixedByteAndLong(agentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseCurrentTimeMillis(timestamp));
	}

	public static byte[] getTransactionId(TSpan span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        return BytesUtils.stringLongLongToBytes(span.getTraceAgentId(), AGENT_NAME_MAX_LEN, span.getTraceAgentStartTime(), span.getTraceTransactionSequence());

	}

	public static byte[] getTransactionId(TSpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        return BytesUtils.stringLongLongToBytes(spanEvent.getTraceAgentId(), AGENT_NAME_MAX_LEN, spanEvent.getTraceAgentStartTime(), spanEvent.getTraceTransactionSequence());
	}

	public static byte[] getTransactionId(TSpanChunk spanChunk) {
        if (spanChunk == null) {
            throw new NullPointerException("spanChunk must not be null");
        }
        return BytesUtils.stringLongLongToBytes(spanChunk.getTraceAgentId(), AGENT_NAME_MAX_LEN, spanChunk.getTraceAgentStartTime(), spanChunk.getTraceTransactionSequence());
	}
}
