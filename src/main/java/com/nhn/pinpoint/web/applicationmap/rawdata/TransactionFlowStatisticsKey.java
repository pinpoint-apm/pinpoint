package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public class TransactionFlowStatisticsKey {
    private final String from;
    private final ServiceType fromServiceType;
    private final String to;
    private final ServiceType toServiceType;

    public TransactionFlowStatisticsKey(TransactionFlowStatistics transactionFlowStatistics) {
        this(transactionFlowStatistics.getFrom(), transactionFlowStatistics.getFromServiceType(), transactionFlowStatistics.getTo(), transactionFlowStatistics.getToServiceType());
    }

    private TransactionFlowStatisticsKey(String from, ServiceType fromServiceType, String to, ServiceType toServiceType) {
        if (from == null) {
            throw new NullPointerException("from must not be null");
        }
        if (fromServiceType == null) {
            throw new NullPointerException("fromServiceType must not be null");
        }
        if (to == null) {
            throw new NullPointerException("to must not be null");
        }
        if (toServiceType == null) {
            throw new NullPointerException("toServiceType must not be null");
        }
        this.from = from;
        this.fromServiceType = fromServiceType;
        this.to = to;
        this.toServiceType = toServiceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionFlowStatisticsKey that = (TransactionFlowStatisticsKey) o;

        if (!from.equals(that.from)) return false;
        if (fromServiceType != that.fromServiceType) return false;
        if (!to.equals(that.to)) return false;
        if (toServiceType != that.toServiceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + fromServiceType.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + toServiceType.hashCode();
        return result;
    }
}
