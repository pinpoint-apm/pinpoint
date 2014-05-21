package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.applicationmap.histogram.Histogram;

/**
 * @author emeroad
 */
public class LinkStateResolver {
    public static final LinkStateResolver DEFAULT_LINK_STATE_RESOLVER = new LinkStateResolver();

    public String resolve(Link link) {
        if (link == null) {
            throw new NullPointerException("link must not be null");
        }
        // Histogram이 중복으로 생성되고 있어 그냥 인자로 받음 수정 요망.
        final long error = getErrorRate(link.getHistogram());
        if (error * 100 > 10) {
            return "bad";
        }
        return "default";

    }

    public boolean resolve2(Link link) {
        if (link == null) {
            throw new NullPointerException("link must not be null");
        }
        final long error = getErrorRate(link.getHistogram());
        if (error * 100 > 10) {
            return true;
        }
        return false;
    }

    private long getErrorRate(Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        final long totalCount = histogram.getTotalCount();
        if (totalCount == 0) {
            return 0;
        }
        return histogram.getErrorCount() / totalCount;
    }
}
