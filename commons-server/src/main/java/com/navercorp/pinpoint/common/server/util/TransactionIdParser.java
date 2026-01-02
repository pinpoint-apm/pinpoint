package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.util.IdValidateUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class TransactionIdParser {

    public static TransactionId parse(final byte[] transactionId, String defaultAgentId) {
        Objects.requireNonNull(transactionId, "transactionId");

        final Buffer buffer = new FixedBuffer(transactionId);
        final byte version = buffer.readByte();
        if (version != TransactionIdUtils.VERSION) {
            throw new IllegalArgumentException("invalid Version");
        }

        String agentId = buffer.readPrefixedString();
        agentId = Objects.toString(agentId, defaultAgentId);
        if (!IdValidateUtils.validateId(agentId)) {
            throw new IllegalArgumentException("invalid transactionId:" + Arrays.toString(transactionId));
        }

        final long agentStartTime = buffer.readVLong();
        final long transactionSequence = buffer.readVLong();

        return TransactionId.of(agentId, agentStartTime,transactionSequence);
    }

    /**
     * deserializer ref : TransactionIdMapper.parseVarTransactionId
     */
    public static byte[] getVarTransactionId(TransactionId transactionId, Supplier<String> agentIdSupplier) {

        Objects.requireNonNull(transactionId, "span");
        String agentId = transactionId.getAgentId();
        if (agentId == null) {
            agentId = agentIdSupplier.get();
        }
        return getVarTransactionId(agentId, transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
    }

    public static byte[] getVarTransactionId(ServerTraceId serverTraceId, Supplier<String> agentIdSupplier) {
        Objects.requireNonNull(serverTraceId, "serverTraceId");

        if (!(serverTraceId instanceof PinpointServerTraceId pinpointServerTraceId)) {
            throw new IllegalArgumentException("unsupported ServerTraceId type:" + serverTraceId.getClass().getName());
        }
        String agentId = pinpointServerTraceId.getAgentId();
        if (agentId == null) {
            agentId = agentIdSupplier.get();
        }
        return getVarTransactionId(agentId, pinpointServerTraceId.getAgentStartTime(), pinpointServerTraceId.getTransactionSequence());
    }

    public static byte[] getVarTransactionId(String agentId, long agentStartTime, long sequence) {
        Objects.requireNonNull(agentId, "agentId");

        final Buffer buffer = new AutomaticBuffer(32);
        buffer.putPrefixedString(agentId);
        buffer.putSVLong(agentStartTime);
        buffer.putVLong(sequence);
        return buffer.getBuffer();
    }

    public static TransactionId parseVarTransactionId(byte[] bytes, int offset, int length) {
        Objects.requireNonNull(bytes, "bytes");

        final Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
        return readTransactionIdV1(buffer);
    }

    public static void writeTransactionIdV1(Buffer buffer, TransactionId transactionId) {
        buffer.putPrefixedString(transactionId.getAgentId());
        buffer.putSVLong(transactionId.getAgentStartTime());
        buffer.putVLong(transactionId.getTransactionSequence());
    }

    public static void writeTransactionIdV1(Buffer buffer, ServerTraceId transactionId) {
        if (transactionId instanceof PinpointServerTraceId pinpointServerTraceId) {
            buffer.putPrefixedString(pinpointServerTraceId.getAgentId());
            buffer.putSVLong(pinpointServerTraceId.getAgentStartTime());
            buffer.putVLong(pinpointServerTraceId.getTransactionSequence());
        } else {
            throw new IllegalArgumentException("unsupported ServerTraceId type:" + transactionId.getClass().getName());
        }
    }

    public static TransactionId readTransactionIdV1(Buffer buffer) {
        String agentId = buffer.readPrefixedString();
        long agentStartTime = buffer.readSVLong();
        long transactionSequence = buffer.readVLong();
        return TransactionId.of(agentId, agentStartTime, transactionSequence);
    }
}
