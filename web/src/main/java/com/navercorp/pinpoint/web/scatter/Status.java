package com.navercorp.pinpoint.web.scatter;

import com.navercorp.pinpoint.common.server.util.time.Range;

public record Status(long currentServerTime, long from, long to) {

    public Status(long currentServerTime, Range range) {
        this(currentServerTime, range.getFrom(), range.getTo());
    }
}
