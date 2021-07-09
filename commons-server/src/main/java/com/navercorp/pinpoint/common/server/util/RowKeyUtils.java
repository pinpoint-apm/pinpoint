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

import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

import static com.navercorp.pinpoint.common.PinpointConstants.AGENT_ID_MAX_LEN;
import static com.navercorp.pinpoint.common.util.BytesUtils.INT_BYTE_LENGTH;
import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;


/**
 * @author emeroad
 */
public final class RowKeyUtils {
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
        BytesUtils.writeLong(l, rowKey, maxFixedLength);
        return rowKey;
    }


}
