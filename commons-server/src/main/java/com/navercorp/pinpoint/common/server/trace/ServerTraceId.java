package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.server.util.TransactionIdParser;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Arrays;
import java.util.Objects;

public interface ServerTraceId {
    byte[] getId();

    static byte[] encodeTraceRowKey(int saltKeySize, ServerTraceId serverTraceId) {
        if (serverTraceId instanceof PinpointServerTraceId pinpointServerTraceId) {
            final String agentId = pinpointServerTraceId.getAgentId();
            return RowKeyUtils.stringLongLongToBytes(saltKeySize, agentId, PinpointConstants.AGENT_ID_MAX_LEN, pinpointServerTraceId.getAgentStartTime(), pinpointServerTraceId.getTransactionSequence());
        } else if (serverTraceId instanceof OtelServerTraceId otelServerTraceId) {
            byte[] otelTraceIdBytes = otelServerTraceId.getId();
            byte[] rowKey = new byte[PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN + 1];
            System.arraycopy(otelTraceIdBytes, 0, rowKey, 1, PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN);
            return rowKey;
        } else {
            throw new IllegalStateException("unsupported ServerTraceId=" + serverTraceId);
        }
    }

    static ServerTraceId decodeTraceRowKey(byte[] rowKey, int saltKeySize) {
        if (rowKey.length == saltKeySize + PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN) {
            byte[] otelTraceId = Arrays.copyOfRange(rowKey, saltKeySize, saltKeySize + PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN);
            return new OtelServerTraceId(otelTraceId);
        }

        // PinpointServerTraceId
        String agentId = BytesUtils.toStringAndRightTrim(rowKey, saltKeySize, PinpointConstants.AGENT_ID_MAX_LEN);
        long agentStartTime = ByteArrayUtils.bytesToLong(rowKey, saltKeySize + PinpointConstants.AGENT_ID_MAX_LEN);
        long transactionSequence = ByteArrayUtils.bytesToLong(rowKey, saltKeySize + BytesUtils.LONG_BYTE_LENGTH + PinpointConstants.AGENT_ID_MAX_LEN);
        return new PinpointServerTraceId(agentId, agentStartTime, transactionSequence);
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
