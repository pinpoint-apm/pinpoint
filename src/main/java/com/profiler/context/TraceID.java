package com.profiler.context;

public class TraceID {
	private String traceId;
	private String parentSpanId;
	private String spanId;
	private boolean sampled;
	private int flags;

	public TraceID(String traceId, String parentSpanId, String spanId, boolean sampled, int flags) {
		this.traceId = (traceId == null) ? parentSpanId : traceId;
		this.parentSpanId = (parentSpanId == null) ? spanId : parentSpanId;
		this.spanId = spanId;
		this.sampled = sampled;
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
