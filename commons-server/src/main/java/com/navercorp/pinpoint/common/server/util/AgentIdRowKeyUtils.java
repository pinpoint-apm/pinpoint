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

package com.navercorp.pinpoint.common.server.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Arrays;
import java.util.function.Predicate;

public class AgentIdRowKeyUtils {

    private static final HashFunction hashFunction = Hashing.murmur3_32_fixed();
    private static final int applicationNameOffset = 4 + 4 + 4 + PinpointConstants.AGENT_ID_MAX_LEN + 8;

    private AgentIdRowKeyUtils() {
    }

    public static byte[] createPrefix(int serviceUid, String applicationName) {
        Buffer buffer = new AutomaticBuffer(4 +
                4
        );
        buffer.putInt(serviceUid);
        buffer.putInt(toApplicationNameHash(applicationName));
        return buffer.getBuffer();
    }

    public static byte[] createPrefix(int serviceUid, String applicationName, int serviceTypeCode) {
        Buffer buffer = new AutomaticBuffer(4 +
                4 + 4
        );
        buffer.putInt(serviceUid);
        buffer.putInt(toApplicationNameHash(applicationName));
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }

    public static byte[] createPrefix(int serviceUid, String applicationName, int serviceTypeCode, String agentId) {
        Buffer buffer = new AutomaticBuffer(4 +
                4 + 4 +
                PinpointConstants.AGENT_ID_MAX_LEN
        );
        buffer.putInt(serviceUid);
        buffer.putInt(toApplicationNameHash(applicationName));
        buffer.putInt(serviceTypeCode);
        buffer.putPadString(agentId, PinpointConstants.AGENT_ID_MAX_LEN);
        return buffer.getBuffer();
    }

    public static byte[] createRow(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime) {
        Buffer buffer = new AutomaticBuffer(4 +
                4 + 4 +
                PinpointConstants.AGENT_ID_MAX_LEN +
                8 +
                BytesUtils.computeVar32StringSize(applicationName)
        );
        buffer.putInt(serviceUid);
        buffer.putInt(toApplicationNameHash(applicationName));
        buffer.putInt(serviceTypeCode);
        buffer.putPadString(agentId, PinpointConstants.AGENT_ID_MAX_LEN);
        buffer.putLong(LongInverter.invert(agentStartTime));
        buffer.putPrefixedString(applicationName);
        return buffer.getBuffer();
    }

    public static int toApplicationNameHash(String applicationName) {
        return hashFunction.hashUnencodedChars(applicationName).asInt();
    }

    public static int extractServiceUid(byte[] row) {
        return ByteArrayUtils.bytesToInt(row, 0);
    }

    public static String extractApplicationName(byte[] row) {
        Buffer buffer = new OffsetFixedBuffer(row);
        buffer.setOffset(applicationNameOffset);
        return buffer.readPrefixedString();
    }

    public static int extractServiceTypeCode(byte[] row) {
        return ByteArrayUtils.bytesToInt(row, 4 +
                4);
    }

    public static String extractAgentId(byte[] row) {
        return BytesUtils.toStringAndRightTrim(row, 4 +
                4 + 4, PinpointConstants.AGENT_ID_MAX_LEN);
    }

    public static long extractAgentStartTime(byte[] row) {
        return LongInverter.restore(
                ByteArrayUtils.bytesToLong(row, 4 +
                        4 + 4 + PinpointConstants.AGENT_ID_MAX_LEN)
        );
    }

    public static Predicate<byte[]> createApplicationNamePredicate(String applicationName) {
        Buffer buffer = new FixedBuffer(BytesUtils.computeVar32StringSize(applicationName));
        buffer.putPrefixedString(applicationName);
        byte[] prefixedApplicationName = buffer.getBuffer();

        return row -> {
            if (row.length < applicationNameOffset) {
                return false;
            } else {
                return Arrays.equals(row, applicationNameOffset, row.length,
                        prefixedApplicationName, 0, prefixedApplicationName.length);
            }
        };
    }
}
