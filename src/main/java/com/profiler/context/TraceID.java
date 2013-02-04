package com.profiler.context;

import java.util.UUID;

public class TraceID {
	private UUID id;
	private int parentSpanId;
	private int spanId;
	private boolean sampled;
	private short flags;

	public static TraceID newTraceId() {
		UUID uuid = UUID.randomUUID();
		return new TraceID(uuid, SpanID.NULL, SpanID.newSpanID(), false, (short) 0);
	}

	public TraceID getNextTraceId() {
		return new TraceID(id, spanId, SpanID.nextSpanID(spanId), sampled, flags);
	}

	public TraceID(UUID id, int parentSpanId, int spanId, boolean sampled, short flags) {
		this.id = id;
		this.parentSpanId = parentSpanId;
		this.spanId = spanId;
		this.sampled = sampled;
		this.flags = flags;
	}

	public UUID getId() {
		return id;
	}

	public TraceKey getTraceKey() {
		long most = id.getMostSignificantBits();
		long least = id.getLeastSignificantBits();
		return new TraceKey(most, least, spanId);
	}

	public static class TraceKey {
		private long most;
		private long least;
		private long span;

		public TraceKey(long most, long least, long span) {
			this.most = most;
			this.least = least;
			this.span = span;
		}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TraceKey traceKey = (TraceKey) o;

            if (least != traceKey.least) return false;
            if (most != traceKey.most) return false;
            if (span != traceKey.span) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (most ^ (most >>> 32));
            result = 31 * result + (int) (least ^ (least >>> 32));
            result = 31 * result + (int) (span ^ (span >>> 32));
            return result;
        }
    }

	public int getParentSpanId() {
		return parentSpanId;
	}

	public int getSpanId() {
		return spanId;
	}

	public boolean isSampled() {
		return sampled;
	}

	public short getFlags() {
		return flags;
	}

	public void setTraceId(UUID traceId) {
		this.id = traceId;
	}

	public void setParentSpanId(int parentSpanId) {
		this.parentSpanId = parentSpanId;
	}

	public void setSpanId(int spanId) {
		this.spanId = spanId;
	}

	public void setSampled(boolean sampled) {
		this.sampled = sampled;
	}

	public void setFlags(short flags) {
		this.flags = flags;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{");
		sb.append("id=").append(id);
		sb.append(", parentSpanId=").append(parentSpanId);
		sb.append(", spanId=").append(spanId);
		sb.append(", sampled=").append(sampled);
		sb.append(", flags=").append(flags);
		sb.append("}");

		return sb.toString();
	}
}
