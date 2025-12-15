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

package com.navercorp.pinpoint.common.server.applicationmap.statistics.v3;

import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.trace.SlotCode;
import org.apache.hadoop.hbase.Cell;


/**
 * @author emeroad
 */
public class ColumnNameV3 implements ColumnName {

    private final byte slotCode;

    public static ColumnName histogram(SlotCode slotCode) {
        return new ColumnNameV3(slotCode.code());
    }

    public ColumnNameV3(byte slotCode) {
        this.slotCode = slotCode;
    }

    public byte getSlotCode() {
        return slotCode;
    }

    public byte[] getColumnName() {
        return makeColumnName(slotCode);
    }

    public static byte[] makeColumnName(byte slotCode) {
        return new byte[] { slotCode };
    }

    public static ColumnNameV3 makeColumnName(Cell cell) {
        byte[] qualifierArray = cell.getQualifierArray();
        byte slotCode = qualifierArray[cell.getQualifierOffset()];
        return new ColumnNameV3(slotCode);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ColumnNameV3 that = (ColumnNameV3) o;
        return slotCode == that.slotCode;
    }

    @Override
    public int hashCode() {
        return slotCode;
    }

    @Override
    public String toString() {
        return "ColumnNameV3{" +
               "slotCode=" + slotCode +
               '}';
    }
}
