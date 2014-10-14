package com.nhn.pinpoint.collector.dao.hbase.statistics;

import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;

/**
 * @author emeroad
 */
public class CalleeColumnName implements ColumnName {
    private final String callerAgentId;
    private final short calleeServiceType;
    private final String calleeApplicationName;
    //  호출당하거나, 호출한 host,
    private final String callHost;
    private final short columnSlotNumber;

    // 주의 hash 값 캐시는 equals/hashCode 생성시 넣으면 안됨.
    private int hash;

    private long callCount;

    public CalleeColumnName(String callerAgentId, short calleeServiceType, String calleeApplicationName, String callHost, short columnSlotNumber) {
        if (callerAgentId == null) {
            throw new NullPointerException("callerAgentId must not be null");
        }
        if (calleeApplicationName == null) {
            throw new NullPointerException("calleeApplicationName must not be null");
        }
        if (callHost == null) {
            throw new NullPointerException("callHost must not be null");
        }
        this.callerAgentId = callerAgentId;
        this.calleeServiceType = calleeServiceType;
        this.calleeApplicationName = calleeApplicationName;
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
        final Buffer buffer = new AutomaticBuffer(64);
        buffer.put(calleeServiceType);
        buffer.putPrefixedString(calleeApplicationName);
        buffer.putPrefixedString(callHost);
        buffer.put(columnSlotNumber);
        buffer.putPrefixedString(callerAgentId);
        return buffer.getBuffer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalleeColumnName that = (CalleeColumnName) o;

        if (callCount != that.callCount) return false;
        if (calleeServiceType != that.calleeServiceType) return false;
        if (columnSlotNumber != that.columnSlotNumber) return false;
        if (!callHost.equals(that.callHost)) return false;
        if (!calleeApplicationName.equals(that.calleeApplicationName)) return false;
        if (!callerAgentId.equals(that.callerAgentId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int result = callerAgentId.hashCode();
        result = 31 * result + (int) calleeServiceType;
        result = 31 * result + calleeApplicationName.hashCode();
        result = 31 * result + callHost.hashCode();
        result = 31 * result + (int) columnSlotNumber;
        result = 31 * result + hash;
        result = 31 * result + (int) (callCount ^ (callCount >>> 32));
        this.hash = result;
        return result;
    }

    /**
     * hashCode수정시 주의할겻 hbasekey 캐쉬값이 있음.
     * @return
     */


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CalleeColumnName{");
        sb.append("callerAgentId=").append(callerAgentId);
        sb.append(", calleeServiceType=").append(calleeServiceType);
        sb.append(", calleeApplicationName='").append(calleeApplicationName).append('\'');
        sb.append(", callHost='").append(callHost).append('\'');
        sb.append(", columnSlotNumber=").append(columnSlotNumber);
        sb.append(", callCount=").append(callCount);
        sb.append('}');
        return sb.toString();
    }
}
