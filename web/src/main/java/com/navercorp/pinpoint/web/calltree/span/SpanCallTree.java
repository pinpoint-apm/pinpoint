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

/**
 * 
 * @author jaehong.kim
 *
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

    public void add(final int depth, final SpanAlign spanAlign) {
        if (depth < MIN_DEPTH || depth == ROOT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
        }

        if (hasMissing(spanAlign)) {
            throw new MissingSpanEventException("missing event. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
        }

        if (depth == LEVEL_DEPTH || depth == cursor.getDepth()) {
            // validate
            if (cursor.isRoot()) {
                throw new IllegalArgumentException("invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
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
                throw new IllegalArgumentException("invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
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
            throw new IllegalArgumentException("invalid depth. depth=" + depth + ", cursor=" + cursor + ", align=" + spanAlign);
        }

        final CallTreeNode node = findUpperLevelLastSibling(depth, cursor);
        node.setSibling(spanAlign);
        cursor = node.getSibling();
    }

    boolean hasMissing(final SpanAlign spanAlign) {
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
}