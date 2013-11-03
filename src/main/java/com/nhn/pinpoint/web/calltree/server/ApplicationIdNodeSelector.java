package com.nhn.pinpoint.web.calltree.server;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 *
 */
@Deprecated
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
