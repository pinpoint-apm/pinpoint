package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.bo.SpanBo;

import java.util.List;

/**
 * @author emeroad
 */
public class BypassURLPatternFilter implements URLPatternFilter {
    @Override
    public boolean accept(List<SpanBo> spanBoList) {
        return ACCEPT;
    }
}
