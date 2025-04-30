package com.navercorp.pinpoint.common.server.uid;

import java.util.Objects;

public class HbaseCellData {

    private final byte[] rowKey;
    long timestamp;
    private final Object value;

    public HbaseCellData(byte[] rowKey, long timestamp, Object valueObject) {
        this.rowKey = Objects.requireNonNull(rowKey, "rowKey");
        this.timestamp = timestamp;
        this.value = valueObject;
    }

    public byte[] getRowKey() {
        return rowKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Object getValue() {
        return value;
    }
}
