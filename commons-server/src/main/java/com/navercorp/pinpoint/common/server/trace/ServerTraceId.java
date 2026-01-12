package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.PinpointConstants;

import java.util.Objects;

public interface ServerTraceId {
    byte[] getId();

    static ServerTraceId of(byte[] traceIdBytes, int offset, int length) {
        if (length == PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN) {
            return OtelServerTraceId.of(traceIdBytes, offset, length);
        }

        return PinpointServerTraceId.of(traceIdBytes, offset, length);
    }

    static ServerTraceId of(final String transactionId) {
        Objects.requireNonNull(transactionId, "transactionId");
        if (transactionId.length() == PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN * 2) {
            if (transactionId.indexOf(PinpointServerTraceId.DELIMITER) == -1) {
                return OtelServerTraceId.of(transactionId);
            }
        }

        return PinpointServerTraceId.of(transactionId);
    }
}
