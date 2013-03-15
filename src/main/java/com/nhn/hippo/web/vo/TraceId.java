package com.nhn.hippo.web.vo;

import com.profiler.common.util.BytesUtils;
import com.profiler.common.util.TraceIdUtils;

public class TraceId {

    private final long most;
    private final long least;

    public TraceId(byte[] traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        if (traceId.length < 16) {
            throw new IllegalArgumentException("invalid traceId");
        }
        this.most = BytesUtils.bytesToFirstLong(traceId);
        this.least = BytesUtils.bytesToSecondLong(traceId);
    }

    public TraceId(long most, long least) {
        this.least = least;
        this.most = most;
    }

    public TraceId(String traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        String[] parsedTraceId = TraceIdUtils.parseTraceId(traceId);
        this.most = TraceIdUtils.parseMostId(parsedTraceId);
        this.least = TraceIdUtils.parseLeastId(parsedTraceId);
    }

    public byte[] getBytes() {
        return BytesUtils.longLongToBytes(most, least);
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
        String traceId = TraceIdUtils.formatString(most, least);
        return "TraceId [" + traceId + "]";
    }

    public String getFormatString() {
        return TraceIdUtils.formatString(most, least);
    }

    public String getFormatLong() {
        return this.most + ":" + this.least;
    }
}
