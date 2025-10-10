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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;


/**
 * @author emeroad
 */
public final class RowKeyUtils {

    private static final int FUZZY_SLOT_SIZE = 1;

    private RowKeyUtils() {
    }

    public static byte[] agentIdAndTimestamp(String agentId, long timestamp) {
        return concatFixedByteAndLong(BytesUtils.toBytes(agentId), HbaseTableConstants.AGENT_ID_MAX_LEN, timestamp);
    }

    public static byte[] concatFixedByteAndLong(byte[] fixedBytes, int maxFixedLength, long l) {
        Objects.requireNonNull(fixedBytes, "fixedBytes");

        if (fixedBytes.length > maxFixedLength) {
            throw new IndexOutOfBoundsException("fixedBytes.length too big. length:" + fixedBytes.length);
        }
        byte[] rowKey = new byte[maxFixedLength + LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, 0, fixedBytes);
        ByteArrayUtils.writeLong(l, rowKey, maxFixedLength);
        return rowKey;
    }

    public static byte[] concatFixedByteAndLongFuzzySlot(byte[] fixedBytes, int maxFixedLength, long timestamp, byte fuzzySlotKey) {
        return concatFixedByteAndLongFuzzySlot(0, fixedBytes, maxFixedLength, timestamp, fuzzySlotKey);
    }

    public static byte[] concatFixedByteAndLongFuzzySlot(int prefix, byte[] fixedBytes, int maxFixedLength, long timestamp, byte fuzzySlotKey) {
        Objects.requireNonNull(fixedBytes, "fixedBytes");

        if (fixedBytes.length > maxFixedLength) {
            throw new IndexOutOfBoundsException("fixedBytes.length too big. length:" + fixedBytes.length);
        }
        int offset = prefix + maxFixedLength;
        byte[] rowKey = new byte[offset + LONG_BYTE_LENGTH + FUZZY_SLOT_SIZE];
        BytesUtils.writeBytes(rowKey, prefix, fixedBytes);
        ByteArrayUtils.writeLong(timestamp, rowKey, offset);
        rowKey[rowKey.length -1] = fuzzySlotKey;
        return rowKey;
    }

    public static byte[] concatApplicationAndLongFuzzySlot(int prefix, int serviceUid, String applicationName, long timestamp, byte fuzzySlotKey) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPadBytes(null, prefix);
        buffer.putInt(serviceUid);
        buffer.putPrefixedString(applicationName);
        buffer.putLong(timestamp);
        buffer.putByte(fuzzySlotKey);
        return buffer.getBuffer();
    }

    public static byte[] stringLongLongToBytes(final String string, final int maxStringSize, final long value1, final long value2) {
        return stringLongLongToBytes(0, string, maxStringSize, value1, value2);
    }

    public static byte[] stringLongLongToBytes(int prefix, final String string, final int maxStringSize, final long value1, final long value2) {
        if (string == null) {
            throw new NullPointerException("string");
        }
        if (maxStringSize < 0) {
            throw new StringIndexOutOfBoundsException(maxStringSize);
        }
        final byte[] stringBytes = BytesUtils.toBytes(string);
        if (stringBytes.length > maxStringSize) {
            throw new StringIndexOutOfBoundsException("string is max " + stringBytes.length + ", string='" + string + "'");
        }
        int offset = prefix + maxStringSize;
        final byte[] buffer = new byte[offset + BytesUtils.LONG_LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(buffer, prefix, stringBytes);
        offset = ByteArrayUtils.writeLong(value1, buffer, offset);
        ByteArrayUtils.writeLong(value2, buffer, offset);
        return buffer;
    }

}
