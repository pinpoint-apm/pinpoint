package com.nhn.pinpoint.collector.dao.hbase.statistics;

import com.nhn.pinpoint.common.util.ApplicationStatisticsUtils;

/**
 * @author emeroad
 */
public class StatisticsColumnName implements ColumnName {
    private String agentId;
    private short columnSlotNumber;

    private int hash;

    private long callCount;

    public StatisticsColumnName(String agentId, short columnSlotNumber) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.columnSlotNumber = columnSlotNumber;
    }

    @Override
    public byte[] getColumnName() {
        return ApplicationStatisticsUtils.makeColumnName(agentId, columnSlotNumber);
    }

    @Override
    public long getCallCount() {
        return callCount;
    }

    @Override
    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatisticsColumnName that = (StatisticsColumnName) o;

        if (columnSlotNumber != that.columnSlotNumber) return false;
        if (!agentId.equals(that.agentId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int result = agentId.hashCode();
        result = 31 * result + (int) columnSlotNumber;
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatisticsColumnName{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", columnSlotNumber=").append(columnSlotNumber);
        sb.append(", callCount=").append(callCount);
        sb.append('}');
        return sb.toString();
    }
}
