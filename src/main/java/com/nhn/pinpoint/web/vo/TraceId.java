package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.TraceIdUtils;

public class TraceId {
    public static final String AGENT_DELIMITER = "=";
    protected final String agentId;
    protected final long agentStartTime;
    protected final long transactionId;

    public TraceId(byte[] traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        if (traceId.length < BytesUtils.LONG_LONG_BYTE_LENGTH + HBaseTables.AGENT_NAME_MAX_LEN) {
            throw new IllegalArgumentException("invalid traceId");
        }

        this.agentId = BytesUtils.toStringAndRightTrim(traceId, 0, HBaseTables.AGENT_NAME_MAX_LEN);
        this.agentStartTime = BytesUtils.bytesToLong(traceId, HBaseTables.AGENT_NAME_MAX_LEN);
        this.transactionId = BytesUtils.bytesToLong(traceId, BytesUtils.LONG_BYTE_LENGTH + HBaseTables.AGENT_NAME_MAX_LEN);

    }

    public TraceId(String agentId, long agentStartTime, long transactionId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.transactionId = transactionId;
        this.agentStartTime = agentStartTime;
    }

    public TraceId(String traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }

        final int agentIdIndex = traceId.indexOf(AGENT_DELIMITER);
        if (agentIdIndex == -1) {
            throw new IllegalArgumentException("traceId delimiter not found:" + traceId);
        }
        this.agentId = traceId.substring(0, agentIdIndex);
        String ids = traceId.substring(agentIdIndex + 1, traceId.length());
        String[] strings = TraceIdUtils.parseTraceId(ids);
        this.agentStartTime = TraceIdUtils.parseMostId(strings);
        this.transactionId = TraceIdUtils.parseLeastId(strings);

    }

    public byte[] getBytes() {
        return BytesUtils.stringLongLongToBytes(agentId, HBaseTables.AGENT_NAME_MAX_LEN, agentStartTime, transactionId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TraceId traceId = (TraceId) o;

        if (agentStartTime != traceId.agentStartTime) return false;
        if (transactionId != traceId.transactionId) return false;
        if (!agentId.equals(traceId.agentId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = agentId.hashCode();
        result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
        result = 31 * result + (int) (transactionId ^ (transactionId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        String traceId = TraceIdUtils.formatString(agentId, agentStartTime, transactionId);
        return "TraceId [" + traceId + "]";
    }

    public String getFormatString() {
        return TraceIdUtils.formatString(agentId, agentStartTime, transactionId);
    }

}
