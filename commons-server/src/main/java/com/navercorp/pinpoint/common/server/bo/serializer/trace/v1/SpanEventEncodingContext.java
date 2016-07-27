package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventEncodingContext {
    private final long spanId;
    private final SpanEventBo spanEventBo;

    public SpanEventEncodingContext(long spanId, SpanEventBo spanEventBo) {
        this.spanId = spanId;
        this.spanEventBo = spanEventBo;
    }

    public long getSpanId() {
        return spanId;
    }

    public SpanEventBo getSpanEventBo() {
        return spanEventBo;
    }
}
