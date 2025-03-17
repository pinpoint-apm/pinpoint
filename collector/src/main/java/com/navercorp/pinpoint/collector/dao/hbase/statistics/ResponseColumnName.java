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

import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class ResponseColumnName implements ColumnName {

    private final String agentId;
    private final short columnSlotNumber;

    public ResponseColumnName(String agentId, short columnSlotNumber) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.columnSlotNumber = columnSlotNumber;
    }

    public byte[] getColumnName() {
        return ApplicationMapStatisticsUtils.makeColumnName(agentId, columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ResponseColumnName that = (ResponseColumnName) o;
        return columnSlotNumber == that.columnSlotNumber && agentId.equals(that.agentId);
    }

    @Override
    public int hashCode() {
        int result = agentId.hashCode();
        result = 31 * result + columnSlotNumber;
        return result;
    }

    @Override
    public String toString() {
        return "ResponseColumnName{" +
                "agentId='" + agentId + '\'' +
                ", columnSlotNumber=" + columnSlotNumber +
                '}';
    }
}
