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

package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

import static com.navercorp.pinpoint.common.PinpointConstants.AGENT_ID_MAX_LEN;
import static com.navercorp.pinpoint.common.util.BytesUtils.INT_BYTE_LENGTH;
import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

public class MetadataEncoder implements RowKeyEncoder<MetaDataRowKey> {

    private final ByteHasher hasher;

    public MetadataEncoder(RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
        this.hasher = rowKeyDistributorByHashPrefix.getByteHasher();
    }

    @Override
    public byte[] encodeRowKey(MetaDataRowKey metadataRowKey) {
        Objects.requireNonNull(metadataRowKey, "metadataRowKey");

        return encodeRowKey(hasher.getSaltKey().size(), metadataRowKey);
    }

    @Override
    public byte[] encodeRowKey(int saltKeySize, MetaDataRowKey metadataRowKey) {

        byte[] rowKey = readMetaDataRowKey(saltKeySize, metadataRowKey.getAgentId(),
                metadataRowKey.getAgentStartTime(), metadataRowKey.getId());
        if (saltKeySize == 0) {
            return rowKey;
        }
        return hasher.writeSaltKey(rowKey);
    }

    public static byte[] readMetaDataRowKey(int saltKeySize, String agentId, long agentStartTime, int keyCode) {
        Objects.requireNonNull(agentId, "agentId");

        final byte[] agentBytes = BytesUtils.toBytes(agentId);
        if (agentBytes.length > AGENT_ID_MAX_LEN) {
            throw new IndexOutOfBoundsException("agent.length too big. agent:" + agentId + " length:" + agentId.length());
        }
        int offset = saltKeySize + AGENT_ID_MAX_LEN;
        final byte[] buffer = new byte[offset + LONG_BYTE_LENGTH + INT_BYTE_LENGTH];
        BytesUtils.writeBytes(buffer, saltKeySize, agentBytes);

        long reverseCurrentTimeMillis = LongInverter.invert(agentStartTime);
        offset = ByteArrayUtils.writeLong(reverseCurrentTimeMillis, buffer, offset);
        ByteArrayUtils.writeInt(keyCode, buffer, offset);
        return buffer;
    }


}
