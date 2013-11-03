package com.nhn.pinpoint.web.calltree.server;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 *
 */
@Deprecated
public interface NodeSelector {

    String getServerId(SpanBo span);

    String getServerId(SpanEventBo spanEventBo);

}
