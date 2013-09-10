package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.util.TraceIdUtils;

public class DefaultTraceID implements TraceID {
    public static final String AGENT_DELIMITER = "=";

    private String agentId;
	private long agentStartTime;
    private long transactionId;

	private int parentSpanId;
	private int spanId;
	private short flags;

    public DefaultTraceID(String agentId, long agentStartTime, long transactionId) {
        this(agentId, agentStartTime, transactionId, SpanID.NULL, SpanID.newSpanID(), (short) 0);
    }

    public static DefaultTraceID parse(final String guid, int parentSpanID, int spanID, short flags) {
        if (guid == null) {
            throw new NullPointerException("guid must not be null");
        }
        final int agentIdIndex = guid.indexOf(DefaultTraceID.AGENT_DELIMITER);
        if (agentIdIndex == -1) {
            throw new IllegalArgumentException("guid delimiter not found:" + guid);
        }
        final String agentId = guid.substring(0, agentIdIndex);
        String ids = guid.substring(agentIdIndex + 1, guid.length());
        String[] strings = TraceIdUtils.parseTraceId(ids);
        final long startTime = TraceIdUtils.parseMostId(strings);
        final long transactionId = TraceIdUtils.parseLeastId(strings);
        return new DefaultTraceID(agentId, startTime, transactionId, parentSpanID, spanID, flags);

    }

	public TraceID getNextTraceId() {
		return new DefaultTraceID(this.agentId, this.agentStartTime, transactionId, spanId, SpanID.nextSpanID(spanId, parentSpanId), flags);
	}

	public DefaultTraceID(String agentId, long agentStartTime, long transactionId, int parentSpanId, int spanId, short flags) {
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionId = transactionId;

		this.parentSpanId = parentSpanId;
		this.spanId = spanId;
		this.flags = flags;
	}

	public String getId() {
        return TraceIdUtils.formatString(agentId, agentStartTime, transactionId);
	}

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public TraceKey getTraceKey() {
		return new TraceKey(this.agentId, agentStartTime, transactionId, spanId);
	}

	public static final class TraceKey {
        private final String agentId;
		private final long agentStartTime;
		private final long transactionId;
		private final int span;

		public TraceKey(String agentId, long agentStartTime, long transactionId, int span) {
            if (agentId == null) {
                throw new NullPointerException("agentId must not be null");
            }
            this.agentId = agentId;
			this.agentStartTime = agentStartTime;
			this.transactionId = transactionId;
			this.span = span;
		}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TraceKey traceKey = (TraceKey) o;

            if (agentStartTime != traceKey.agentStartTime) return false;
            if (span != traceKey.span) return false;
            if (transactionId != traceKey.transactionId) return false;
            if (!agentId.equals(traceKey.agentId)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = agentId.hashCode();
            result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
            result = 31 * result + (int) (transactionId ^ (transactionId >>> 32));
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
		return this.parentSpanId == SpanID.NULL;
	}


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultTraceID{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", agentStartTime=").append(agentStartTime);
        sb.append(", transactionId=").append(transactionId);
        sb.append(", parentSpanId=").append(parentSpanId);
        sb.append(", spanId=").append(spanId);
        sb.append(", flags=").append(flags);
        sb.append('}');
        return sb.toString();
    }
}
