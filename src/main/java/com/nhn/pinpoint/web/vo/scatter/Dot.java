package com.nhn.pinpoint.web.vo.scatter;

import com.nhn.pinpoint.web.vo.TransactionId;

public class Dot {
    private TransactionId transactionId;
    private final long timestamp;
    private final int executionTime;
    private final int exceptionCode;

    /**
     * 
     * @param transactionId
     * @param acceptedTime
     * @param executionTime
     * @param exceptionCode 0 : 정상, 1 : error
     */
	public Dot(TransactionId transactionId, long acceptedTime, int executionTime, int exceptionCode) {
        this.transactionId = transactionId;
        this.timestamp = acceptedTime;
        this.executionTime = executionTime;
        this.exceptionCode = exceptionCode;
    }

	public String getTransactionId() {
		return transactionId.getFormatString();
	}

	public void setTransactionId(TransactionId transactionId) {
		this.transactionId = transactionId;
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
        final StringBuilder sb = new StringBuilder("Dot{");
        sb.append("transactionId='").append(transactionId).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", executionTime=").append(executionTime);
        sb.append(", exceptionCode=").append(exceptionCode);
        sb.append('}');
        return sb.toString();
    }
}
