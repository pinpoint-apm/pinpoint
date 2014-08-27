package com.nhn.pinpoint.common.util;

import static com.nhn.pinpoint.common.PinpointConstants.AGENT_NAME_MAX_LEN;

import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;

/**
 * @author emeroad
 */
public class SpanUtils {
    @Deprecated
	public static byte[] getAgentIdTraceIndexRowKey(String agentId, long timestamp) {
		if (agentId == null) {
			throw new IllegalArgumentException("agentId must not null");
		}
		final byte[] bAgentId = BytesUtils.toBytes(agentId);
		return RowKeyUtils.concatFixedByteAndLong(bAgentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
	}

	public static byte[] getApplicationTraceIndexRowKey(String applicationName, long timestamp) {
		if (applicationName == null) {
			throw new IllegalArgumentException("agentId must not null");
		}
		final byte[] bApplicationName = BytesUtils.toBytes(applicationName);
		return RowKeyUtils.concatFixedByteAndLong(bApplicationName, AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
	}
	
	public static byte[] getTraceIndexRowKey(byte[] agentId, long timestamp) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        return RowKeyUtils.concatFixedByteAndLong(agentId, AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
	}

    public static byte[] getVarTransactionId(TSpan span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        final byte[] transactionIdBytes = span.getTransactionId();
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(transactionIdBytes);
        String agentId = transactionId.getAgentId();
        if (agentId == null) {
            agentId = span.getAgentId();
        }

        final Buffer buffer= new AutomaticBuffer(32);
        buffer.putPrefixedString(agentId);
        buffer.putSVar(transactionId.getAgentStartTime());
        buffer.putVar(transactionId.getTransactionSequence());
        return buffer.getBuffer();
    }

	public static byte[] getTransactionId(TSpan span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        final byte[] transactionIdBytes = span.getTransactionId();
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(transactionIdBytes);
        String agentId = transactionId.getAgentId();
        if (agentId == null) {
            agentId = span.getAgentId();
        }
        return BytesUtils.stringLongLongToBytes(agentId, AGENT_NAME_MAX_LEN, transactionId.getAgentStartTime(), transactionId.getTransactionSequence());

	}

	public static byte[] getTransactionId(TSpanChunk spanChunk) {
        if (spanChunk == null) {
            throw new NullPointerException("spanChunk must not be null");
        }
        final byte[] transactionIdBytes = spanChunk.getTransactionId();
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(transactionIdBytes);
        String agentId = transactionId.getAgentId();
        if (agentId == null) {
            agentId = spanChunk.getAgentId();
        }
        return BytesUtils.stringLongLongToBytes(agentId, AGENT_NAME_MAX_LEN, transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
	}
}
