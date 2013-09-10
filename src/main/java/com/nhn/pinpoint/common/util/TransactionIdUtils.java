package com.nhn.pinpoint.common.util;

/**
 *
 */
public class TransactionIdUtils {

    public static final String TRANSACTION_ID_DELIMITER = "=";

    public static final String formatString(String agentId, long agentStartTime, long transactionSequence) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append(agentId);
        sb.append(TRANSACTION_ID_DELIMITER);
        sb.append(agentStartTime);
        sb.append(TRANSACTION_ID_DELIMITER);
        sb.append(transactionSequence);
        return sb.toString();
    }

    public static String[] parseTransactionId(final String transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        String[] component = transactionId.split(TRANSACTION_ID_DELIMITER);
        if (component.length != 3) {
            throw new IllegalArgumentException("Invalid TraceId string: "+ transactionId);
        }
        return component;
    }
}
