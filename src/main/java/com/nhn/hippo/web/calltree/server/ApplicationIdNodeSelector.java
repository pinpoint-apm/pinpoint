package com.nhn.hippo.web.calltree.server;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEvent;

/**
 *
 */
public class ApplicationIdNodeSelector implements NodeSelector {
    @Override
    public String getServerId(SpanBo span) {
        return span.getApplicationId();
    }

    @Override
    public String getServerId(SpanEvent spanEvent) {
        return spanEvent.getDestinationId();
    }
}
