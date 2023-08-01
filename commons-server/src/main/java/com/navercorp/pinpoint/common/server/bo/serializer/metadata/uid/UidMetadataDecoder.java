package com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Arrays;

public class UidMetadataDecoder implements RowKeyDecoder<UidMetaDataRowKey> {

    @Override
    public UidMetaDataRowKey decodeRowKey(byte[] rowKey) {
        final String agentId = readAgentId(rowKey);
        final long agentStartTime = readAgentStartTime(rowKey);
        final byte[] uid = readUid(rowKey);

        return new DefaultUidMetaDataRowKey(agentId, agentStartTime, uid);
    }

    private String readAgentId(byte[] rowKey) {
        return BytesUtils.toStringAndRightTrim(rowKey, 0, PinpointConstants.AGENT_ID_MAX_LEN);
    }

    private long readAgentStartTime(byte[] rowKey) {
        return TimeUtils.recoveryTimeMillis(BytesUtils.bytesToLong(rowKey, PinpointConstants.AGENT_ID_MAX_LEN));
    }

    private byte[] readUid(byte[] rowKey) {
        return Arrays.copyOfRange(rowKey, PinpointConstants.AGENT_ID_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH, rowKey.length);
    }
}
