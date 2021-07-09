package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Objects;

import static com.navercorp.pinpoint.common.PinpointConstants.AGENT_ID_MAX_LEN;
import static com.navercorp.pinpoint.common.util.BytesUtils.INT_BYTE_LENGTH;
import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

public class MetadataEncoder implements RowKeyEncoder<MetaDataRowKey> {

    @Override
    public byte[] encodeRowKey(MetaDataRowKey metadataRowKey) {
        Objects.requireNonNull(metadataRowKey, "metadataRowKey");

        return readMetaDataRowKey(metadataRowKey.getAgentId(),
                metadataRowKey.getAgentStartTime(), metadataRowKey.getId());
    }

    public static byte[] readMetaDataRowKey(String agentId, long agentStartTime, int keyCode) {
        Objects.requireNonNull(agentId, "agentId");

        final byte[] agentBytes = BytesUtils.toBytes(agentId);
        if (agentBytes.length > AGENT_ID_MAX_LEN) {
            throw new IndexOutOfBoundsException("agent.length too big. agent:" + agentId + " length:" + agentId.length());
        }

        final byte[] buffer = new byte[AGENT_ID_MAX_LEN + LONG_BYTE_LENGTH + INT_BYTE_LENGTH];
        BytesUtils.writeBytes(buffer, 0, agentBytes);

        long reverseCurrentTimeMillis = TimeUtils.reverseTimeMillis(agentStartTime);
        BytesUtils.writeLong(reverseCurrentTimeMillis, buffer, AGENT_ID_MAX_LEN);

        BytesUtils.writeInt(keyCode, buffer, AGENT_ID_MAX_LEN + LONG_BYTE_LENGTH);
        return buffer;
    }


}
