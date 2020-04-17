package com.navercorp.pinpoint.web.scatter;

import com.navercorp.pinpoint.web.vo.Range;

public class Status {
    private final long currentServerTime;
    private final long from;
    private final long to;

    public Status(long currentServerTime, Range range) {
        this(currentServerTime, range.getFrom(), range.getTo());
    }

    public Status(long currentServerTime, long from, long to) {
        this.currentServerTime = currentServerTime;
        this.from = from;
        this.to = to;
    }

    public long getCurrentServerTime() {
        return currentServerTime;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }
}
