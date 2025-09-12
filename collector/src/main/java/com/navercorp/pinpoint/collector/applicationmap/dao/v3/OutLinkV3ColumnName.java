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

package com.navercorp.pinpoint.collector.applicationmap.dao.v3;

import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author emeroad
 */
public class OutLinkV3ColumnName implements ColumnName {
    private final int inServiceType;
    private final String inApplicationName;
    // called or calling host
    private final String outSubLink;
    private final short columnSlotNumber;

    public static ColumnName histogram(Vertex outVertex, String outSubLink, short columnSlotNumber) {
        return histogram(outVertex.serviceType(), outVertex.applicationName(), outSubLink, columnSlotNumber);
    }

    public static ColumnName histogram(ServiceType outServiceType, String inApplicationName, String outSubLink, short columnSlotNumber) {
        return new OutLinkV3ColumnName(outServiceType.getCode(), inApplicationName, outSubLink, columnSlotNumber);
    }

    public static ColumnName sum(Vertex inVertex, String callHost, ServiceType outServiceType) {
        return sum(inVertex.serviceType(), inVertex.applicationName(), callHost, outServiceType);
    }

    public static ColumnName sum(ServiceType inServiceType, String inApplicationName, String outSubLink, ServiceType outServiceType) {
        final short slotTime = outServiceType.getHistogramSchema().getSumStatSlot().getSlotTime();
        return histogram(inServiceType, inApplicationName, outSubLink, slotTime);
    }

    public static ColumnName max(Vertex inVertex, String outSubLink, ServiceType outServiceType) {
        final short slotTime = outServiceType.getHistogramSchema().getMaxStatSlot().getSlotTime();
        return histogram(inVertex.serviceType(), inVertex.applicationName(), outSubLink, slotTime);
    }

    public static ColumnName max(ServiceType inServiceType, String inApplicationName, String outSubLink, ServiceType outServiceType) {
        final short slotTime = outServiceType.getHistogramSchema().getMaxStatSlot().getSlotTime();
        return histogram(inServiceType, inApplicationName, outSubLink, slotTime);
    }

    public OutLinkV3ColumnName(int inServiceType, String inApplicationName, String outSubLink, short columnSlotNumber) {
        this.inServiceType = inServiceType;
        this.inApplicationName = Objects.requireNonNull(inApplicationName, "inApplicationName");
        this.outSubLink = Objects.requireNonNull(outSubLink, "outSubLink");
        this.columnSlotNumber = columnSlotNumber;
    }


    public byte[] getColumnName() {
        final Buffer buffer = new AutomaticBuffer(64);
        buffer.putInt(inServiceType);
        buffer.putShort(columnSlotNumber);

        buffer.putUnsignedBytePrefixedString(inApplicationName);
        buffer.putPrefixedString(outSubLink);
        return buffer.getBuffer();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        OutLinkV3ColumnName that = (OutLinkV3ColumnName) o;
        return inServiceType == that.inServiceType && columnSlotNumber == that.columnSlotNumber && inApplicationName.equals(that.inApplicationName) && outSubLink.equals(that.outSubLink);
    }

    @Override
    public int hashCode() {
        int result = inServiceType;
        result = 31 * result + inApplicationName.hashCode();
        result = 31 * result + outSubLink.hashCode();
        result = 31 * result + columnSlotNumber;
        return result;
    }

    @Override
    public String toString() {
        return "OutColumnName{" +
               ", columnSlotNumber=" + columnSlotNumber +
               ", inServiceType=" + inServiceType +
               ", inApplicationName='" + inApplicationName + '\'' +
               ", inHost='" + outSubLink + '\'' +
               '}';
    }
}
