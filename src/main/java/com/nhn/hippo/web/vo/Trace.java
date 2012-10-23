package com.nhn.hippo.web.vo;

public class Trace {

	private final String traceId;
	private final long executionTime;
	private final long timestamp;

	public Trace(String traceId, long executionTime, long timestamp) {
		this.traceId = traceId;
		this.executionTime = executionTime;
		this.timestamp = timestamp;
	}

	public String getTraceId() {
		return traceId;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
