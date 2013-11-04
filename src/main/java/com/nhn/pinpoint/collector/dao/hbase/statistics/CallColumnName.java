package com.nhn.pinpoint.collector.dao.hbase.statistics;

import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;

/**
 * @author emeroad
 */
public class CallColumnName implements ColumnName {
    private short callServiceType;
    private String callApplicationName;
    //  호출당하거나, 호출한 host,
    private String callHost;
    private short columnSlotNumber;

    // 주의 hash 값 캐시는 equals/hashCode 생성시 넣으면 안됨.
    private int hash;

    private long callCount;

    public CallColumnName(short callServiceType, String callApplicationName, String callHost, short columnSlotNumber) {
        if (callApplicationName == null) {
            throw new NullPointerException("callApplicationName must not be null");
        }
        if (callHost == null) {
            throw new NullPointerException("callHost must not be null");
        }
        this.callServiceType = callServiceType;
        this.callApplicationName = callApplicationName;
        this.callHost = callHost;
        this.columnSlotNumber = columnSlotNumber;
    }

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    public byte[] getColumnName() {
        return ApplicationMapStatisticsUtils.makeColumnName(callServiceType, callApplicationName, callHost, columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallColumnName that = (CallColumnName) o;

        if (callServiceType != that.callServiceType) return false;
        if (columnSlotNumber != that.columnSlotNumber) return false;
        if (callApplicationName != null ? !callApplicationName.equals(that.callApplicationName) : that.callApplicationName != null) return false;
        if (callHost != null ? !callHost.equals(that.callHost) : that.callHost != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int result = (int) callServiceType;
        result = 31 * result + (callApplicationName != null ? callApplicationName.hashCode() : 0);
        result = 31 * result + (callHost != null ? callHost.hashCode() : 0);
        result = 31 * result + (int) columnSlotNumber;
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallColumnName{");
        sb.append("callServiceType=").append(callServiceType);
        sb.append(", callApplicationName='").append(callApplicationName).append('\'');
        sb.append(", callHost='").append(callHost).append('\'');
        sb.append(", columnSlotNumber=").append(columnSlotNumber);
        sb.append(", callCount=").append(callCount);
        sb.append('}');
        return sb.toString();
    }
}
