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

package com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Arrays;

public class UidMetadataDecoder implements RowKeyDecoder<UidMetaDataRowKey> {

    private final int saltKeySize;

    public UidMetadataDecoder() {
         this(ByteSaltKey.SALT.size());
    }

    public UidMetadataDecoder(int saltKeySize) {
        this.saltKeySize = saltKeySize;
    }

    @Override
    public UidMetaDataRowKey decodeRowKey(byte[] rowKey) {
        final String agentId = readAgentId(rowKey, saltKeySize);
        final long agentStartTime = readAgentStartTime(rowKey, PinpointConstants.AGENT_ID_MAX_LEN + saltKeySize);
        final byte[] uid = readUid(rowKey, PinpointConstants.AGENT_ID_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH + saltKeySize);

        return new DefaultUidMetaDataRowKey(agentId, agentStartTime, uid);
    }

    private String readAgentId(byte[] rowKey, int offset) {
        return BytesUtils.toStringAndRightTrim(rowKey, offset, PinpointConstants.AGENT_ID_MAX_LEN);
    }

    private long readAgentStartTime(byte[] rowKey, int offset) {
        return LongInverter.restore(ByteArrayUtils.bytesToLong(rowKey, offset));
    }

    private byte[] readUid(byte[] rowKey, int offset) {
        return Arrays.copyOfRange(rowKey, offset, rowKey.length);
    }
}
