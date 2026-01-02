package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.util.TransactionIdParser;
import com.navercorp.pinpoint.common.util.IdValidateUtils;

import java.util.Objects;

public class PinpointServerTraceId implements ServerTraceId {

    public static final char DELIMITER = '^';
    public static final int DELIMITER_LENGTH = 1;

    private final String agentId;
    private final long agentStartTime;
    private final long transactionSequence;


    public static ServerTraceId of(byte[] traceIdBytes, int offset, int length) {
        Buffer buffer = new OffsetFixedBuffer(traceIdBytes, offset, length);
        return of(buffer);
    }

    public static ServerTraceId of(Buffer buffer) {
        String agentId = buffer.readPrefixedString();
        long agentStartTime = buffer.readSVLong();
        long transactionSequence = buffer.readVLong();
        return new PinpointServerTraceId(agentId, agentStartTime, transactionSequence);
    }

    public static ServerTraceId of(final String transactionId) {
        Objects.requireNonNull(transactionId, "transactionId");

        final int agentIdIndex = indexOf(transactionId, 0);
        if (agentIdIndex == -1) {
            throw new IllegalArgumentException("agentIndex not found:" + transactionId);
        }
        if (!IdValidateUtils.checkId(transactionId, 0, agentIdIndex)) {
            throw new IllegalArgumentException("invalid transactionId:" + transactionId);
        }
        final String agentId = transactionId.substring(0, agentIdIndex);

        final int agentStartTimeIndex = indexOf(transactionId, agentIdIndex + DELIMITER_LENGTH);
        if (agentStartTimeIndex == -1) {
            throw new IllegalArgumentException("agentStartTimeIndex not found:" + transactionId);
        }
        final long agentStartTime = parseLong(transactionId, agentIdIndex + DELIMITER_LENGTH, agentStartTimeIndex);

        int transactionSequenceIndex = indexOf(transactionId, agentStartTimeIndex + DELIMITER_LENGTH);
        if (transactionSequenceIndex == -1) {
            // next index may not exist since default value does not have a delimiter after transactionSequence.
            // may need fixing when id spec changes
            transactionSequenceIndex = transactionId.length();
        }
        final long transactionSequence = parseLong(transactionId, agentStartTimeIndex + DELIMITER_LENGTH, transactionSequenceIndex);
        return new PinpointServerTraceId(agentId, agentStartTime, transactionSequence);
    }

    private static int indexOf(String transactionId, int fromIndex) {
        return transactionId.indexOf(DELIMITER, fromIndex);
    }

    private static long parseLong(String traceId, int beginIndex, int endIndex) {
        try {
            return Long.parseLong(traceId, beginIndex, endIndex, 10);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parseLong Error. " + traceId);
        }
    }

    public PinpointServerTraceId(String agentId, long agentStartTime, long transactionSequence) {
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionSequence;
    }

    @Override
    public byte[] getId() {
        return TransactionIdParser.getVarTransactionId(agentId, agentStartTime, transactionSequence);
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getTransactionSequence() {
        return transactionSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        PinpointServerTraceId that = (PinpointServerTraceId) o;
        return agentStartTime == that.agentStartTime && transactionSequence == that.transactionSequence && Objects.equals(agentId, that.agentId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(agentId);
        result = 31 * result + Long.hashCode(agentStartTime);
        result = 31 * result + Long.hashCode(transactionSequence);
        return result;
    }

    @Override
    public String toString() {
        return TransactionIdUtils.formatString(agentId, agentStartTime, transactionSequence);
    }
}
