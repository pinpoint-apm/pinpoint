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

import java.util.*;

/**
 * @author jaehong.kim
 */
public class SpanCallTree implements CallTree {

    private static final int MIN_DEPTH = -1;
    private static final int LEVEL_DEPTH = -1;
    private static final int ROOT_DEPTH = 0;

    private CallTreeNode root;
    private CallTreeNode cursor;

    public SpanCallTree(final SpanAlign spanAlign) {
        this.root = new CallTreeNode(null, spanAlign);
        this.cursor = this.root;
    }

    public CallTreeNode getRoot() {
        return root;
    }

    public CallTreeIterator iterator() {
        return new CallTreeIterator(root);
    }

    public boolean isEmpty() {
        return root.getValue() == null;
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

    public void add(final int depth, final SpanAlign spanAlign) {

        if (hasCorrupted(spanAlign)) {
            throw new CorruptedSpanCallTreeNodeException("invalid sequence", "corrupted event. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
        }

        if (depth == LEVEL_DEPTH || depth == cursor.getDepth()) {
            // validate
            if (cursor.isRoot()) {
                throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
            }

            CallTreeNode sibling = findLastSibling(cursor);
            sibling.setSibling(spanAlign);
            cursor = sibling.getSibling();
            return;
        }

        // greater
        if (depth > cursor.getDepth()) {
            // validate
            if (depth > cursor.getDepth() + 1) {
                throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
            }

            if (!cursor.hasChild()) {
                cursor.setChild(spanAlign);
                cursor = cursor.getChild();
                return;
            }

            CallTreeNode sibling = findLastSibling(cursor.getChild());
            sibling.setSibling(spanAlign);
            cursor = sibling.getSibling();
            return;
        }

        // lesser
        if (cursor.getDepth() - depth <= ROOT_DEPTH) {
            throw new CorruptedSpanCallTreeNodeException("invalid depth", "invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
        }

        final CallTreeNode node = findUpperLevelLastSibling(depth, cursor);
        node.setSibling(spanAlign);
        cursor = node.getSibling();
    }

    boolean hasCorrupted(final SpanAlign spanAlign) {
        if (spanAlign.isSpan()) {
            return false;
        }

        if (cursor.getValue().isSpan()) {
            return spanAlign.getSpanEventBo().getSequence() != 0;
        }

        return cursor.getValue().getSpanEventBo().getSequence() + 1 != spanAlign.getSpanEventBo().getSequence();
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
        while(sibling != null) {
            sortChildSibling(sibling);
            if(sibling.hasChild()) {
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
        Collections.sort(spans, new Comparator<CallTreeNode>() {
            @Override
            public int compare(CallTreeNode source, CallTreeNode target) {
                return (int) (source.getValue().getStartTime() - target.getValue().getStartTime());
            }
        });

        // sort
        final List<CallTreeNode> nodes = new ArrayList<>();
        for (CallTreeNode event : events) {
            while (spans.peek() != null && event.getValue().getStartTime() > spans.peek().getValue().getStartTime()) {
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
        if (node.getValue().isSpan()) {
            spans.add(node);
        } else {
            events.add(node);
        }

        while (node.hasSibling()) {
            node = node.getSibling();
            if (node.getValue().isSpan()) {
                spans.add(node);
            } else {
                events.add(node);
            }
        }
    }
}