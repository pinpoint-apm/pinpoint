package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.common.util.TransactionId;
import com.nhn.pinpoint.common.util.TransactionIdUtils;

/**
 * @author emeroad
 */
public class DefaultTraceId implements TraceId {

    private final String agentId;
	private final long agentStartTime;
    private final long transactionSequence;

	private final long parentSpanId;
	private final long spanId;
	private final short flags;

    public DefaultTraceId(String agentId, long agentStartTime, long transactionId) {
        this(agentId, agentStartTime, transactionId, SpanId.NULL, SpanId.newSpanId(), (short) 0);
    }

    public static DefaultTraceId parse(String transactionId, long parentSpanID, long spanID, short flags) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        final TransactionId parseId = TransactionIdUtils.parseTransactionId(transactionId);
        return new DefaultTraceId(parseId.getAgentId(), parseId.getAgentStartTime(), parseId.getTransactionSequence(), parentSpanID, spanID, flags);

    }

	public TraceId getNextTraceId() {
		return new DefaultTraceId(this.agentId, this.agentStartTime, transactionSequence, spanId, SpanId.nextSpanID(spanId, parentSpanId), flags);
	}

	public DefaultTraceId(String agentId, long agentStartTime, long transactionId, long parentSpanId, long spanId, short flags) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionId;

		this.parentSpanId = parentSpanId;
		this.spanId = spanId;
		this.flags = flags;
	}

	public String getTransactionId() {
        return TransactionIdUtils.formatString(agentId, agentStartTime, transactionSequence);
	}

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getTransactionSequence() {
        return transactionSequence;
    }


	public long getParentSpanId() {
		return parentSpanId;
	}

	public long getSpanId() {
		return spanId;
	}


	public short getFlags() {
		return flags;
	}

	public boolean isRoot() {
		return this.parentSpanId == SpanId.NULL;
	}


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultTraceID{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", agentStartTime=").append(agentStartTime);
        sb.append(", transactionSequence=").append(transactionSequence);
        sb.append(", parentSpanId=").append(parentSpanId);
        sb.append(", spanId=").append(spanId);
        sb.append(", flags=").append(flags);
        sb.append('}');
        return sb.toString();
    }
}
