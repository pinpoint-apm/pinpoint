package com.nhn.hippo.web.calltree.server;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEvent;

/**
 *
 */
public interface NodeSelector {

    String getServerId(SpanBo span);

    String getServerId(SpanEvent spanEvent);

}
