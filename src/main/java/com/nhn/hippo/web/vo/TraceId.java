package com.nhn.hippo.web.vo;

import com.profiler.common.util.BytesUtils;

import java.util.UUID;

public class TraceId {

    private final byte[] id;
    private final long most;
    private final long least;

    public TraceId(byte[] traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId");
        }
        if (traceId.length < 16) {
            throw new IllegalArgumentException("invalid traceId");
        }
        this.id = traceId;
        this.most = BytesUtils.bytesToFirstLong(id);
        this.least = BytesUtils.bytesToSecondLong(id);
    }
    
	public TraceId(UUID uuid) {
		this.most = uuid.getMostSignificantBits();
		this.least = uuid.getLeastSignificantBits();
		this.id = BytesUtils.longLongToBytes(most, least);
	}

    public byte[] getBytes() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TraceId traceId = (TraceId) o;

        if (least != traceId.least) return false;
        if (most != traceId.most) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (most ^ (most >>> 32));
        result = 31 * result + (int) (least ^ (least >>> 32));
        return result;
    }

    @Override
    public String toString() {
        UUID uuid = new UUID(most, least);
        return "TraceId [" + uuid + "]";
    }
}
