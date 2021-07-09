package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

public class MetadataDecoder implements RowKeyDecoder<MetaDataRowKey> {

    @Override
    public MetaDataRowKey decodeRowKey(byte[] rowkey) {
        final String agentId = readAgentId(rowkey);
        final long agentStartTime = readAgentStartTime(rowkey);
        final int id = readId(rowkey);

        return new DefaultMetaDataRowKey(agentId, agentStartTime, id);
    }

    private String readAgentId(byte[] rowKey) {
        return BytesUtils.safeTrim(BytesUtils.toString(rowKey, 0, PinpointConstants.AGENT_ID_MAX_LEN));
    }

    private long readAgentStartTime(byte[] rowKey) {
        return TimeUtils.recoveryTimeMillis(BytesUtils.bytesToLong(rowKey, PinpointConstants.AGENT_ID_MAX_LEN));
    }

    private int readId(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, PinpointConstants.AGENT_ID_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH);
    }
}
