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

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;

public class TraceIndexRowUtils {

    private TraceIndexRowUtils() {
    }

    public static long extractTimestamp(byte[] bytes, int offset) {
        long reverseStartTime = ByteArrayUtils.bytesToLong(bytes, offset);
        return LongInverter.restore(reverseStartTime);
    }

    // TraceIndex
    public static long extractAcceptTime(byte[] bytes, int baseOffset) {
        int timestampOffset = baseOffset + HbaseTableConstants.TRACE_INDEX_TIMESTAMP_OFFSET;
        return extractTimestamp(bytes, timestampOffset);
    }
}
