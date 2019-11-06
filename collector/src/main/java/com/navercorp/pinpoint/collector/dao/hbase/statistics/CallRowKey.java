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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.common.profiler.util.ApplicationMapStatisticsUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class CallRowKey implements RowKey {
    private final String callApplicationName;
    private final short callServiceType;
    private final long rowTimeSlot;

    // WARNING - cached hash value should not be included for equals/hashCode
    private int hash;

    public CallRowKey(String callApplicationName, short callServiceType, long rowTimeSlot) {
        this.callApplicationName = Objects.requireNonNull(callApplicationName, "callApplicationName");
        this.callServiceType = callServiceType;
        this.rowTimeSlot = rowTimeSlot;
    }
    public byte[] getRowKey() {
        return ApplicationMapStatisticsUtils.makeRowKey(callApplicationName, callServiceType, rowTimeSlot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallRowKey that = (CallRowKey) o;

        if (callServiceType != that.callServiceType) return false;
        if (rowTimeSlot != that.rowTimeSlot) return false;
        if (callApplicationName != null ? !callApplicationName.equals(that.callApplicationName) : that.callApplicationName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int result = callApplicationName != null ? callApplicationName.hashCode() : 0;
        result = 31 * result + (int) callServiceType;
        result = 31 * result + (int) (rowTimeSlot ^ (rowTimeSlot >>> 32));
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallRowKey{");
        sb.append("callApplicationName='").append(callApplicationName).append('\'');
        sb.append(", callServiceType=").append(callServiceType);
        sb.append(", rowTimeSlot=").append(rowTimeSlot);
        sb.append('}');
        return sb.toString();
    }
}
