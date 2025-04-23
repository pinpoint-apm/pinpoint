/*
 * Copyright 2015 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.SpanBo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author jaehong.kim
 */
public class SpanCallTree implements CallTree {

    private static final int MIN_DEPTH = -1;
    private static final int LEVEL_DEPTH = -1;
    private static final int ROOT_DEPTH = 0;

    private final CallTreeNode root;
    private CallTreeNode cursor;

    public SpanCallTree(final Align align) {
        this.root = new CallTreeNode(null, align);
        this.cursor = this.root;
    }

    private SpanBo getSpanBo() {
        return this.root.getAlign().getSpanBo();
    }

    public boolean isRootSpan() {
        return this.getSpanBo().isRoot();
    }

    public boolean filterSpan(Predicate<SpanBo> spanFilter) {
        Objects.requireNonNull(spanFilter, "spanFilter");

        DfsTraversal context = new DfsTraversal(spanFilter);
        return context.travel(root);
    }

    private static class DfsTraversal {
        private final static int MAX_OVERFLOW_COUNT = 1024;
        private final Predicate<SpanBo> filter;

        // Defence cycle ref
        // Weak validate
        private int overflowCounter;
        // Aggressive validate
        // equals, hashcode not implemented
        // private List<CallTreeNode> visited = new ArrayList<>();

        public DfsTraversal(Predicate<SpanBo> filter) {
            this.filter = Objects.requireNonNull(filter, "filter");
        }

        private boolean travel(CallTreeNode node) {
            if (checkOverFlow()) {
                return false;
            }
            if (filterNode(node)) {
                return true;
            }

            if (node.hasChild()) {
                if (travel(node.getChild())) {
                    return true;
                }
            }

            // change logic from recursive to loop, because of avoid call-stack-overflow.
            CallTreeNode sibling = node.getSibling();
            while (sibling != null) {
                if (filterNode(sibling)) {
                    return true;
                }
                if (sibling.hasChild()) {
                    if (travel(sibling.getChild())) {
                        return true;
                    }
                }
                sibling = sibling.getSibling();
            }
            return false;
        }

        private boolean checkOverFlow() {
            if (overflowCounter++ > MAX_OVERFLOW_COUNT) {
                return true;
            }
            return false;
        }

        boolean filterNode(CallTreeNode node) {
            if (!node.getAlign().isSpan()) {
                // fast filter
                return false;
            }
            SpanBo spanBo = node.getAlign().getSpanBo();
            return filter.test(spanBo);
        }
    }

    public CallTreeNode getRoot() {
        return root;
    }

    public CallTreeIterator iterator() {
        return new CallTreeIterator(root);
    }

    public boolean isEmpty() {
        return root.getAlign() == null;
    }

    public void add(final CallTree tree) {
        final CallTreeNode node = tree.getRoot();
        if (node == null) {
            // skip
            return;
        }

        if (!cursor.hasChild()) {
            node.setParent(cursor);
            cursor.setChild(node);
            return;
        }

        CallTreeNode sibling = findLastSibling(cursor.getChild());
        node.setParent(sibling.getParent());
        sibling.setSibling(node);
    }

    // test only
    public void add(final int parentDepth, final CallTree tree) {
        if (parentDepth < MIN_DEPTH) {
            throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid parent depth. parent depth=" + parentDepth + ", cursor=" + cursor + ", tree=" + tree);
        }

        final CallTreeNode node = tree.getRoot();
        if (node == null) {
            // skip
            return;
        }

        if (parentDepth == LEVEL_DEPTH || parentDepth == cursor.getDepth()) {
            // validate
            if (cursor.isRoot()) {
                throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + parentDepth + ", cursor=" + cursor + ", tree=" + tree);
            }

            if (!cursor.hasChild()) {
                node.setParent(cursor);
                cursor.setChild(node);
                return;
            }

            CallTreeNode sibling = findLastSibling(cursor.getChild());
            node.setParent(cursor);
            sibling.setSibling(node);
            return;
        }

