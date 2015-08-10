package com.navercorp.pinpoint.web.filter.responsetime;

import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilter;

/**
 * @author emeroad
 */
public class AcceptResponseTimeFilter implements ResponseTimeFilter {
    @Override
    public boolean accept(long elapsed) {
        return ACCEPT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AcceptResponseTimeFilter{");
        sb.append('}');
        return sb.toString();
    }
}
