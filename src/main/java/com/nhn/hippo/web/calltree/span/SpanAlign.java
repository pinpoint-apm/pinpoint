package com.nhn.hippo.web.calltree.span;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;
import com.profiler.common.dto.thrift.Span;
import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class SpanAlign {
    private int depth;
    private SpanBo span;
    private SubSpanBo subSpanBo;
    private boolean root = true;

    public SpanAlign(int depth, SpanBo span) {
        this.depth = depth;
        this.span = span;
    }

    public SpanAlign(int depth, SpanBo root, SubSpanBo subSpanBo) {
        this.depth = depth;
        this.span = root;
        this.subSpanBo = subSpanBo;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isRoot() {
        return root;
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

    public SpanBo getSpan() {
        return span;
    }

    public SubSpanBo getSubSpanBo() {
        return subSpanBo;
    }
}
