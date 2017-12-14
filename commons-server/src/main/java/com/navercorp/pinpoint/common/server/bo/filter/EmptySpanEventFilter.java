package com.navercorp.pinpoint.common.server.bo.filter;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EmptySpanEventFilter implements SpanEventFilter {

    public EmptySpanEventFilter() {
    }

    @Override
    public boolean filter(SpanEventBo spanEventBo) {
        return ACCEPT;
    }
}
