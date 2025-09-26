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

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HostRowKeyEncoder;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

public class HostRowKeyEncoderV3 implements HostRowKeyEncoder {

    private final ByteHasher hasher;
    private final int applicationNameMaxLength;

    public HostRowKeyEncoderV3(ByteHasher hasher) {
        this(hasher, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
    }

    public HostRowKeyEncoderV3(ByteHasher hasher, int applicationNameMaxLength) {
        this.hasher = Objects.requireNonNull(hasher, "hasher");
        this.applicationNameMaxLength = applicationNameMaxLength;
    }

    public byte[] encodeRowKey(String parentApplicationName, int parentServiceType, int parentServiceUid, long timestamp) {

        // even if  a agentId be added for additional specifications, it may be safe to scan rows.
        // But is it needed to add parentAgentServiceType?
        int offset = hasher.getSaltKey().size();
        final int SIZE = offset + applicationNameMaxLength + BytesUtils.INT_BYTE_LENGTH + BytesUtils.INT_BYTE_LENGTH + BytesUtils.LONG_BYTE_LENGTH;

        byte[] rowKey = new byte[SIZE];

        final byte[] parentAppNameBytes = BytesUtils.toBytes(parentApplicationName);
        if (parentAppNameBytes.length > applicationNameMaxLength) {
            throw new IllegalArgumentException("Parent application name length exceed " + parentApplicationName);
        }
        BytesUtils.writeBytes(rowKey, offset, parentAppNameBytes);
        offset += applicationNameMaxLength;
        offset = ByteArrayUtils.writeInt(parentServiceType, rowKey, offset);
        offset = ByteArrayUtils.writeInt(parentServiceUid, rowKey, offset);
        long reverseTimestamp = LongInverter.invert(timestamp);
        ByteArrayUtils.writeLong(reverseTimestamp, rowKey, offset);
        return rowKey;
    }


}
