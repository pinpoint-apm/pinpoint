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

package com.navercorp.pinpoint.common.server.applicationmap.statistics.v3;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.timeseries.util.IntInverter;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.SlotCode;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class InLinkV3ColumnName implements ColumnName {
    private final int selfServiceType;
    private final String selfApplicationName;
    // called or calling host
    private final String outHost;
    private final byte slotCode;

    public static ColumnName histogram(Vertex selfVertex, String outHost, SlotCode slotCode) {
        return histogram(selfVertex.applicationName(), selfVertex.serviceType(), outHost, slotCode);
    }

    public static ColumnName histogram(String selfApplicationName, ServiceType selfServiceType, String outHost, SlotCode slotCode) {
        return new InLinkV3ColumnName(selfApplicationName, selfServiceType.getCode(), outHost, slotCode.code());
    }

    public InLinkV3ColumnName(String selfApplicationName, int selfServiceType, String outHost, byte slotCode) {
        this.selfServiceType = selfServiceType;
        this.selfApplicationName = Objects.requireNonNull(selfApplicationName, "selfApplicationName");
        this.outHost = Objects.requireNonNull(outHost, "outHost");
        this.slotCode = slotCode;
    }

    public int getSelfServiceType() {
        return selfServiceType;
    }

    public String getSelfApplicationName() {
        return selfApplicationName;
    }

    public String getOutHost() {
        return outHost;
    }

    public byte getSlotCode() {
        return slotCode;
    }

    @Override
    public byte[] getColumnName() {
        return makeColumnName(selfServiceType, selfApplicationName, outHost, slotCode);
    }

    public static byte[] makeColumnName(int selfServiceType, String selfApplicationName, String destHost, byte slotCode) {
        Objects.requireNonNull(selfApplicationName, "selfApplicationName");
        destHost = Objects.toString(destHost, "");

        // approximate size of destHost
        final Buffer buffer = new AutomaticBuffer(
                BytesUtils.INT_BYTE_LENGTH + BytesUtils.BYTE_LENGTH +
                BytesUtils.computeVar32StringSize(selfApplicationName) +
                BytesUtils.computeVar32StringSize(destHost)
        );
        buffer.putInt(IntInverter.invert(selfServiceType));
        buffer.putByte(slotCode);
        buffer.putPrefixedString(selfApplicationName);
        buffer.putPrefixedString(destHost);
        return buffer.getBuffer();
    }


    public static InLinkV3ColumnName parseColumnName(Buffer buffer) {
        int selfServiceType = IntInverter.restore(buffer.readInt());
        byte slotCode = buffer.readByte();
        String selfApplicationName = buffer.readPrefixedString();
        String destHost = buffer.readPrefixedString();
        return new InLinkV3ColumnName(selfApplicationName, selfServiceType, destHost, slotCode);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        InLinkV3ColumnName that = (InLinkV3ColumnName) o;
        return selfServiceType == that.selfServiceType
               && slotCode == that.slotCode
               && selfApplicationName.equals(that.selfApplicationName)
               && outHost.equals(that.outHost);
    }

    @Override
    public int hashCode() {
        int result = selfServiceType;
        result = 31 * result + selfApplicationName.hashCode();
        result = 31 * result + outHost.hashCode();
        result = 31 * result + slotCode;
        return result;
    }

    @Override
    public String toString() {
        return "CallerColumnName{" +
               "callerServiceType=" + selfServiceType +
               ", callerApplicationName='" + selfApplicationName + '\'' +
               ", callHost='" + outHost + '\'' +
               ", slotCode=" + slotCode +
               '}';
    }
}
