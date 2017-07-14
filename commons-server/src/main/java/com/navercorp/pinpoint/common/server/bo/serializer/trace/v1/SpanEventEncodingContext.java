package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventEncodingContext {
    private final BasicSpan basicSpan;
    private final SpanEventBo spanEventBo;

    public SpanEventEncodingContext(BasicSpan basicSpan, SpanEventBo spanEventBo) {
        if (basicSpan == null) {
            throw new NullPointerException("basicSpan must not be null");
        }
        if (spanEventBo == null) {
            throw new NullPointerException("spanEventBo must not be null");
        }
        this.basicSpan = basicSpan;
        this.spanEventBo = spanEventBo;
    }

    public BasicSpan getBasicSpan() {
        return basicSpan;
    }

    public SpanEventBo getSpanEventBo() {
        return spanEventBo;
    }
}
