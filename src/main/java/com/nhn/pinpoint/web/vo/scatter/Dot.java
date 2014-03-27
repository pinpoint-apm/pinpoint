package com.nhn.pinpoint.web.vo.scatter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nhn.pinpoint.web.view.DotSerializer;
import com.nhn.pinpoint.web.vo.TransactionId;

@JsonSerialize(using = DotSerializer.class)
public class Dot {
    public static final int EXCEPTION_NONE = 0;

    public static final int SUCCESS_STATE = 1;
    public static final int FAILED_STATE = 0;

    private final TransactionId transactionId;
    private final long acceptedTime;
    private final int elapsedTime;
    private final int exceptionCode;
    private final String agentId;

    /**
     * 
     * @param transactionId
     * @param acceptedTime
     * @param elapsedTime
     * @param exceptionCode 0 : 정상, 1 : error
     */
	public Dot(TransactionId transactionId, long acceptedTime, int elapsedTime, int exceptionCode, String agentId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.transactionId = transactionId;
        this.acceptedTime = acceptedTime;
        this.elapsedTime = elapsedTime;
        this.exceptionCode = exceptionCode;
        this.agentId = agentId;
    }

	public String getTransactionId() {
		return transactionId.getFormatString();
	}

	public int getExceptionCode() {
		return exceptionCode;
	}

	public int getElapsedTime() {
		return elapsedTime;
	}

	public long getAcceptedTime() {
		return acceptedTime;
	}

    public String getAgentId() {
        return agentId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("Dot{");
        sb.append("transactionId=").append(transactionId);
        sb.append(", acceptedTime=").append(acceptedTime);
        sb.append(", elapsedTime=").append(elapsedTime);
        sb.append(", exceptionCode=").append(exceptionCode);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
