package com.nhn.pinpoint.web.calltree.server;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEventBo;

/**
 *
 */
public class ApplicationIdNodeSelector implements NodeSelector {
    @Override
    public String getServerId(SpanBo span) {
        return span.getApplicationId();
    }

    @Override
    public String getServerId(SpanEventBo spanEventBo) {
        return spanEventBo.getDestinationId();
    }
}
