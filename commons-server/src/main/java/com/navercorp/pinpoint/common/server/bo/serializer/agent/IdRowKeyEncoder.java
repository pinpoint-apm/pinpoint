package com.navercorp.pinpoint.common.server.bo.serializer.agent;

import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

public class IdRowKeyEncoder {

    private final int max;

    public IdRowKeyEncoder(int max) {
        this.max = max;
    }

    public byte[] encodeRowKey(String id, long timestamp) {
        byte[] idKey = BytesUtils.toBytes(id);
        long reverseTimestamp = TimeUtils.reverseTimeMillis(timestamp);
        return RowKeyUtils.concatFixedByteAndLong(idKey, max, reverseTimestamp);
    }
}
