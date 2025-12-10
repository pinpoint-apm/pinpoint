/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.applicationmap.statistics.v2;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author emeroad
 */
public class OutLinkV2ColumnName implements ColumnName {
    private final String outAgentId;

    private final short inServiceType;
    private final String inApplicationName;
    // called or calling host
    private final String callHost;
    private final short slotNumber;

    public String getOutAgentId() {
        return outAgentId;
    }

    public short getInServiceType() {
        return inServiceType;
    }

    public String getInApplicationName() {
        return inApplicationName;
    }

    public String getCallHost() {
        return callHost;
    }

    public short getSlotNumber() {
        return slotNumber;
    }

    public static ColumnName histogram(String outAgentId, Vertex inVertex, String callHost, short columnSlotNumber) {
        return histogram(outAgentId, inVertex.serviceType(), inVertex.applicationName(), callHost, columnSlotNumber);
    }

    public static ColumnName histogram(String outAgentId, ServiceType inServiceType, String inApplicationName, String callHost, short columnSlotNumber) {
        return new OutLinkV2ColumnName(outAgentId, inServiceType.getCode(), inApplicationName, callHost, columnSlotNumber);
    }

    public OutLinkV2ColumnName(String outAgentId, short inServiceType, String inApplicationName, String callHost, short slotNumber) {
        this.outAgentId = Objects.requireNonNull(outAgentId, "outAgentId");
        this.inServiceType = inServiceType;
        this.inApplicationName = Objects.requireNonNull(inApplicationName, "inApplicationName");
        this.callHost = Objects.requireNonNull(callHost, "callHost");
        this.slotNumber = slotNumber;
    }

    @Override
    public byte[] getColumnName() {
        final Buffer buffer = new AutomaticBuffer(64);
        buffer.putShort(inServiceType);
        buffer.putPrefixedString(inApplicationName);
        buffer.putPrefixedString(callHost);
        buffer.putShort(slotNumber);
        buffer.putPrefixedString(outAgentId);
        return buffer.getBuffer();
    }

    public static OutLinkV2ColumnName parseColumnName(Buffer buffer) {
        short inServiceType = buffer.readShort();
        String inApplication = buffer.readPrefixedString();
        String callHost = buffer.readPrefixedString();
        short slotNumber = buffer.readShort();
        String outAgentId = buffer.readPrefixedString();
        return new OutLinkV2ColumnName(outAgentId, inServiceType, inApplication, callHost, slotNumber);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        OutLinkV2ColumnName that = (OutLinkV2ColumnName) o;
        return inServiceType == that.inServiceType
               && slotNumber == that.slotNumber
               && outAgentId.equals(that.outAgentId)
               && inApplicationName.equals(that.inApplicationName)
               && callHost.equals(that.callHost);
    }

    @Override
    public int hashCode() {
        int result = outAgentId.hashCode();
        result = 31 * result + inServiceType;
        result = 31 * result + inApplicationName.hashCode();
        result = 31 * result + callHost.hashCode();
        result = 31 * result + slotNumber;
        return result;
    }

    @Override
    public String toString() {
        return "OutColumnName{" +
               "outAgentId='" + outAgentId + '\'' +
               ", columnSlotNumber=" + slotNumber +
               ", inServiceType=" + inServiceType +
               ", inApplicationName='" + inApplicationName + '\'' +
               ", inHost='" + callHost + '\'' +
               '}';
    }
}
