package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.web.vo.Application;

/**
 * @author emeroad
 */
public class SkipLinkFilter implements LinkFilter {
    public static final LinkFilter FILTER = new SkipLinkFilter();

    private SkipLinkFilter() {
    }

    @Override
    public boolean filter(Application foundApplication) {
        return false;
    }
}
