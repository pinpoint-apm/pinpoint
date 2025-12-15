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
public class ResponseV3ColumnName implements ColumnName {

    private final byte slotCode;

    public static ColumnName histogram(SlotCode slotCode) {
        return new ResponseV3ColumnName(slotCode.code());
    }

    public ResponseV3ColumnName(byte slotCode) {
        this.slotCode = slotCode;
    }

    public byte[] getColumnName() {
        return makeColumnName(slotCode);
    }

    public static byte[] makeColumnName(byte slotCode) {
        return new byte[] { slotCode };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ResponseV3ColumnName that = (ResponseV3ColumnName) o;
        return slotCode == that.slotCode;
    }

    @Override
    public int hashCode() {
        return slotCode;
    }

    @Override
    public String toString() {
        return "ResponseV3ColumnName{" +
               "slotCode=" + slotCode +
               '}';
    }
}
