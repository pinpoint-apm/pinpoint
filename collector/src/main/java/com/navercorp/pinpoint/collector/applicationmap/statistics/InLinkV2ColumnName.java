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

package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class InLinkV2ColumnName implements ColumnName {
    private final short selfServiceType;
    private final String selfApplicationName;
    // called or calling host
    private final String outHost;
    private final short columnSlotNumber;

    public static ColumnName histogram(Vertex selfVertex, String outHost, short columnSlotNumber) {
        return histogram(selfVertex.applicationName(), selfVertex.serviceType(), outHost, columnSlotNumber);
    }

    public static ColumnName histogram(String selfApplicationName, ServiceType selfServiceType, String outHost, short columnSlotNumber) {
        return new InLinkV2ColumnName(selfApplicationName, selfServiceType.getCode(), outHost, columnSlotNumber);
    }


    public InLinkV2ColumnName(String selfApplicationName, short selfServiceType, String outHost, short columnSlotNumber) {
        this.selfServiceType = selfServiceType;
        this.selfApplicationName = Objects.requireNonNull(selfApplicationName, "selfApplicationName");
        this.outHost = Objects.requireNonNull(outHost, "outHost");
        this.columnSlotNumber = columnSlotNumber;
    }


    public byte[] getColumnName() {
        return makeColumnName(selfServiceType, selfApplicationName, outHost, columnSlotNumber);
    }

    public static byte[] makeColumnName(short serviceType, String applicationName, String destHost, short slotNumber) {
        Objects.requireNonNull(applicationName, "applicationName");
        destHost = Objects.toString(destHost, "");

        // approximate size of destHost
        final Buffer buffer = new AutomaticBuffer(BytesUtils.SHORT_BYTE_LENGTH + PinpointConstants.APPLICATION_NAME_MAX_LEN + destHost.length() + BytesUtils.SHORT_BYTE_LENGTH);
        buffer.putShort(serviceType);
        buffer.putShort(slotNumber);
        buffer.put2PrefixedString(applicationName);
        buffer.putBytes(BytesUtils.toBytes(destHost));
        return buffer.getBuffer();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        InLinkV2ColumnName that = (InLinkV2ColumnName) o;
        return selfServiceType == that.selfServiceType
               && columnSlotNumber == that.columnSlotNumber
               && selfApplicationName.equals(that.selfApplicationName)
               && outHost.equals(that.outHost);
    }

    @Override
    public int hashCode() {
        int result = selfServiceType;
        result = 31 * result + selfApplicationName.hashCode();
        result = 31 * result + outHost.hashCode();
        result = 31 * result + columnSlotNumber;
        return result;
    }

    @Override
    public String toString() {
        return "CallerColumnName{" +
               "callerServiceType=" + selfServiceType +
               ", callerApplicationName='" + selfApplicationName + '\'' +
               ", callHost='" + outHost + '\'' +
               ", columnSlotNumber=" + columnSlotNumber +
               '}';
    }
}
