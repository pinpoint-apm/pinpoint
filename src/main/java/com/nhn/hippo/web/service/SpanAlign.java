package com.nhn.hippo.web.service;

import com.profiler.common.dto.thrift.Span;

/**
 *
 */
public class SpanAlign {
    private int depth;
    private Span span;

    public SpanAlign(int depth, Span span) {
        this.depth = depth;
        this.span = span;
    }

    public int getDepth() {
        return depth;
    }

    public Span getSpan() {
        return span;
    }
}
