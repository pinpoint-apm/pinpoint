package com.navercorp.pinpoint.common.server.uid;

import java.util.Objects;

public class HbaseCellData {

    private final byte[] rowKey;
    private final Object value;
    long timestamp;

    public HbaseCellData(byte[] rowKey, Object valueObject, long timestamp) {
        this.rowKey = Objects.requireNonNull(rowKey, "rowKey");
        this.value = valueObject;
        this.timestamp = timestamp;
    }

    public byte[] getRowKey() {
        return rowKey;
    }

    public Object getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
