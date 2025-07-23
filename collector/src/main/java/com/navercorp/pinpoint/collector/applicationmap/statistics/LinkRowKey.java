/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.collector.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author emeroad
 */
public class LinkRowKey implements RowKey {
    private final String applicationName;
    private final short serviceType;
    private final long rowTimeSlot;

    public static RowKey of(Vertex vertex, long rowTimeSlot) {
        return new LinkRowKey(vertex.applicationName(), vertex.serviceType().getCode(), rowTimeSlot);
    }

    public static RowKey of(String applicationName, ServiceType serviceType, long rowTimeSlot) {
        return new LinkRowKey(applicationName, serviceType.getCode(), rowTimeSlot);
    }

    LinkRowKey(String applicationName, short serviceType, long rowTimeSlot) {
        this.applicationName = Objects.requireNonNull(applicationName, "callApplicationName");
        this.serviceType = serviceType;
        this.rowTimeSlot = rowTimeSlot;
    }

    public byte[] getRowKey(int saltKeySize) {
        return ApplicationMapStatisticsUtils.makeRowKey(saltKeySize, applicationName, serviceType, rowTimeSlot);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        LinkRowKey that = (LinkRowKey) o;
        return serviceType == that.serviceType
                && rowTimeSlot == that.rowTimeSlot
                && applicationName.equals(that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = applicationName.hashCode();
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(rowTimeSlot);
        return result;
    }

    @Override
    public String toString() {
        return "CallRowKey{" +
                "applicationName='" + applicationName + '\'' +
                ", serviceType=" + serviceType +
                ", rowTimeSlot=" + rowTimeSlot +
                '}';
    }
}
