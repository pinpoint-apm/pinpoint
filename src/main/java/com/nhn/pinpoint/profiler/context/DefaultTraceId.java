package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.util.TransactionIdUtils;

public class DefaultTraceId implements TraceId {
    public static final String AGENT_DELIMITER = "=";

    private String agentId;
	private long agentStartTime;
    private long transactionSequence;

	private int parentSpanId;
	private int spanId;
	private short flags;

    public DefaultTraceId(String agentId, long agentStartTime, long transactionId) {
        this(agentId, agentStartTime, transactionId, SpanId.NULL, SpanId.newSpanId(), (short) 0);
    }

    public static DefaultTraceId parse(final String transactionId, int parentSpanID, int spanID, short flags) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        final int agentIdIndex = transactionId.indexOf(DefaultTraceId.AGENT_DELIMITER);
        if (agentIdIndex == -1) {
            throw new IllegalArgumentException("transactionId delimiter not found:" + transactionId);
        }
        final String agentId = transactionId.substring(0, agentIdIndex);
        String ids = transactionId.substring(agentIdIndex + 1, transactionId.length());
        String[] strings = TransactionIdUtils.parseTraceId(ids);
        final long startTime = TransactionIdUtils.parseMostId(strings);
        final long eachTransactionId = TransactionIdUtils.parseLeastId(strings);
        return new DefaultTraceId(agentId, startTime, eachTransactionId, parentSpanID, spanID, flags);

    }

	public TraceId getNextTraceId() {
		return new DefaultTraceId(this.agentId, this.agentStartTime, transactionSequence, spanId, SpanId.nextSpanID(spanId, parentSpanId), flags);
	}

	public DefaultTraceId(String agentId, long agentStartTime, long transactionId, int parentSpanId, int spanId, short flags) {
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

    public TraceKey getTraceKey() {
		return new TraceKey(this.agentId, agentStartTime, transactionSequence, spanId);
	}

	public static final class TraceKey {
        private final String agentId;
		private final long agentStartTime;
		private final long transactionSequence;
		private final int span;

		public TraceKey(String agentId, long agentStartTime, long transactionSequence, int span) {
            if (agentId == null) {
                throw new NullPointerException("agentId must not be null");
            }
            this.agentId = agentId;
			this.agentStartTime = agentStartTime;
			this.transactionSequence = transactionSequence;
			this.span = span;
		}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TraceKey traceKey = (TraceKey) o;

            if (agentStartTime != traceKey.agentStartTime) return false;
            if (span != traceKey.span) return false;
            if (transactionSequence != traceKey.transactionSequence) return false;
            if (!agentId.equals(traceKey.agentId)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = agentId.hashCode();
            result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
            result = 31 * result + (int) (transactionSequence ^ (transactionSequence >>> 32));
            result = 31 * result + span;
            return result;
        }
    }

	public int getParentSpanId() {
		return parentSpanId;
	}

	public int getSpanId() {
		return spanId;
	}


	public short getFlags() {
		return flags;
	}


	public void setParentSpanId(int parentSpanId) {
		this.parentSpanId = parentSpanId;
	}

	public void setSpanId(int spanId) {
		this.spanId = spanId;
	}

	public void setFlags(short flags) {
		this.flags = flags;
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
