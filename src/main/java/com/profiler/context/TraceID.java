package com.profiler.context;

import java.util.UUID;

public class TraceID {
	private UUID id;
	private long parentSpanId;
	private long spanId;
	private boolean sampled;
	private int flags;

	public static TraceID newTraceId() {
		UUID uuid = UUID.randomUUID();
		return new TraceID(uuid, SpanID.NULL, SpanID.newSpanID(), false, 0);
	}

	public TraceID(UUID id, long parentSpanId, long spanId, boolean sampled, int flags) {
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
		return new TraceKey(most, least);
	}

	public static class TraceKey {
		private long most;
		private long least;

		public TraceKey(long most, long least) {
			this.most = most;
			this.least = least;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			TraceKey that = (TraceKey) o;

			if (least != that.least)
				return false;
			if (most != that.most)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = (int) (most ^ (most >>> 32));
			result = 31 * result + (int) (least ^ (least >>> 32));
			return result;
		}
	}

	public long getParentSpanId() {
		return parentSpanId;
	}

	public long getSpanId() {
		return spanId;
	}

	public boolean isSampled() {
		return sampled;
	}

	public int getFlags() {
		return flags;
	}

	public void setTraceId(UUID traceId) {
		this.id = traceId;
	}

	public void setParentSpanId(long parentSpanId) {
		this.parentSpanId = parentSpanId;
	}

	public void setSpanId(long spanId) {
		this.spanId = spanId;
	}

	public void setSampled(boolean sampled) {
		this.sampled = sampled;
	}

	public void setFlags(int flags) {
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
