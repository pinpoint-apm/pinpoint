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

import com.navercorp.pinpoint.collector.dao.hbase.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.util.ApplicationMapUtils;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ApplicationMapColumnName implements ColumnName {

    private final String serviceName;
    private final short applicationTypeCode;
    private final String applicationName;
    private final short columnSlotNumber;

    // WARNING - cached hash value should not be included for equals/hashCode
    private int hash;
    private long callCount;

    public ApplicationMapColumnName(
            String serviceName,
            short applicationTypeCode, String applicationName,
            short columnSlotNumber
    ) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.applicationTypeCode = applicationTypeCode;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.columnSlotNumber = columnSlotNumber;
    }

    @Override
    public byte[] getColumnName() {
        return ApplicationMapUtils.makeColumnName(
                serviceName, applicationName, applicationTypeCode, columnSlotNumber
        );
    }

    @Override
    public long getCallCount() {
        return callCount;
    }

    @Override
    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationMapColumnName that = (ApplicationMapColumnName) o;

        if (applicationTypeCode != that.applicationTypeCode) return false;
        if (columnSlotNumber != that.columnSlotNumber) return false;
        if (hash != that.hash) return false;
        if (callCount != that.callCount) return false;
        if (!serviceName.equals(that.serviceName)) return false;
        return applicationName.equals(that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + (int) applicationTypeCode;
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + (int) columnSlotNumber;
        result = 31 * result + hash;
        result = 31 * result + (int) (callCount ^ (callCount >>> 32));
        return result;
    }
}
