package com.nhn.pinpoint.common.util;

/**
 *
 */
public class TransactionIdUtils {
    public static final String TRANSACTION_ID_DELIMITER = "%";

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

    public static TransactionId parseTransactionId(final String transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        final int agentIdIndex = transactionId.indexOf(TRANSACTION_ID_DELIMITER);
        if (agentIdIndex == -1) {
            throw new IllegalArgumentException("agentIndex not found:" + transactionId);
        }
        final String agentId = transactionId.substring(0, agentIdIndex);

        final int agentStartTimeIndex = transactionId.indexOf(TRANSACTION_ID_DELIMITER, agentIdIndex + 1);
        if (agentStartTimeIndex == -1) {
            throw new IllegalArgumentException("agentStartTimeIndex not found:" + transactionId);
        }
        final long agentStartTime = parseLong(transactionId.substring(agentIdIndex + 1, agentStartTimeIndex));

        int transactionSequenceIndex = transactionId.indexOf(TRANSACTION_ID_DELIMITER, agentStartTimeIndex + 1);
        if (transactionSequenceIndex == -1) {
            // 이거는 없을수 있음. transactionSequence 다음에 델리미터가 일단 없는게 기본값임. 향후 추가 아이디 스펙이 확장가능하므로 보완한다.
            transactionSequenceIndex = transactionId.length();
        }
        final long transactionSequence = parseLong(transactionId.substring(agentStartTimeIndex + 1, transactionSequenceIndex));
        return new TransactionId(agentId, agentStartTime, transactionSequence);
    }

    private static long parseLong(String longString) {
        try {
            return Long.parseLong(longString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parseError. " + longString);
        }
    }
}
