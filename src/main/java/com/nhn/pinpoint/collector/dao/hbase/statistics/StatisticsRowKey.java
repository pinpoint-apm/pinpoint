package com.nhn.pinpoint.collector.dao.hbase.statistics;

import com.nhn.pinpoint.common.util.ApplicationStatisticsUtils;

/**
 * @author emeroad
 */
public class StatisticsRowKey implements RowKey {
    private String applicationName;
    private short applicationType;
    private long rowTimeSlot;
    // 주의 hash 값 캐시는 equals/hashCode 생성시 넣으면 안됨.
    private int hash;

    public StatisticsRowKey(String applicationName, short applicationType, long rowTimeSlot) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        this.applicationName = applicationName;
        this.applicationType = applicationType;
        this.rowTimeSlot = rowTimeSlot;
    }


    @Override
    public byte[] getRowKey() {
        return ApplicationStatisticsUtils.makeRowKey(applicationName, applicationType, rowTimeSlot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatisticsRowKey that = (StatisticsRowKey) o;

        if (applicationType != that.applicationType) return false;
        if (rowTimeSlot != that.rowTimeSlot) return false;
        if (!applicationName.equals(that.applicationName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if(hash != 0) {
            return hash;
        }
        int result = applicationName.hashCode();
        result = 31 * result + (int) applicationType;
        result = 31 * result + (int) (rowTimeSlot ^ (rowTimeSlot >>> 32));
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatisticsRowKey{");
        sb.append("applicationName='").append(applicationName).append('\'');
        sb.append(", applicationType=").append(applicationType);
        sb.append(", rowTimeSlot=").append(rowTimeSlot);
        sb.append('}');
        return sb.toString();
    }
}
