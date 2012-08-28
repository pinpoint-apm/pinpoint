package com.profiler.context;

import java.util.UUID;

public class TraceID {
	private String traceId;
	private String parentSpanId;
	private String spanId;
	private boolean sampled;
	private int flags;

	public static TraceID newTraceId() {
		return new TraceID(UUID.randomUUID().toString(), null, SpanID.newSpanID(), false, 0);
	}

	public TraceID(String traceId, String parentSpanId, String spanId, boolean sampled, int flags) {
		this.traceId = (traceId == null) ? parentSpanId : traceId;
		this.parentSpanId = (parentSpanId == null) ? spanId : parentSpanId;
		this.spanId = spanId;
		this.sampled = sampled;
		this.flags = flags;
	}

	public String getTraceId() {
		return traceId;
	}

	public String getParentSpanId() {
		return parentSpanId;
	}

	public String getSpanId() {
		return spanId;
	}

	public boolean isSampled() {
		return sampled;
	}

	public int getFlags() {
		return flags;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public void setParentSpanId(String parentSpanId) {
		this.parentSpanId = parentSpanId;
	}

	public void setSpanId(String spanId) {
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

		sb.append("TraceID={");
		sb.append("traceId=").append(traceId);
		sb.append(", parentSpanId=").append(parentSpanId);
		sb.append(", spanId=").append(spanId);
		sb.append(", sampled=").append(sampled);
		sb.append(", flags=").append(flags);
		sb.append("}");

		return sb.toString();
	}
}
