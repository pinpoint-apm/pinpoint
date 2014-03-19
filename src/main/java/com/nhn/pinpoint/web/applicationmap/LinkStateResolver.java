package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;

/**
 * @author emeroad
 */
public class LinkStateResolver {
    public String resolve(Link link, Histogram histogram) {
        // Histogram이 중복으로 생성되고 있어 그냥 인자로 받음 수정 요망.
        final long error = getErrorRate(histogram);
        if(error * 100 > 10) {
            return "bad";
        }
        return "default";

    }

    private long getErrorRate(Histogram histogram) {
        long totalCount = histogram.getTotalCount();
        if (totalCount == 0) {
            return 0;
        }
        return histogram.getErrorCount() / totalCount;
    }
}
