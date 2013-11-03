package com.nhn.pinpoint.web.calltree.server;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 *
 */
@Deprecated
public class AgentIdNodeSelector implements NodeSelector {

    @Override
    public String getServerId(SpanBo span) {
        return span.getAgentId();
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
