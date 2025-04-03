package com.navercorp.pinpoint.web.scatter;

import com.navercorp.pinpoint.common.timeseries.time.Range;

public record Status(long currentServerTime, long from, long to) {

    public Status(long currentServerTime, Range range) {
        this(currentServerTime, range.getFrom(), range.getTo());
    }
}
