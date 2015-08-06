package com.navercorp.pinpoint.web.filter.responsetime;

import com.navercorp.pinpoint.web.vo.ResponseTime;

/**
 * @author emeroad
 */
public class ResponseTimeFilterFactory {
    private final Long from;
    private final Long to;

    public ResponseTimeFilterFactory(Long from, Long to) {
        this.from = from;
        this.to = to;
    }

    public ResponseTimeFilter createFilter() {
        if (from == null && to == null) {
            return new AcceptResponseTimeFilter();
        }
        // TODO default value is 0 or Long.MIN_VALUE ??
        final long fromLong = defaultLong(from, Long.MIN_VALUE);
        final long toLong = defaultLong(to, Long.MAX_VALUE);
        return new DefaultResponseTimeFilter(fromLong, toLong);
    }

    private Long defaultLong(Long value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
