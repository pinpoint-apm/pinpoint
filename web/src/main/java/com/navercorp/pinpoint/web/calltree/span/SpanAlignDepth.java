package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

public class SpanAlignDepth {
    private static final int DEFAULT_DEPTH = 0;
    private static final int PARENT_DEPTH = -1;

    private int parent;
    private int current;
    private int sync;
    private int asyncParent;
    private int async;

    public SpanAlignDepth(final int depth) {
        if (depth < DEFAULT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth);
        }
        
        parent = current = sync = asyncParent = async = depth;
    }

    public int getDepth(final SpanEventBo spanEventBo) {
        if (spanEventBo.getAsyncId() != -1) {
            if (spanEventBo.getSequence() == 0) {
                asyncParent = parent + current;
            }

            if (spanEventBo.getDepth() != PARENT_DEPTH) {
                async = asyncParent + spanEventBo.getDepth();
            }
            current = async;
        } else {
            if (spanEventBo.getDepth() != PARENT_DEPTH) {
                sync = parent + spanEventBo.getDepth();
            }
            current = sync;
        }

        return current;
    }

    private class Depth {
        int parent;
        int current;

        public Depth(int depth) {
            parent = depth;
            current = depth;
        }
    }
}