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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.common.util.BytesUtils;

public class MetadataDecoder implements RowKeyDecoder<MetaDataRowKey> {

    private final int saltKeySize;

    public MetadataDecoder() {
        this(ByteSaltKey.SALT.size());
    }

    public MetadataDecoder(int saltKeySize) {
        this.saltKeySize = saltKeySize;
    }

    @Override
    public MetaDataRowKey decodeRowKey(byte[] rowkey) {
        final String agentId = readAgentId(rowkey, saltKeySize);
        final long agentStartTime = readAgentStartTime(rowkey, PinpointConstants.AGENT_ID_MAX_LEN + saltKeySize);
        final int id = readId(rowkey, PinpointConstants.AGENT_ID_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH + saltKeySize);

        return new DefaultMetaDataRowKey(agentId, agentStartTime, id);
    }

    private String readAgentId(byte[] rowKey, int offset) {
        return BytesUtils.toStringAndRightTrim(rowKey, offset, PinpointConstants.AGENT_ID_MAX_LEN);
    }

    private long readAgentStartTime(byte[] rowKey, int offset) {
        return LongInverter.restore(ByteArrayUtils.bytesToLong(rowKey, offset));
    }

    private int readId(byte[] rowKey, int offset) {
        return ByteArrayUtils.bytesToInt(rowKey, offset);
    }
}
