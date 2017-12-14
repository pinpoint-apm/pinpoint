/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import static com.navercorp.pinpoint.common.PinpointConstants.AGENT_NAME_MAX_LEN;
import static com.navercorp.pinpoint.common.util.BytesUtils.INT_BYTE_LENGTH;
import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;


/**
 * @author emeroad
 */
public final class RowKeyUtils {
    private RowKeyUtils() {
    }

    public static byte[] concatFixedByteAndLong(byte[] fixedBytes, int maxFixedLength, long l) {
        if (fixedBytes == null) {
            throw new NullPointerException("fixedBytes must not null");
        }
        if (fixedBytes.length > maxFixedLength) {
            throw new IndexOutOfBoundsException("fixedBytes.length too big. length:" + fixedBytes.length);
        }
        byte[] rowKey = new byte[maxFixedLength + LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, 0, fixedBytes);
        BytesUtils.writeLong(l, rowKey, maxFixedLength);
        return rowKey;
    }


    public static byte[] getMetaInfoRowKey(String agentId, long agentStartTime, int keyCode) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        final byte[] agentBytes = BytesUtils.toBytes(agentId);
        if (agentBytes.length > AGENT_NAME_MAX_LEN) {
            throw new IndexOutOfBoundsException("agent.length too big. agent:" + agentId + " length:" + agentId.length());
        }

        final byte[] buffer = new byte[AGENT_NAME_MAX_LEN + LONG_BYTE_LENGTH + INT_BYTE_LENGTH];
        BytesUtils.writeBytes(buffer, 0, agentBytes);

        long reverseCurrentTimeMillis = TimeUtils.reverseTimeMillis(agentStartTime);
        BytesUtils.writeLong(reverseCurrentTimeMillis, buffer, AGENT_NAME_MAX_LEN);

        BytesUtils.writeInt(keyCode, buffer, AGENT_NAME_MAX_LEN + LONG_BYTE_LENGTH);
        return buffer;
    }


}
