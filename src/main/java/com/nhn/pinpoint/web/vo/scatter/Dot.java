package com.nhn.pinpoint.web.vo.scatter;

public class Dot {
    private String traceId;
    private final long timestamp;
    private final int executionTime;
    private final int exceptionCode;

    /**
     * 
     * @param traceId
     * @param acceptedTime
     * @param executionTime
     * @param exceptionCode 0 : 정상, 1 : error
     */
	public Dot(String traceId, long acceptedTime, int executionTime, int exceptionCode) {
        this.traceId = traceId;
        this.timestamp = acceptedTime;
        this.executionTime = executionTime;
        this.exceptionCode = exceptionCode;
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
