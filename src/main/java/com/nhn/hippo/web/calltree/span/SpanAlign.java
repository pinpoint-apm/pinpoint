package com.nhn.hippo.web.calltree.span;

import com.profiler.common.dto.thrift.Span;
import org.apache.commons.lang.StringUtils;

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

//    public String getDepthSpace() {
//        StringBuilder sb = new StringBuilder(depth);
//        for (int i = 0; i < depth; i++) {
//            sb.append(' ');
//        }
//        return sb.toString();
//    }

    public Span getSpan() {
        return span;
    }
}
