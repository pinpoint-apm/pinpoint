/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;

/**
 * @author emeroad
 */
public class CallerColumnName implements ColumnName {
    private short callerServiceType;
    private String callerApplicationName;
    // called or calling host
    private String callHost;
    private short columnSlotNumber;

    // WARNING - cached hash value should not be included for equals/hashCode
    private int hash;

    private long callCount;

    public CallerColumnName(short callerServiceType, String callerApplicationName, String callHost, short columnSlotNumber) {
        if (callerApplicationName == null) {
            throw new NullPointerException("callerApplicationName must not be null");
        }
        if (callHost == null) {
            throw new NullPointerException("callHost must not be null");
        }
        this.callerServiceType = callerServiceType;
        this.callerApplicationName = callerApplicationName;
        this.callHost = callHost;
        this.columnSlotNumber = columnSlotNumber;
    }

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    public byte[] getColumnName() {
        return ApplicationMapStatisticsUtils.makeColumnName(callerServiceType, callerApplicationName, callHost, columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallerColumnName that = (CallerColumnName) o;

        if (callerServiceType != that.callerServiceType) return false;
        if (columnSlotNumber != that.columnSlotNumber) return false;
        if (callerApplicationName != null ? !callerApplicationName.equals(that.callerApplicationName) : that.callerApplicationName != null) return false;
        if (callHost != null ? !callHost.equals(that.callHost) : that.callHost != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        // take care when modifying this method - contains hashCodes for hbasekeys 
        if (hash != 0) {
            return hash;
        }
        int result = (int) callerServiceType;
        result = 31 * result + (callerApplicationName != null ? callerApplicationName.hashCode() : 0);
        result = 31 * result + (callHost != null ? callHost.hashCode() : 0);
        result = 31 * result + (int) columnSlotNumber;
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallerColumnName{");
        sb.append("callerServiceType=").append(callerServiceType);
        sb.append(", callerApplicationName='").append(callerApplicationName).append('\'');
        sb.append(", callHost='").append(callHost).append('\'');
        sb.append(", columnSlotNumber=").append(columnSlotNumber);
        sb.append(", callCount=").append(callCount);
        sb.append('}');
        return sb.toString();
    }
}
