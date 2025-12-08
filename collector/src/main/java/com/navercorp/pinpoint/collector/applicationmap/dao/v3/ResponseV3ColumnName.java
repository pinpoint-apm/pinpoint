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

import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.trace.SlotCode;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class ResponseV3ColumnName implements ColumnName {

    private final String agentId;
    private final byte slotCode;

    public static ColumnName histogram(String agentId, SlotCode slotCode) {
        return new ResponseV3ColumnName(agentId, slotCode.code());
    }

    public ResponseV3ColumnName(String agentId, byte slotCode) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.slotCode = slotCode;
    }

    public byte[] getColumnName() {
        return makeColumnName(agentId, slotCode);
    }

    public static byte[] makeColumnName(String agentId, byte slotCode) {
        Objects.requireNonNull(agentId, "agentId");

        final Buffer buffer = new AutomaticBuffer(agentId.length() + 1);
        buffer.putByte(slotCode);

        final byte[] agentIdBytes = BytesUtils.toBytes(agentId);
        buffer.putBytes(agentIdBytes);

        return buffer.getBuffer();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ResponseV3ColumnName that = (ResponseV3ColumnName) o;
        return slotCode == that.slotCode && agentId.equals(that.agentId);
    }

    @Override
    public int hashCode() {
        int result = agentId.hashCode();
        result = 31 * result + slotCode;
        return result;
    }

    @Override
    public String toString() {
        return "ResponseColumnName{" +
               "agentId='" + agentId + '\'' +
               ", slotCode=" + slotCode +
               '}';
    }
}
