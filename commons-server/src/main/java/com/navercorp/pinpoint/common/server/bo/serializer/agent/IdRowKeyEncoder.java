package com.navercorp.pinpoint.common.server.bo.serializer.agent;

import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.common.util.UuidUtils;

import java.util.UUID;

public class IdRowKeyEncoder {

    private final int max;

    public IdRowKeyEncoder(int max) {
        this.max = max;
    }

    public byte[] encodeRowKey(String idKey, long timestamp) {
        return this.encodeRowKey(BytesUtils.toBytes(idKey), timestamp);
    }

    public byte[] encodeRowKey(UUID idKey, long timestamp) {
        return this.encodeRowKey(UuidUtils.toBytes(idKey), timestamp);
    }

    private byte[] encodeRowKey(byte[] idKey, long timestamp) {
        long reverseTimestamp = TimeUtils.reverseTimeMillis(timestamp);
        return RowKeyUtils.concatFixedByteAndLong(idKey, max, reverseTimestamp);
    }

}
