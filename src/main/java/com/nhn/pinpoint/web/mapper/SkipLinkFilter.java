package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.web.vo.Application;

/**
 * @author emeroad
 */
public class SkipLinkFilter implements LinkFilter {
    public static LinkFilter FILTER = new SkipLinkFilter();

    private SkipLinkFilter() {
    }

    @Override
    public boolean filter(Application foundApplication) {
        return false;
    }
}
