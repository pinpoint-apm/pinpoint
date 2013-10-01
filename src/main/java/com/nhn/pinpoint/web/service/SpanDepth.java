package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;

/**
 *
 */
public class SpanDepth {
    private SpanAlign spanAlign;
    private int id;

    public SpanDepth(SpanAlign spanAlign, int id) {
        if (spanAlign == null) {
            throw new NullPointerException("spanAlign must not be null");
        }
        this.spanAlign = spanAlign;
        this.id = id;
    }

    public SpanAlign getSpanAlign() {
        return spanAlign;
    }

    public int getId() {
        return id;
    }

}
