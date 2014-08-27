package com.nhn.pinpoint.web.vo;

/**
 * @author emeroad
 */
public class Trace {

	private final String transactionId;
	private final long executionTime;
	private final long startTime;

	private final int exceptionCode;

	public Trace(String transactionId, long executionTime, long startTime, int exceptionCode) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        this.transactionId = transactionId;
		this.executionTime = executionTime;
		this.startTime = startTime;
		this.exceptionCode = exceptionCode;
	}

	public String getTransactionId() {
		return transactionId;
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
