package com.nhn.hippo.web.vo.scatter;

public class Dot {
	private final int exceptionCode;
	private final int executionTime;
	private final long timestamp;
	private String traceId;

	public Dot(int exceptionCode, int executionTime, long timestamp, String traceId) {
		super();
		this.exceptionCode = exceptionCode;
		this.executionTime = executionTime;
		this.timestamp = timestamp;
		this.traceId = traceId;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public int getExceptionCode() {
		return exceptionCode;
	}

	public int getExecutionTime() {
		return executionTime;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "Dot [exceptionCode=" + exceptionCode + ", executionTime=" + executionTime + ", timestamp=" + timestamp + ", traceId=" + traceId + "]";
	}
}
