package com.nhn.pinpoint.web.calltree.server;

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
        String endPoint = spanEventBo.getEndPoint();
        if (endPoint == null || endPoint.length() == 0) {
            // http client와 같은 경우는 destinationId()만 존재한다.
            return spanEventBo.getDestinationId();
        }
        return endPoint +"/" + spanEventBo.getDestinationId();
    }

}
