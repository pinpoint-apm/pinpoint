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
package com.navercorp.pinpoint.servermap.dao.hbase.statistics;

import com.navercorp.pinpoint.common.server.applicationmap.ApplicationMapUtils;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ApplicationMapColumnName {
    private final int serviceId;
    private final short applicationTypeCode;
    private final String applicationName;
    private final short columnSlotNumber;

    // WARNING - cached hash value should not be included for equals/hashCode
    private int hash;

    public ApplicationMapColumnName(
            int serviceId,
            short applicationTypeCode, String applicationName,
            short columnSlotNumber
    ) {
        this.serviceId = serviceId;
        this.applicationTypeCode = applicationTypeCode;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.columnSlotNumber = columnSlotNumber;
    }

    public byte[] getColumnName() {
        return ApplicationMapUtils.makeColumnName(
                serviceId,
                applicationName, applicationTypeCode,
                columnSlotNumber
        );
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationMapColumnName that)) return false;

        if (serviceId != that.serviceId) return false;
        if (applicationTypeCode != that.applicationTypeCode) return false;
        if (columnSlotNumber != that.columnSlotNumber) return false;
        return applicationName.equals(that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = serviceId;
        result = 31 * result + (int) applicationTypeCode;
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + (int) columnSlotNumber;
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationMapColumnName{" +
                "serviceId='" + serviceId + '\'' +
                ", applicationTypeCode=" + applicationTypeCode +
                ", applicationName='" + applicationName + '\'' +
                ", columnSlotNumber=" + columnSlotNumber +
                '}';
    }
}