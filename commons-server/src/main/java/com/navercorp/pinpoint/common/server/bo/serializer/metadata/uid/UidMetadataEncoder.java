package com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Objects;

import static com.navercorp.pinpoint.common.PinpointConstants.AGENT_ID_MAX_LEN;
import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

public class UidMetadataEncoder implements RowKeyEncoder<UidMetaDataRowKey> {

    @Override
    public byte[] encodeRowKey(UidMetaDataRowKey metaDataRowKey) {
        Objects.requireNonNull(metaDataRowKey, "metaDataRowKey");

        return readMetaDataRowKey(metaDataRowKey.getAgentId(),
                metaDataRowKey.getAgentStartTime(),
                metaDataRowKey.getUid());
    }

    public static byte[] readMetaDataRowKey(String agentId, long agentStartTime, byte[] keyCode) {
        Objects.requireNonNull(agentId, "agentId");

        final byte[] agentBytes = BytesUtils.toBytes(agentId);
        if (agentBytes.length > PinpointConstants.AGENT_ID_MAX_LEN) {
            throw new IndexOutOfBoundsException("agent.length too big. agent:" + agentId + " length:" + agentId.length());
        }

        final byte[] buffer = new byte[AGENT_ID_MAX_LEN + LONG_BYTE_LENGTH + keyCode.length];
        BytesUtils.writeBytes(buffer, 0, agentBytes);

        long reverseCurrentTimeMillis = TimeUtils.reverseTimeMillis(agentStartTime);
        BytesUtils.writeLong(reverseCurrentTimeMillis, buffer, AGENT_ID_MAX_LEN);

        BytesUtils.writeBytes(buffer, AGENT_ID_MAX_LEN + LONG_BYTE_LENGTH, keyCode);
        return buffer;
    }
}