        // greater
        if (parentDepth > cursor.getDepth()) {
            throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + parentDepth + ", cursor=" + cursor + ", align=" + tree);
        }

        // lesser
        if (cursor.getDepth() - parentDepth < ROOT_DEPTH) {
            throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + parentDepth + ", cursor=" + cursor + ", align=" + tree);
        }

        final CallTreeNode parent = findUpperLevelLastSibling(parentDepth, cursor);
        cursor = parent;
        if (!cursor.hasChild()) {
            node.setParent(cursor);
            cursor.setChild(node);
            return;
        }

        CallTreeNode sibling = findLastSibling(cursor.getChild());
        node.setParent(cursor);
        sibling.setSibling(node);
    }

    public void add(final Align align) {
        Objects.requireNonNull(align, "align");

        final int depth = align.getSpanEventBo().getDepth();
        add(depth, align);
    }

    public void add(final int depth, final Align align) {

        if (hasCorrupted(align)) {
            throw new CorruptedSpanCallTreeNodeException("invalid sequence", "corrupted event. depth=" + depth + ", cursor=" + cursor + ", align=" + align);
        }

        if (depth == LEVEL_DEPTH || depth == cursor.getDepth()) {
            // validate
            if (cursor.isRoot()) {
                throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + align);
            }

            CallTreeNode sibling = findLastSibling(cursor);
            sibling.setSibling(align);
            cursor = sibling.getSibling();
            return;
        }

        // greater
        if (depth > cursor.getDepth()) {
            // validate
            if (depth > cursor.getDepth() + 1) {
                throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + align);
            }

            if (!cursor.hasChild()) {
                cursor.setChild(align);
                cursor = cursor.getChild();
                return;
            }

            CallTreeNode sibling = findLastSibling(cursor.getChild());
            sibling.setSibling(align);
            cursor = sibling.getSibling();
            return;
        }

        // lesser
        if (cursor.getDepth() - depth <= ROOT_DEPTH) {
            throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + align);
        }

        final CallTreeNode node = findUpperLevelLastSibling(depth, cursor);
        node.setSibling(align);
        cursor = node.getSibling();
    }

    boolean hasCorrupted(final Align align) {
        if (align.isSpan()) {
            return false;
        }

        if (cursor.getAlign().isSpan()) {
            return align.getSpanEventBo().getSequence() != 0;
        }

        int cursorSequence = cursor.getAlign().getSpanEventBo().getSequence() + 1;
        short spanEventSequence = align.getSpanEventBo().getSequence();
        return cursorSequence != spanEventSequence;
    }

    CallTreeNode findUpperLevelLastSibling(final int level, final CallTreeNode node) {
        final CallTreeNode parent = findUpperLevel(level, node);
        return findLastSibling(parent);
    }

    CallTreeNode findUpperLevel(final int level, final CallTreeNode node) {
        CallTreeNode parent = node.getParent();
        while (parent.getDepth() != level) {
            parent = parent.getParent();
        }

        return parent;
    }

    CallTreeNode findLastSibling(final CallTreeNode node) {
        CallTreeNode lastSibling = node;
        while (lastSibling.getSibling() != null) {
            lastSibling = lastSibling.getSibling();
        }

        return lastSibling;
    }

    @Override
    public void pruning(Predicate<CallTreeNode> filter) {
        pruningTravel(filter, root);
    }

    void pruningTravel(Predicate<CallTreeNode> filter, CallTreeNode node) {
        if (node == null) {
            return;
        }

        final List<CallTreeNode> siblings = new ArrayList<>();
        CallTreeNode sibling = node.getSibling();
        while (sibling != null) {
            siblings.add(sibling);
            sibling = sibling.getSibling();
        }

        CallTreeNode nextSibling = null;
        boolean removed = false;
        for (int i = siblings.size(); i > 0; i--) {
            final CallTreeNode lastSibling = siblings.get(i - 1);
            if (removed) {
                lastSibling.setSibling(nextSibling);
            }
            if (lastSibling.hasChild()) {
                pruningTravel(filter, lastSibling.getChild());
            }
            if (filter.test(lastSibling)) {
                removed = true;
                nextSibling = lastSibling.getSibling();

                lastSibling.setParent(null);
                lastSibling.setSibling((CallTreeNode) null);
                lastSibling.setChild((CallTreeNode) null);
                lastSibling.setAlign(null);
            } else {
                removed = false;
                nextSibling = lastSibling;
            }
        }
        node.setSibling(nextSibling);

        if (node.hasChild()) {
            pruningTravel(filter, node.getChild());
        }

        if (filter.test(node)) {
            final CallTreeNode parent = node.getParent();
            if (parent != null) {
                parent.setChild(node.getSibling());

                node.setParent(null);
                node.setSibling((CallTreeNode) null);
                node.setChild((CallTreeNode) null);
                node.setAlign(null);
            }
        }
    }

    @Override
    public String toString() {
        final SpanBo spanBo = getSpanBo();

        final StringBuilder sb = new StringBuilder("{");
        sb.append("parentSpanId=").append(spanBo.getParentSpanId());
        sb.append(", spanId=").append(spanBo.getSpanId());
        sb.append(", startTime=").append(spanBo.getStartTime());
        sb.append(", elapsed=").append(spanBo.getElapsed());
        sb.append('}');
        return sb.toString();
    }
}