package com.profiler.common.util;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.common.dto.thrift.SubSpanList;

public class SpanUtils {

    public static final int AGENT_NAME_LIMIT = 24;

    public static byte[] getTraceIndexRowKey(Span span) {
        // TODO 서버가 받은 시간을 키로 사용해야 될듯 함???
        return getTraceIndexRowKey(span.getAgentId(), span.getStartTime());
    }

    public static byte[] getApplicationTraceIndexRowKey(String applicationName, Span span) {
        // TODO 서버가 받은 시간을 키로 사용해야 될듯 함???
        return getTraceIndexRowKey(applicationName, span.getStartTime());
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

    public static byte[] getTraceId(SubSpan subSpan) {
        return BytesUtils.longLongToBytes(subSpan.getMostTraceId(), subSpan.getLeastTraceId());
    }

    public static byte[] getTraceId(SubSpanList subSpanList) {
        return BytesUtils.longLongToBytes(subSpanList.getMostTraceId(), subSpanList.getLeastTraceId());
    }


}
