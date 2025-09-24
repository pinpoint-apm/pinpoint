/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.agent;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;

public class TraceIndexRowUtils {

    private TraceIndexRowUtils() {
    }

    public static byte[] encodeRowKey(int serviceUid, String applicationName, int serviceTypeCode, long timestamp) {
        long reverseTimestamp = LongInverter.invert(timestamp);
        Buffer buffer = new FixedBuffer(HbaseTableConstants.TRACE_INDEX_ROW_KEY_SIZE);
        buffer.putPadString(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        buffer.putInt(serviceUid);
        buffer.putInt(serviceTypeCode);
        buffer.putLong(reverseTimestamp);
        return buffer.getBuffer();
    }

    public static byte[] encodeFuzzyRowKey(int saltKeySize, int serviceUid, String applicationName, int serviceTypeCode, long timestamp, byte fuzzySlotKey) {
        long reverseTimestamp = LongInverter.invert(timestamp);
        Buffer buffer = new FixedBuffer(saltKeySize + HbaseTableConstants.TRACE_INDEX_ROW_KEY_SIZE + 1); // +1 for fuzzy slot
        buffer.putPadBytes(null, saltKeySize);
        buffer.putPadString(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        buffer.putInt(serviceUid);
        buffer.putInt(serviceTypeCode);
        buffer.putLong(reverseTimestamp);
        buffer.putByte(fuzzySlotKey);
        return buffer.getBuffer();
    }

    public static long extractTimestamp(byte[] bytes, int offset) {
        long reverseStartTime = ByteArrayUtils.bytesToLong(bytes, offset);
        return LongInverter.restore(reverseStartTime);
    }

    // TraceIndex
    public static long extractAcceptTime(byte[] bytes, int baseOffset) {
        int timestampOffset = baseOffset + HbaseTableConstants.TRACE_INDEX_SALT_KEY_SIZE + HbaseTableConstants.TRACE_INDEX_TIMESTAMP_OFFSET;
        return extractTimestamp(bytes, timestampOffset);
    }

    // ApplicationTraceIndex
    public static long extractAcceptTimeV1(byte[] bytes, int baseOffset) {
        int timestampOffset = baseOffset + HbaseTableConstants.APPLICATION_NAME_MAX_LEN + HbaseTables.ApplicationTraceIndexTrace.ROW_DISTRIBUTE_SIZE;
        return extractTimestamp(bytes, timestampOffset);
    }
}
