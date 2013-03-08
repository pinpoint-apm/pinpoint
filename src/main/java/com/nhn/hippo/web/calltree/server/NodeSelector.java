package com.nhn.hippo.web.calltree.server;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEventBo;

/**
 *
 */
public interface NodeSelector {

    String getServerId(SpanBo span);

    String getServerId(SpanEventBo spanEventBo);

}
