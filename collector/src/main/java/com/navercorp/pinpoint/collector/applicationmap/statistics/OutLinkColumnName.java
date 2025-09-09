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

import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author emeroad
 */
public class OutLinkColumnName implements ColumnName {
    private final short outServiceType;
    private final String outApplicationName;
    // called or calling host
    private final String outHost;
    private final short columnSlotNumber;

    public static ColumnName histogram(Vertex outVertex, String outHost, short columnSlotNumber) {
        return histogram(outVertex.applicationName(), outVertex.serviceType(), outHost, columnSlotNumber);
    }

    public static ColumnName histogram(String outApplicationName, ServiceType outServiceType, String outHost, short columnSlotNumber) {
        return new OutLinkColumnName(outApplicationName, outServiceType.getCode(), outHost, columnSlotNumber);
    }

    public static ColumnName sum(Vertex outVertex, String outHost, ServiceType inServiceType) {
        return sum(outVertex.applicationName(), outVertex.serviceType(), outHost, inServiceType);
    }

    public static ColumnName sum(String outApplicationName, ServiceType outServiceType, String outHost, ServiceType inServiceType) {
        final short slotTime = inServiceType.getHistogramSchema().getSumStatSlot().getSlotTime();
        return histogram(outApplicationName, outServiceType, outHost, slotTime);
    }

    public static ColumnName max(Vertex outVertex, String outHost, ServiceType inServiceType) {
        return max(outVertex.applicationName(), outVertex.serviceType(), outHost, inServiceType);
    }

    public static ColumnName max(String outApplicationName, ServiceType outServiceType, String outHost, ServiceType inServiceType) {
        final short slotTime = inServiceType.getHistogramSchema().getMaxStatSlot().getSlotTime();
        return histogram(outApplicationName, outServiceType, outHost, slotTime);
    }

    public OutLinkColumnName(String outApplicationName, short outServiceType, String outHost, short columnSlotNumber) {
        this.outServiceType = outServiceType;
        this.outApplicationName = Objects.requireNonNull(outApplicationName, "outApplicationName");
        this.outHost = Objects.requireNonNull(outHost, "outHost");
        this.columnSlotNumber = columnSlotNumber;
    }


    public byte[] getColumnName() {
        return ApplicationMapStatisticsUtils.makeColumnName(outServiceType, outApplicationName, outHost, columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        OutLinkColumnName that = (OutLinkColumnName) o;
        return outServiceType == that.outServiceType
                && columnSlotNumber == that.columnSlotNumber
                && outApplicationName.equals(that.outApplicationName)
                && outHost.equals(that.outHost);
    }

    @Override
    public int hashCode() {
        int result = outServiceType;
        result = 31 * result + outApplicationName.hashCode();
        result = 31 * result + outHost.hashCode();
        result = 31 * result + columnSlotNumber;
        return result;
    }

    @Override
    public String toString() {
        return "CallerColumnName{" +
                "callerServiceType=" + outServiceType +
                ", callerApplicationName='" + outApplicationName + '\'' +
                ", callHost='" + outHost + '\'' +
                ", columnSlotNumber=" + columnSlotNumber +
                '}';
    }
}
