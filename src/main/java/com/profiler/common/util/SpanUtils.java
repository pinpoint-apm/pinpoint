package com.profiler.common.util;

import com.profiler.common.dto.thrift.Span;

public class SpanUtils {

    public static byte[] getTraceIndexRowKey(Span span) {
        return BytesUtils.add(span.getAgentId(), span.getTimestamp());
    }

    public static byte[] getTraceId(Span span) {
        return BytesUtils.longLongToBytes(span.getMostTraceId(), span.getLeastTraceId());
    }
}
