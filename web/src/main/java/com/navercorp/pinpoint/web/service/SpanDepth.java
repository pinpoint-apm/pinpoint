package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;

/**
 * @author emeroad
 */
public class SpanDepth {
    private final SpanAlign spanAlign;
    private final int id;
    // gap 을 구하기 위해 바로 전 lastExecuteTime을 구함
    private final long lastExecuteTime;

    public SpanDepth(SpanAlign spanAlign, int id, long lastExecuteTime) {
        if (spanAlign == null) {
            throw new NullPointerException("spanAlign must not be null");
        }
        this.spanAlign = spanAlign;
        this.id = id;
        this.lastExecuteTime = lastExecuteTime;
    }

    public SpanAlign getSpanAlign() {
        return spanAlign;
    }

    public int getId() {
        return id;
    }

    public long getLastExecuteTime() {
        return lastExecuteTime;
    }
}
