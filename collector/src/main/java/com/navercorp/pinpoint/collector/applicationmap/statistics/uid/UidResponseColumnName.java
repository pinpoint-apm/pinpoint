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

package com.navercorp.pinpoint.collector.applicationmap.statistics.uid;

import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author emeroad
 */
public class UidResponseColumnName implements ColumnName {

    private final short columnSlotNumber;

    public static ColumnName histogram(short columnSlotNumber) {
        return new UidResponseColumnName(columnSlotNumber);
    }

    public static ColumnName sum(ServiceType serviceType) {
        short slotTime = serviceType.getHistogramSchema().getSumStatSlot().getSlotTime();
        return new UidResponseColumnName(slotTime);
    }

    public static ColumnName max(ServiceType serviceType) {
        short slotTime = serviceType.getHistogramSchema().getMaxStatSlot().getSlotTime();
        return new UidResponseColumnName(slotTime);
    }

    public UidResponseColumnName(short columnSlotNumber) {
        this.columnSlotNumber = columnSlotNumber;
    }

    public byte[] getColumnName() {
        return Bytes.toBytes(columnSlotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidResponseColumnName that = (UidResponseColumnName) o;
        return columnSlotNumber == that.columnSlotNumber;
    }

    @Override
    public int hashCode() {
        return columnSlotNumber;
    }

    @Override
    public String toString() {
        return "ResponseColumnName{" +
                ", columnSlotNumber=" + columnSlotNumber +
                '}';
    }
}
