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

package com.navercorp.pinpoint.collector.applicationmap.dao.v3;

import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.trace.SlotCode;

/**
 * @author emeroad
 */
public class ApplicationResponseColumnName implements ColumnName {

    private final byte slotCode;

    public static ColumnName histogram(SlotCode slotCode) {
        return new ApplicationResponseColumnName(slotCode.code());
    }

    public ApplicationResponseColumnName(byte slotCode) {
        this.slotCode = slotCode;
    }

    public byte[] getColumnName() {
        return makeColumnName(slotCode);
    }

    public static byte[] makeColumnName(byte slotCode) {
        byte[] bytes = new byte[1];
        bytes[0] = slotCode;
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationResponseColumnName that = (ApplicationResponseColumnName) o;
        return slotCode == that.slotCode;
    }

    @Override
    public int hashCode() {
        return slotCode;
    }

    @Override
    public String toString() {
        return "ResponseColumnName{" +
               ", slotCode=" + slotCode +
               '}';
    }
}
