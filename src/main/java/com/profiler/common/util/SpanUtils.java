package com.profiler.common.util;

import com.profiler.common.dto.thrift.Span;
import org.apache.hadoop.hbase.util.Bytes;

public class SpanUtils {

    public static final int AGENT_NAME_LIMIT = 24;


    public static byte[] getTraceIndexRowKey(Span span) {
        return getTraceIndexRowKey(span.getAgentId(), span.getTimestamp());
    }

    public static byte[] getTraceIndexRowKey(byte[] agentId, long time) {
        return RowKeyUtils.concatFixedByteAndLong(agentId, AGENT_NAME_LIMIT, time);
    }

    public static byte[] getTraceIndexRowKey(String agentId, long time) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId must not null");
        }
        byte[] bAgentId = BytesUtils.getBytes(agentId);
        return getTraceIndexRowKey(bAgentId, time);
    }

    public static byte[] getTraceId(Span span) {
        return BytesUtils.longLongToBytes(span.getMostTraceId(), span.getLeastTraceId());
    }
}
