package com.nhn.hippo.web.vo.scatter;

public class Dot {
	private final int resultCode;
	private final int executionTime;
	private final long timestamp;
	private String traceId;

	public Dot(int resultCode, int executionTime, long timestamp, String traceId) {
		super();
		this.resultCode = resultCode;
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

	public int getResultCode() {
		return resultCode;
	}

	public int getExecutionTime() {
		return executionTime;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "Dot [resultCode=" + resultCode + ", executionTime=" + executionTime + ", timestamp=" + timestamp + ", traceId=" + traceId + "]";
	}
}
