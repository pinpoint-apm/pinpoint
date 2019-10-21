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
public class ResponseColumnName implements ColumnName {

    private String agentId;
    private short columnSlotNumber;

 // WARNING - cached hash value should not be included for equals/hashCode
    private int hash;

    private long callCount;

    public ResponseColumnName(String agentId, short columnSlotNumber) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.columnSlotNumber = columnSlotNumber;
    }

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    public byte[] getColumnName() {
        return ApplicationMapStatisticsUtils.makeColumnName(agentId, columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResponseColumnName that = (ResponseColumnName) o;

        if (columnSlotNumber != that.columnSlotNumber) return false;
        if (!agentId.equals(that.agentId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        // take care when modifying this method - contains hashCodes for hbasekeys
        if (hash != 0) {
            return hash;
        }
        int result = agentId.hashCode();
        result = 31 * result + (int) columnSlotNumber;
        hash = result;
        return result;
    }

    @Override
    public String toString() {
        return "ResponseColumnName{" +
                "agentId='" + agentId + '\'' +
                ", columnSlotNumber=" + columnSlotNumber +
                ", callCount=" + callCount +
                '}';
    }
}
