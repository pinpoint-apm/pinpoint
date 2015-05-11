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
 * 
 * @author jaehong.kim
 *
 */
public class CallTreeIterator implements Iterator<CallTreeNode> {

    private List<CallTreeNode> nodes = new LinkedList<CallTreeNode>();
    private int index = -1;

    public CallTreeIterator(final CallTreeNode root) {
        if (root == null) {
            return;
        }

        populate(root);
        index = -1;
    }

    void populate(CallTreeNode node) {
        nodes.add(node);
        index++;

        final SpanAlign align = node.getValue();
        align.setGap(getGap());
        align.setDepth(node.getDepth());
        align.setExecutionMilliseconds(getExecutionTime());

        if (node.hasChild()) {
            populate(node.getChild());
        }

        if (node.hasSibling()) {
            populate(node.getSibling());
        }
    }

    public long getGap() {
        final CallTreeNode current = getCurrent();
        final long lastExecuteTime = getLastExecuteTime();
        final SpanAlign currentAlign = current.getValue();
        if (currentAlign.isSpan()) {
            return currentAlign.getStartTime() - lastExecuteTime;
        }

        if (currentAlign.isAsyncFirst()) {
            final CallTreeNode parent = getAsyncParent(current);
            if (parent == null) {
                return 0;
            }
            final SpanAlign parentAlign = parent.getValue();
            return currentAlign.getStartTime() - parentAlign.getStartTime();
        }

        return currentAlign.getStartTime() - lastExecuteTime;
    }

    public long getLastExecuteTime() {
        final CallTreeNode current = getCurrent();
        final SpanAlign currentAlign = current.getValue();
        if (current.isRoot()) {
            return currentAlign.getStartTime();
        }

        CallTreeNode prev = getPrev();
        final SpanAlign prevAlign = prev.getValue();
        if (!currentAlign.isAsync() && !prevAlign.isSpan() && prevAlign.isAsync()) {
            // skip sub(async) call tree.
            prev = current.getParent();
        }

        if (prev.getDepth() == current.getDepth()) {
            // equal
            final SpanAlign align = prev.getValue();
            return align.getLastTime();
        }

        if (prev.getDepth() > current.getDepth()) {
            // less, back step
            CallTreeNode sibling = getPrevSibling(current);
            final SpanAlign align = sibling.getValue();
            return align.getStartTime();
        } else {
            // bigger
            final SpanAlign align = prev.getValue();
            return align.getStartTime();
        }
    }

    CallTreeNode getPrevSibling(final CallTreeNode node) {
        CallTreeNode sibling = node.getParent().getChild();
        while (node != sibling.getSibling()) {
            sibling = sibling.getSibling();
        }

        return sibling;
    }

    CallTreeNode getAsyncParent(final CallTreeNode node) {
        final int asyncId = node.getValue().getSpanEventBo().getAsyncId();
        CallTreeNode parent = node.getParent();
        while (!parent.isRoot()) {
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
        List<SpanAlign> values = new ArrayList<SpanAlign>();
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