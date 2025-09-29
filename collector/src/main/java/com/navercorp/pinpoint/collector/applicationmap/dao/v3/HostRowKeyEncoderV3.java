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
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;

import java.util.Objects;

public class HostRowKeyEncoderV3 implements HostRowKeyEncoder {

    private final ByteHasher hasher;

    public HostRowKeyEncoderV3(ByteHasher hasher) {
        this.hasher = Objects.requireNonNull(hasher, "hasher");
    }

    public byte[] encodeRowKey(String parentApplicationName, int parentServiceType, int parentServiceUid, long timestamp) {
        byte[] rowkey = encodeRowKey0(parentApplicationName, parentServiceType, parentServiceUid, timestamp);
        return hasher.writeSaltKey(rowkey);
    }

    byte[] encodeRowKey0(String parentApplicationName, int parentServiceType, int parentServiceUid, long timestamp) {
        // even if  a agentId be added for additional specifications, it may be safe to scan rows.
        // But is it needed to add parentAgentServiceType?
//        int offset = hasher.getSaltKey().size();
//        final int SIZE = offset + applicationNameMaxLength + BytesUtils.INT_BYTE_LENGTH + BytesUtils.INT_BYTE_LENGTH + BytesUtils.LONG_BYTE_LENGTH;
//
//        byte[] rowKey = new byte[SIZE];
//
//        final byte[] parentAppNameBytes = BytesUtils.toBytes(parentApplicationName);
//        if (parentAppNameBytes.length > applicationNameMaxLength) {
//            throw new IllegalArgumentException("Parent application name length exceed " + parentApplicationName);
//        }
//        BytesUtils.writeBytes(rowKey, offset, parentAppNameBytes);
//        offset += applicationNameMaxLength;
//        offset = ByteArrayUtils.writeInt(parentServiceType, rowKey, offset);
//        offset = ByteArrayUtils.writeInt(parentServiceUid, rowKey, offset);
//        long reverseTimestamp = LongInverter.invert(timestamp);
//        ByteArrayUtils.writeLong(reverseTimestamp, rowKey, offset);
//        return rowKey;

        return UidLinkRowKey.makeRowKey(hasher.getSaltKey().size(),
                parentServiceUid, parentApplicationName, parentServiceType, timestamp);
    }


}
