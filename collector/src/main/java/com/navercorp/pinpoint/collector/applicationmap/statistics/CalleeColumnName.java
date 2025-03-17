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

package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;

import java.util.Objects;

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

    public CalleeColumnName(String callerAgentId, short calleeServiceType, String calleeApplicationName, String callHost, short columnSlotNumber) {
        this.callerAgentId = Objects.requireNonNull(callerAgentId, "callerAgentId");
        this.calleeServiceType = calleeServiceType;
        this.calleeApplicationName = Objects.requireNonNull(calleeApplicationName, "calleeApplicationName");
        this.callHost = Objects.requireNonNull(callHost, "callHost");
        this.columnSlotNumber = columnSlotNumber;
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
        if (o == null || getClass() != o.getClass()) return false;

        CalleeColumnName that = (CalleeColumnName) o;
        return calleeServiceType == that.calleeServiceType
                && columnSlotNumber == that.columnSlotNumber
                && callerAgentId.equals(that.callerAgentId)
                && calleeApplicationName.equals(that.calleeApplicationName)
                && callHost.equals(that.callHost);
    }

    @Override
    public int hashCode() {
        int result = callerAgentId.hashCode();
        result = 31 * result + calleeServiceType;
        result = 31 * result + calleeApplicationName.hashCode();
        result = 31 * result + callHost.hashCode();
        result = 31 * result + columnSlotNumber;
        return result;
    }

    @Override
    public String toString() {
        return "CalleeColumnName{" +
                "callerAgentId='" + callerAgentId + '\'' +
                ", columnSlotNumber=" + columnSlotNumber +
                ", calleeServiceType=" + calleeServiceType +
                ", calleeApplicationName='" + calleeApplicationName + '\'' +
                ", callHost='" + callHost + '\'' +
                '}';
    }
}
