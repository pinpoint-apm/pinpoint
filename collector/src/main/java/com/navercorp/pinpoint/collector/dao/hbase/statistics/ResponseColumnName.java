package com.nhn.pinpoint.collector.dao.hbase.statistics;

import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;

/**
 * @author emeroad
 */
public class ResponseColumnName implements ColumnName {

    private String agentId;
    private short columnSlotNumber;

    // 주의 hash 값 캐시는 equals/hashCode 생성시 넣으면 안됨.
    private int hash;

    private long callCount;

    public ResponseColumnName(String agentId, short columnSlotNumber) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.columnSlotNumber = columnSlotNumber;
    }

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    public byte[] getColumnName() {
        return ApplicationMapStatisticsUtils.makeColumnName(agentId, columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResponseColumnName that = (ResponseColumnName) o;

        if (columnSlotNumber != that.columnSlotNumber) return false;
        if (!agentId.equals(that.agentId)) return false;

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
        int result = agentId.hashCode();
        result = 31 * result + (int) columnSlotNumber;
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        return "ResponseColumnName{" +
                "agentId='" + agentId + '\'' +
                ", columnSlotNumber=" + columnSlotNumber +
                ", callCount=" + callCount +
                '}';
    }
}
