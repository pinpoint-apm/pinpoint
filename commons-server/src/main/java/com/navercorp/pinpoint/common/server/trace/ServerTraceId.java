package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.util.TransactionIdParser;

import java.util.Objects;

public interface ServerTraceId {
    byte[] getId();

    /**
     * Identifies whether a span originated from the OpenTelemetry collector path.
     * <p>
     * OTel spans always carry an {@link OtelServerTraceId} (16-byte W3C trace_id), while
     * agent-instrumented spans carry a {@link PinpointServerTraceId}. Detecting OTel via
     * traceId type is independent of ServiceType, so it stays correct as new OTel-side
     * ServiceTypes (e.g. queue/messaging kinds) are added without updating any allowlist.
     */
    static boolean isOpenTelemetry(ServerTraceId traceId) {
        return traceId instanceof OtelServerTraceId;
    }

    static byte[] encodeApplicationTraceIndexQualifier(ServerTraceId serverTraceId) {
        if (serverTraceId instanceof PinpointServerTraceId) {
            return serverTraceId.getId();
        } else if (serverTraceId instanceof OtelServerTraceId) {
            final byte[] bytes = new byte[PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN + 1];
            // add prefix byte
            bytes[0] = OtelServerTraceId.PREFIX_BYTE_APPLICATION_TRACE_INDEX;
            System.arraycopy(serverTraceId.getId(), 0, bytes, 1, PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN);
            return bytes;
        } else {
            throw new IllegalStateException("unsupported ServerTraceId=" + serverTraceId);
        }
    }

    static ServerTraceId decodeApplicationTraceIndexQualifier(byte[] traceIdBytes, int offset, int length) {
        if (length == PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN + 1) {
            if (traceIdBytes[offset] == OtelServerTraceId.PREFIX_BYTE_APPLICATION_TRACE_INDEX) {
                // skip prefix byte
                return OtelServerTraceId.of(traceIdBytes, offset + 1, PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN);
            }
        }

        return PinpointServerTraceId.of(traceIdBytes, offset, length);
    }

    static void encodeServerTraceId(Buffer buffer, ServerTraceId serverTraceId) {
        if (serverTraceId instanceof PinpointServerTraceId pinpointServerTraceId) {
            buffer.putByte(PinpointServerTraceId.PREFIX_BYTE_SERVER_TRACE_ID);
            TransactionIdParser.writeTransactionIdV1(buffer, pinpointServerTraceId);
        } else if (serverTraceId instanceof OtelServerTraceId otelServerTraceId) {
            buffer.putByte(OtelServerTraceId.PREFIX_BYTE_SERVER_TRACE_ID);
            buffer.putBytes(otelServerTraceId.getId());
        } else {
            throw new IllegalArgumentException("unknown ServerTraceId=" + serverTraceId);
        }
    }

    static ServerTraceId decodeServerTraceId(Buffer buffer) {
        byte type = buffer.readByte();
        if (type == PinpointServerTraceId.PREFIX_BYTE_SERVER_TRACE_ID) {
            return PinpointServerTraceId.of(buffer);
        } else if (type == OtelServerTraceId.PREFIX_BYTE_SERVER_TRACE_ID) {
            return OtelServerTraceId.of(buffer);
        } else {
            throw new IllegalArgumentException("unknown ServerTraceId type=" + type);
        }
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
