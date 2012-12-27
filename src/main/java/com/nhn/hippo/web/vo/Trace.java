package com.nhn.hippo.web.vo;

public class Trace {

	private final String traceId;
	private final long executionTime;
	private final long startTime;

	private final boolean exception;

	public Trace(String traceId, long executionTime, long startTime, boolean exception) {
		this.traceId = traceId;
		this.executionTime = executionTime;
		this.startTime = startTime;
		this.exception = exception;
	}

	public String getTraceId() {
		return traceId;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public boolean isException() {
		return exception;
	}
}
