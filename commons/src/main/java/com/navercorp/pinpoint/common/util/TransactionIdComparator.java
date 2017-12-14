package com.navercorp.pinpoint.common.util;

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TransactionIdComparator implements Comparator<TransactionId> {

    public static final TransactionIdComparator INSTANCE = new TransactionIdComparator();

    @Override
    public int compare(TransactionId o1, TransactionId o2) {
        int r1 = o1.getAgentId().compareTo(o2.getAgentId());
        if (r1 == 0) {
            if (o1.getAgentStartTime() > o2.getAgentStartTime()) {
                return 1;
            } else if (o1.getAgentStartTime() < o2.getAgentStartTime()) {
                return -1;
            } else {
                if (o1.getTransactionSequence() > o2.getTransactionSequence()) {
                    return 1;
                } else if (o1.getTransactionSequence() < o2.getTransactionSequence()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        } else {
            return r1;
        }
    }

}
