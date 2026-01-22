package com.navercorp.pinpoint.common.server.trace;

public interface ServerTraceId {
    byte[] getId();

    static ServerTraceId of(byte[] traceIdBytes, int offset, int length) {
        return PinpointServerTraceId.of(traceIdBytes, offset, length);
    }

    static ServerTraceId of(final String transactionId) {
        return PinpointServerTraceId.of(transactionId);
    }
}
