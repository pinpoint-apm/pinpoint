package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.server.bo.SpanBo;

import java.util.List;

/**
 * @author emeroad
 */
public interface URLPatternFilter {

    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean accept(List<SpanBo> spanBoLi);
}
