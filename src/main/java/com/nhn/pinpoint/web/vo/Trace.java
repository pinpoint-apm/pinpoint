package com.nhn.pinpoint.web.vo;

/**
 * @author emeroad
 */
public class Trace {

	private final String traceId;
	private final long executionTime;
	private final long startTime;

	private final int exceptionCode;

	public Trace(String traceId, long executionTime, long startTime, int exceptionCode) {
		this.traceId = traceId;
		this.executionTime = executionTime;
		this.startTime = startTime;
		this.exceptionCode = exceptionCode;
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

	public int getExceptionCode() {
		return exceptionCode;
	}
}
