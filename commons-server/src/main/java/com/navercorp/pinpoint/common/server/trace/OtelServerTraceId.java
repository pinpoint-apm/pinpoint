package com.navercorp.pinpoint.common.server.trace;


import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.util.Base16Utils;

import java.util.Arrays;

public class OtelServerTraceId implements ServerTraceId {

    public static OtelServerTraceId of(byte[] traceIdBytes, int offset, int length) {
        return new OtelServerTraceId(Arrays.copyOfRange(traceIdBytes, offset, offset + length));
    }

    public static OtelServerTraceId of(Buffer buffer) {
        return new OtelServerTraceId(buffer.readPadBytes(PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN));
    }

    public static OtelServerTraceId of(final String transactionId) {
        return new OtelServerTraceId(Base16Utils.decodeToBytes(transactionId));
    }

    private final byte[] traceId;

    public OtelServerTraceId(byte[] traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        if (traceId.length != PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN) {
            throw new IllegalArgumentException("invalid OtelServerTraceId bytes length:" + traceId.length);
        }
        this.traceId = traceId;
    }

    @Override
    public byte[] getId() {
        return traceId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        OtelServerTraceId that = (OtelServerTraceId) o;
        return Arrays.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(traceId);
    }

    @Override
    public String toString() {
        return Base16Utils.encodeToString(traceId);
    }
}
