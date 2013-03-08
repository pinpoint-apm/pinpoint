package com.nhn.hippo.web.calltree.server;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEventBo;

/**
 *
 */
public class AgentIdNodeSelector implements NodeSelector {

    @Override
    public String getServerId(SpanBo span) {
        return span.getEndPoint();
    }

    @Override
    public String getServerId(SpanEventBo spanEventBo) {
        return spanEventBo.getEndPoint();
    }

}
