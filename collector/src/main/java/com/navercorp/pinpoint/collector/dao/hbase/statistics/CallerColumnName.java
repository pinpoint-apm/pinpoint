package com.nhn.pinpoint.collector.dao.hbase.statistics;

import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;

/**
 * @author emeroad
 */
public class CallerColumnName implements ColumnName {
    private short callerServiceType;
    private String callerApplicationName;
    //  호출당하거나, 호출한 host,
    private String callHost;
    private short columnSlotNumber;

    // 주의 hash 값 캐시는 equals/hashCode 생성시 넣으면 안됨.
    private int hash;

    private long callCount;

    public CallerColumnName(short callerServiceType, String callerApplicationName, String callHost, short columnSlotNumber) {
        if (callerApplicationName == null) {
            throw new NullPointerException("callerApplicationName must not be null");
        }
        if (callHost == null) {
            throw new NullPointerException("callHost must not be null");
        }
        this.callerServiceType = callerServiceType;
        this.callerApplicationName = callerApplicationName;
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
        return ApplicationMapStatisticsUtils.makeColumnName(callerServiceType, callerApplicationName, callHost, columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallerColumnName that = (CallerColumnName) o;

        if (callerServiceType != that.callerServiceType) return false;
        if (columnSlotNumber != that.columnSlotNumber) return false;
        if (callerApplicationName != null ? !callerApplicationName.equals(that.callerApplicationName) : that.callerApplicationName != null) return false;
        if (callHost != null ? !callHost.equals(that.callHost) : that.callHost != null) return false;

        return true;
    }

    /**
     * hashCode수정시 주의할겻 hbasekey 캐쉬값이 있음.
     * @return
     */
    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int result = (int) callerServiceType;
        result = 31 * result + (callerApplicationName != null ? callerApplicationName.hashCode() : 0);
        result = 31 * result + (callHost != null ? callHost.hashCode() : 0);
        result = 31 * result + (int) columnSlotNumber;
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallerColumnName{");
        sb.append("callerServiceType=").append(callerServiceType);
        sb.append(", callerApplicationName='").append(callerApplicationName).append('\'');
        sb.append(", callHost='").append(callHost).append('\'');
        sb.append(", columnSlotNumber=").append(columnSlotNumber);
        sb.append(", callCount=").append(callCount);
        sb.append('}');
        return sb.toString();
    }
}
