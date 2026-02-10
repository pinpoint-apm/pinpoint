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
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;

public class AgentIdRowKeyUtils {

    private static final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    private AgentIdRowKeyUtils() {
    }

    public static byte[] createRowKey(int serviceUid, String applicationName) {
        Buffer buffer = new AutomaticBuffer(4 +
                254
        );
        buffer.putInt(serviceUid);
        buffer.putPadString(applicationName, 254);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(int serviceUid, String applicationName, int serviceTypeCode) {
        Buffer buffer = new AutomaticBuffer(4 +
                254 + 4
        );
        buffer.putInt(serviceUid);
        buffer.putPadString(applicationName, 254);
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(int serviceUid, String applicationName, int serviceTypeCode, String agentId) {
        Buffer buffer = new AutomaticBuffer(4 +
                254 + 4 +
                24
        );
        buffer.putInt(serviceUid);
        buffer.putPadString(applicationName, 254);
        buffer.putInt(serviceTypeCode);
        buffer.putPadString(agentId, 24);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime) {
        Buffer buffer = new AutomaticBuffer(4 +
                254 + 4 +
                24 +
                8
        );
        buffer.putInt(serviceUid);
        buffer.putPadString(applicationName, 254);
        buffer.putInt(serviceTypeCode);
        buffer.putPadString(agentId, 24);
        buffer.putLong(LongInverter.invert(agentStartTime));
        return buffer.getBuffer();
    }

    public static int toApplicationNameHash(String applicationName) {
        return hashFunction.hashUnencodedChars(applicationName).asInt();
    }
}
