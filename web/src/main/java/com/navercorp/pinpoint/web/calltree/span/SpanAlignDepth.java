/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

/**
 * 
 * @author jaehong.kim
 *
 */
public class SpanAlignDepth {
    private static final int DEFAULT_DEPTH = 0;
    private static final int PARENT_DEPTH = -1;

    private Depth align;
    private Depth async;
    private Depth sync;
    private SpanEventBo prev;

    public SpanAlignDepth(final int depth) {
        if (depth < DEFAULT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth);
        }

        align = new Depth(depth);
        sync = new Depth(depth);
        async = new Depth(depth);
    }

    public int getCurrentDepth() {
        return align.current;
    }

    public boolean isParentMissing(final SpanEventBo spanEventBo) {
        final int sequence = spanEventBo.getSequence();
        if (spanEventBo.isAsync()) {
            if (async.first) {
                if (sequence != 0) {
                    return true;
                }
            } else {
                if (sequence != 0 && sequence > async.sequence + 1) {
                    // check sequence
                    return true;
                }
            }
        } else {
            if (sequence > sync.sequence + 1) {
                // check sequence
                return true;
            }

            if (prev != null && prev.getNextAsyncId() != -1) {
                // check next-async-id
                if (prev.getNextAsyncId() != spanEventBo.getAsyncId()) {
                    return true;
                }
            }
        }

        return false;
    }

    public int getMissingDepth(final SpanEventBo spanEventBo) {
        if (spanEventBo.isAsync()) {
            if (isFirstEvent(spanEventBo) || async.first) {
                // start async event.
                async.parent = align.parent + align.current;
                async.first = false;
            }

            if (isSameParent(spanEventBo)) {
                if (async.parent == 0) {
                    async.current = 1;
                }
            } else {
                int depth = spanEventBo.getDepth();
                if (depth > async.current + 1) {
                    // rebalance.
                    depth = async.current + 1;
                }
                async.current = async.parent + depth;
            }
            align.current = async.current;
        } else {
            if (isSameParent(spanEventBo)) {
                if (align.current == 0) {
                    sync.current = 1;
                }
            } else {
                int depth = spanEventBo.getDepth();
                if (depth > sync.current + 1) {
                    // rebalance.
                    depth = sync.current + 1;
                }
                sync.current = sync.parent + depth;
            }
            align.current = sync.current;
        }

        return align.current;
    }

    public int getDepth(final SpanEventBo spanEventBo) {
        if (spanEventBo.isAsync()) {
            async.sequence = spanEventBo.getSequence();
            if (isFirstEvent(spanEventBo) || async.first) {
                // start async event.
                async.parent = align.parent + align.current;
                async.first = false;
            }

            if (!isSameParent(spanEventBo)) {
                int depth = spanEventBo.getDepth();
                async.current = async.parent + depth;
            }
            align.current = async.current;
        } else {
            sync.sequence = spanEventBo.getSequence();
            if (!isSameParent(spanEventBo)) {
                int depth = spanEventBo.getDepth();
                sync.current = sync.parent + depth;
            }
            align.current = sync.current;
        }
        this.prev = spanEventBo;

        return align.current;
    }

    private boolean isFirstEvent(final SpanEventBo spanEventBo) {
        return spanEventBo.getSequence() == 0;
    }

    private boolean isSameParent(final SpanEventBo spanEventBo) {
        return spanEventBo.getDepth() == PARENT_DEPTH;
    }

    private class Depth {
        boolean first = true;
        int parent;
        int current;
        int sequence;

        public Depth(int depth) {
            parent = depth;
            current = depth;
        }
    }
}