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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class CallTreeIterator implements Iterator<CallTreeNode> {

    private List<CallTreeNode> nodes;
    private int index = -1;

    public CallTreeIterator(final CallTreeNode root) {
        if (root == null) {
            return;
        }

        // init
        int count = traversal(root, false);
        this.nodes = new ArrayList<CallTreeNode>(count);

        // populate
        addNode(root);
        if (root.hasChild()) {
            traversal(root.getChild(), true);
        }
        // reset
        index = -1;
    }

    int traversal(final CallTreeNode node, final boolean populate) {
        if (node == null) {
            return 0;
        }

        int count = 1;
        if (populate) {
            addNode(node);
        }

        if (node.hasChild()) {
            count += traversal(node.getChild(), populate);
        }

        // change logic from recursive to loop, because of avoid call-stack-overflow.
        CallTreeNode sibling = node.getSibling();
        while (sibling != null) {
            count += 1;
            if (populate) {
                addNode(sibling);
            }
            if (sibling.hasChild()) {
                count += traversal(sibling.getChild(), populate);
            }
            sibling = sibling.getSibling();
        }

        return count;
    }

    void addNode(CallTreeNode node) {
        nodes.add(node);
        index++;

        final SpanAlign align = node.getValue();
        align.setGap(getGap());
        align.setDepth(node.getDepth());
        align.setExecutionMilliseconds(getExecutionTime());
    }


    public long getGap() {
        final CallTreeNode current = getCurrent();
        if (current.isRoot()) {
            return 0;
        }

        if (current.getValue().isAsyncFirst()) {
            final CallTreeNode parent = getAsyncParent(current);
            if (parent == null) {
                return 0;
            }
            // skip sibling.
            return current.getValue().getStartTime() - parent.getValue().getStartTime();
        }

        final CallTreeNode prev = getPrev();
        if (prev == null) {
            throw new IllegalStateException("A non-root CallTreeNode must have a previous node");
        }

        return current.getValue().getStartTime() - getLastExecuteTime(current, prev);
    }


    public long getLastExecuteTime(final CallTreeNode current, final CallTreeNode prev) {
        if (prev.getDepth() < current.getDepth()) {
            // push and not closed.
            return prev.getValue().getStartTime();
        }

        CallTreeNode node = prev;
        if (prev.getDepth() > current.getDepth()) {
            // pop prev sibling.
            node = getPrevSibling(current);
        }
        while (true) {
            if (!node.getValue().isAsyncFirst()) {
                // not async first.
                return node.getValue().getLastTime();
            } else if (isFirstChild(node)) {
                // first child
                return node.getParent().getValue().getStartTime();
            }
            // pop prev sibling.
            node = getPrevSibling(node);
        }
    }

    CallTreeNode getPrevSibling(final CallTreeNode node) {
        CallTreeNode sibling = node.getParent().getChild();
        while (node != sibling.getSibling()) {
            sibling = sibling.getSibling();
            if (sibling == null) {
                throw new IllegalStateException("Not found prev sibling " + node);
            }
        }

        return sibling;
    }

    boolean isFirstChild(final CallTreeNode node) {
        return node.getParent().getChild() == node;
    }

    CallTreeNode getAsyncParent(final CallTreeNode node) {
        final int asyncId = node.getValue().getSpanEventBo().getAsyncId();
        CallTreeNode parent = node.getParent();
        while (parent != null && !parent.isRoot()) {
            if (!parent.getValue().isSpan() && asyncId == parent.getValue().getSpanEventBo().getNextAsyncId()) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public long getExecutionTime() {
        final CallTreeNode current = getCurrent();
        final SpanAlign align = current.getValue();
        if (!current.hasChild()) {
            return align.getElapsed();
        }

        return align.getElapsed() - getChildrenTotalElapsedTime(current);
    }

    long getChildrenTotalElapsedTime(final CallTreeNode node) {
        long totalElapsed = 0;
        CallTreeNode child = node.getChild();
        while (child != null) {
            SpanAlign align = child.getValue();
            if (!align.isSpan() && !align.isAsyncFirst()) {
                // skip span and first async event;
                totalElapsed += align.getElapsed();
            }
            child = child.getSibling();
        }

        return totalElapsed;
    }

    @Override
    public boolean hasNext() {
        return index < nodes.size() - 1;
    }

    @Override
    public CallTreeNode next() {
        if (!hasNext()) {
            return null;
        }
        index++;
        return nodes.get(index);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    public boolean hasPrev() {
        return index > 0;
    }

    public CallTreeNode prev() {
        if (!hasPrev()) {
            return null;
        }

        index--;
        return nodes.get(index);
    }

    public CallTreeNode getCurrent() {
        return nodes.get(index);
    }

    public CallTreeNode getPrev() {
        if (!hasPrev()) {
            return null;
        }

        return nodes.get(index - 1);
    }

    public CallTreeNode getNext() {
        if (!hasNext()) {
            return null;
        }

        return nodes.get(index + 1);
    }

    public List<SpanAlign> values() {
        List<SpanAlign> values = new ArrayList<>();
        for (CallTreeNode node : nodes) {
            values.add(node.getValue());
        }

        return values;
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (CallTreeNode node : nodes) {
            for (int i = 0; i <= node.getDepth(); i++) {
                sb.append("#");
            }
            sb.append(" : ").append(node);
            sb.append("\n");
        }
        return sb.toString();
    }
}