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

import java.util.Objects;

/**
 * @author emeroad
 */
public class OutLinkV3ColumnName implements ColumnName {
    private final String outApplicationName;
    private final int outServiceType;
    // called or calling host
    private final String outSubLink;
    private final byte slotCode;

    public static ColumnName histogram(Vertex outVertex, String outSubLink, SlotCode slotCode) {
        return histogram(outVertex.serviceType(), outVertex.applicationName(), outSubLink, slotCode);
    }

    public static ColumnName histogram(ServiceType outServiceType, String outApplicationName, String outSubLink, SlotCode slotCode) {
        return new OutLinkV3ColumnName(outApplicationName, outServiceType.getCode(), outSubLink, slotCode.code());
    }

    public OutLinkV3ColumnName(String outApplicationName, int outServiceType, String outSubLink, byte slotCode) {
        this.outApplicationName = Objects.requireNonNull(outApplicationName, "outApplicationName");
        this.outServiceType = outServiceType;
        this.outSubLink = Objects.requireNonNull(outSubLink, "outSubLink");
        this.slotCode = slotCode;
    }

    public String getOutApplicationName() {
        return outApplicationName;
    }

    public int getOutServiceType() {
        return outServiceType;
    }

    public String getOutSubLink() {
        return outSubLink;
    }

    public byte getSlotCode() {
        return slotCode;
    }

    @Override
    public byte[] getColumnName() {
        final Buffer buffer = new AutomaticBuffer(64);
        buffer.putInt(IntInverter.invert(outServiceType));
        buffer.putByte(slotCode);
        buffer.putPrefixedString(outApplicationName);
        buffer.putPrefixedString(outSubLink);
        return buffer.getBuffer();
    }

    public static OutLinkV3ColumnName parseColumnName(Buffer buffer) {
        int inServiceType = IntInverter.restore(buffer.readInt());
        byte slotNumber = buffer.readByte();
        String inApplicationName = buffer.readPrefixedString();
        String outSubLink = buffer.readPrefixedString();
        return new OutLinkV3ColumnName(inApplicationName, inServiceType, outSubLink, slotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        OutLinkV3ColumnName that = (OutLinkV3ColumnName) o;
        return outServiceType == that.outServiceType && slotCode == that.slotCode && outApplicationName.equals(that.outApplicationName) && outSubLink.equals(that.outSubLink);
    }

    @Override
    public int hashCode() {
        int result = outServiceType;
        result = 31 * result + outApplicationName.hashCode();
        result = 31 * result + outSubLink.hashCode();
        result = 31 * result + slotCode;
        return result;
    }

    @Override
    public String toString() {
        return "OutColumnName{" +
               ", slotCode=" + slotCode +
               ", inServiceType=" + outServiceType +
               ", inApplicationName='" + outApplicationName + '\'' +
               ", inHost='" + outSubLink + '\'' +
               '}';
    }
}
