package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

public class SpanAlignDepth {
    private static final int DEFAULT_DEPTH = 0;
    private static final int PARENT_DEPTH = -1;

    private Depth align;
    private Depth async;
    private Depth sync;
    
    public SpanAlignDepth(final int depth) {
        if (depth < DEFAULT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth);
        }

        align = new Depth(depth);
        sync = new Depth(depth);
        async = new Depth(depth);
    }
    
    public int getDepth(final SpanEventBo spanEventBo) {
        if (isAsyncEvent(spanEventBo)) {
            if (isFirstEvent(spanEventBo) || async.first) {
                // start async event. 
                async.parent = align.parent + align.current;
                async.first = false;
            }

            if(isSameParent(spanEventBo)) {
                if(async.parent == 0) {
                    async.current = 1;
                }
            } else {
                int depth = spanEventBo.getDepth();
                if(depth > async.current + 1) {
                    // rebalance.
                    depth = async.current + 1;
                } 
                async.current = async.parent + depth;
            }
            align.current = async.current;
        } else {
            if(isSameParent(spanEventBo)) {
                if(align.current == 0) {
                    sync.current = 1;
                }
            } else {
                int depth = spanEventBo.getDepth();
                if(depth > sync.current + 1) {
                    // rebalance.
                    depth = sync.current + 1;
                } 
                sync.current = sync.parent + depth;
            }
            align.current = sync.current;
        }

        return align.current;
    }

    private boolean isFirstEvent(final SpanEventBo spanEventBo) {
        return spanEventBo.getSequence() == 0;
    }

    private boolean isSameParent(final SpanEventBo spanEventBo) {
        return spanEventBo.getDepth() == PARENT_DEPTH;
    }

    private boolean isAsyncEvent(final SpanEventBo spanEventBo) {
        return spanEventBo.getAsyncId() != -1;
    }

    private class Depth {
        boolean first = true;
        int parent;
        int current;

        public Depth(int depth) {
            parent = depth;
            current = depth;
        }
    }
}