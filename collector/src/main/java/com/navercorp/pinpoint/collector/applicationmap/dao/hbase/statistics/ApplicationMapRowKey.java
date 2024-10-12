/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.dao.hbase.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.util.ApplicationMapUtils;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ApplicationMapRowKey implements RowKey {
    private final String serviceName;
    private final short applicationType;
    private final String applicationName;
    private final long rowTimeSlot;

    // WARNING - cached hash value should not be included for equals/hashCode
    private int hash;

    public ApplicationMapRowKey(
            String serviceName,
            short applicationType, String applicationName,
            long rowTimeSlot
    ) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.applicationType = applicationType;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.rowTimeSlot = rowTimeSlot;
    }

    @Override
    public byte[] getRowKey() {
        return ApplicationMapUtils.makeRowKey(serviceName, applicationName, applicationType, rowTimeSlot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationMapRowKey that = (ApplicationMapRowKey) o;

        if (applicationType != that.applicationType) return false;
        if (rowTimeSlot != that.rowTimeSlot) return false;
        if (hash != that.hash) return false;
        if (!serviceName.equals(that.serviceName)) return false;
        return applicationName.equals(that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + (int) applicationType;
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + (int) (rowTimeSlot ^ (rowTimeSlot >>> 32));
        result = 31 * result + hash;
        return result;
    }

    @Override
    public String toString() {
        return "ServiceGroupRowKey{" +
                "callServiceGroup='" + serviceName + '\'' +
                ", thisServiceType=" + applicationType +
                ", thisApplicationName='" + applicationName + '\'' +
                ", rowTimeSlot=" + rowTimeSlot +
                ", hash=" + hash +
                '}';
    }
}