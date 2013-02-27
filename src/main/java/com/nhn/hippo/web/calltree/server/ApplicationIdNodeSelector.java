package com.nhn.hippo.web.calltree.server;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

/**
 *
 */
public class ApplicationIdNodeSelector implements NodeSelector {
    @Override
    public String getServerId(SpanBo span) {
        return span.getServiceName();
    }

    @Override
    public String getServerId(SubSpanBo span) {
        return span.getDestinationId();
    }
}
