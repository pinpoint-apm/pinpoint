/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class CallerColumnName implements ColumnName {
    private final short callerServiceType;
    private final String callerApplicationName;
    // called or calling host
    private final String callHost;
    private final short columnSlotNumber;

    public CallerColumnName(short callerServiceType, String callerApplicationName, String callHost, short columnSlotNumber) {
        this.callerServiceType = callerServiceType;
        this.callerApplicationName = Objects.requireNonNull(callerApplicationName, "callerApplicationName");
        this.callHost = Objects.requireNonNull(callHost, "callHost");
        this.columnSlotNumber = columnSlotNumber;
    }

    public byte[] getColumnName() {
        return ApplicationMapStatisticsUtils.makeColumnName(callerServiceType, callerApplicationName, callHost, columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CallerColumnName that = (CallerColumnName) o;
        return callerServiceType == that.callerServiceType
                && columnSlotNumber == that.columnSlotNumber
                && callerApplicationName.equals(that.callerApplicationName)
                && callHost.equals(that.callHost);
    }

    @Override
    public int hashCode() {
        int result = callerServiceType;
        result = 31 * result + callerApplicationName.hashCode();
        result = 31 * result + callHost.hashCode();
        result = 31 * result + columnSlotNumber;
        return result;
    }

    @Override
    public String toString() {
        return "CallerColumnName{" +
                "callerServiceType=" + callerServiceType +
                ", callerApplicationName='" + callerApplicationName + '\'' +
                ", callHost='" + callHost + '\'' +
                ", columnSlotNumber=" + columnSlotNumber +
                '}';
    }
}
