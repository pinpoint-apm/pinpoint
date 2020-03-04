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

import java.util.*;

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

    public boolean hasFocusSpan(final long collectorAcceptTime) {
        travel(collectorAcceptTime, root);
        return this.getSpanBo().getCollectorAcceptTime() == collectorAcceptTime;
    }

    boolean travel(final long collectorAcceptTime, CallTreeNode node) {
        if (isFoucsNode(collectorAcceptTime, node)) {
            return true;
        }

        if (node.hasChild()) {
            travel(node.getChild());
        }

        // change logic from recursive to loop, because of avoid call-stack-overflow.
        CallTreeNode sibling = node.getSibling();
        while (sibling != null) {
            if (isFoucsNode(collectorAcceptTime, sibling)) {
                return true;
            }
            if (sibling.hasChild()) {
                travel(sibling.getChild());
            }
            sibling = sibling.getSibling();
        }
        return false;
    }

    boolean isFoucsNode(final long collectorAcceptTime, CallTreeNode node) {
        if (!node.getAlign().isSpan()) {
            // fast filter
            return false;
        }
        if (node.getAlign().getSpanBo().getCollectorAcceptTime() == collectorAcceptTime) {
            return true;
        }
        return false;
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

    public void sort() {
        travel(root);
    }

    void travel(CallTreeNode node) {
        sortChildSibling(node);
        if (node.hasChild()) {
            travel(node.getChild());
        }

        // change logic from recursive to loop, because of avoid call-stack-overflow.
        CallTreeNode sibling = node.getSibling();
        while (sibling != null) {
            sortChildSibling(sibling);
            if (sibling.hasChild()) {
                travel(sibling.getChild());
            }
            sibling = sibling.getSibling();
        }
    }

    void sortChildSibling(final CallTreeNode parent) {
        if (!parent.hasChild() || !parent.getChild().hasSibling()) {
            // no child or no child sibling.
            return;
        }

        final List<CallTreeNode> events = new ArrayList<>();
        final LinkedList<CallTreeNode> spans = new LinkedList<>();
        splitChildSiblingNodes(parent, events, spans);
        if (spans.isEmpty()) {
            // not found span
            return;
        }

        // order by abs.
        spans.sort(new Comparator<CallTreeNode>() {
            @Override
            public int compare(CallTreeNode source, CallTreeNode target) {
                return (int) (source.getAlign().getStartTime() - target.getAlign().getStartTime());
            }
        });

        // sort
        final List<CallTreeNode> nodes = new ArrayList<>();
        for (CallTreeNode event : events) {
            while (spans.peek() != null && event.getAlign().getStartTime() > spans.peek().getAlign().getStartTime()) {
                nodes.add(spans.poll());
            }
            nodes.add(event);
        }
        nodes.addAll(spans);

        // reform sibling
        CallTreeNode prev = null;
        for (CallTreeNode node : nodes) {
            final CallTreeNode reset = null;
            node.setSibling(reset);
            if (prev == null) {
                parent.setChild(node);
                prev = node;
            } else {
                prev.setSibling(node);
                prev = node;
            }
        }
    }

    private void splitChildSiblingNodes(final CallTreeNode parent, List<CallTreeNode> events, List<CallTreeNode> spans) {
        CallTreeNode node = parent.getChild();
        if (node.getAlign().isSpan()) {
            spans.add(node);
        } else {
            events.add(node);
        }

        while (node.hasSibling()) {
            node = node.getSibling();
            if (node.getAlign().isSpan()) {
                spans.add(node);
            } else {
                events.add(node);
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