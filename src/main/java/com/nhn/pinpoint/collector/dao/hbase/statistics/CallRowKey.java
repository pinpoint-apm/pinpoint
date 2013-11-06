package com.nhn.pinpoint.collector.dao.hbase.statistics;

import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;

/**
 * @author emeroad
 */
public class CallRowKey implements RowKey {
    private String callApplicationName;
    private short callServiceType;
    private long rowTimeSlot;

    // 주의 hash 값 캐시는 equals/hashCode 생성시 넣으면 안됨.
    private int hash;

    public CallRowKey(String callApplicationName, short callServiceType, long rowTimeSlot) {
        if (callApplicationName == null) {
            throw new NullPointerException("callApplicationName must not be null");
        }
        this.callApplicationName = callApplicationName;
        this.callServiceType = callServiceType;
        this.rowTimeSlot = rowTimeSlot;
    }
    public byte[] getRowKey() {
        return ApplicationMapStatisticsUtils.makeRowKey(callApplicationName, callServiceType, rowTimeSlot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallRowKey that = (CallRowKey) o;

        if (callServiceType != that.callServiceType) return false;
        if (rowTimeSlot != that.rowTimeSlot) return false;
        if (callApplicationName != null ? !callApplicationName.equals(that.callApplicationName) : that.callApplicationName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int result = callApplicationName != null ? callApplicationName.hashCode() : 0;
        result = 31 * result + (int) callServiceType;
        result = 31 * result + (int) (rowTimeSlot ^ (rowTimeSlot >>> 32));
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallRowKey{");
        sb.append("callApplicationName='").append(callApplicationName).append('\'');
        sb.append(", callServiceType=").append(callServiceType);
        sb.append(", rowTimeSlot=").append(rowTimeSlot);
        sb.append('}');
        return sb.toString();
    }
}
