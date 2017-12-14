/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;

/**
 * @author emeroad
 */
public class CalleeColumnName implements ColumnName {
    private final String callerAgentId;
    private final short calleeServiceType;
    private final String calleeApplicationName;
    // called or calling host
    private final String callHost;
    private final short columnSlotNumber;

    // WARNING - cached hash value should not be included for equals/hashCode
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
        buffer.putShort(calleeServiceType);
        buffer.putPrefixedString(calleeApplicationName);
        buffer.putPrefixedString(callHost);
        buffer.putShort(columnSlotNumber);
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
        // take care when modifying this method - contains hashCodes for hbasekeys
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
