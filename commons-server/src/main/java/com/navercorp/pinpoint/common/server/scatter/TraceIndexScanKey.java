/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Scan start/end row keys of the trace index table for one (application, range) query.
 * <p>
 * The row timestamp is stored reversed, so the scan start key is built from {@code range.getTo()}
 * and the scan end key from {@code range.getFrom()}.
 * <p>
 * The salt key is not written; it is applied per partition by the {@code RowKeyDistributor}.
 */
public class TraceIndexScanKey {
    // applicationNameHash(4) + serviceUid(4) + serviceType(4)
    private static final int TIMESTAMP_OFFSET = 4 + 4 + 4;
    // pad spanId(8) + elapsed(1) + error(1) + agentIdHash(2) to prevent ArrayIndexOutOfBoundsException
    private static final int PAD_SIZE = 8 + 1 + 1 + 2;

    private final byte[] startKey;
    private final byte[] endKey;

    public TraceIndexScanKey(int serviceUid, String applicationName, int serviceTypeCode, Range range) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");

        // start key uses the end of the range and end key uses the start of the range
        // because the row timestamp is reversed
        this.startKey = encodeScanKey(serviceUid, applicationName, serviceTypeCode, range.getTo());
        this.endKey = copyWithTimestamp(this.startKey, range.getFrom());
    }

    private static byte[] encodeScanKey(int serviceUid, String applicationName, int serviceTypeCode, long timestamp) {
        long reverseTimestamp = LongInverter.invert(timestamp);
        Buffer buffer = new FixedBuffer(TIMESTAMP_OFFSET + 8 + PAD_SIZE);
        buffer.putInt(TraceIndexRowKeyUtils.toApplicationNameHash(applicationName));
        buffer.putInt(serviceUid);
        buffer.putInt(serviceTypeCode);
        buffer.putLong(reverseTimestamp);

        buffer.putPadBytes(null, PAD_SIZE);
        return buffer.getBuffer();
    }

    /**
     * the two keys differ only in the timestamp, so copy the start key and overwrite the timestamp
     */
    private static byte[] copyWithTimestamp(byte[] scanKey, long timestamp) {
        byte[] copy = Arrays.copyOf(scanKey, scanKey.length);
        ByteArrayUtils.writeLong(LongInverter.invert(timestamp), copy, TIMESTAMP_OFFSET);
        return copy;
    }

    public byte[] startKey() {
        return startKey;
    }

    public byte[] endKey() {
        return endKey;
    }
}
