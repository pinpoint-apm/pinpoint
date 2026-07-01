/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.trace.span;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
public class CallTreeIterator implements Iterator<CallTreeNode> {

    private final List<CallTreeNode> nodes = new ArrayList<>();
    private int index = -1;

    public CallTreeIterator(final CallTreeNode root) {
        if (root == null) {
            return;
        }

        // init
        traversal(root, false);
        // populate
        addNode(root);
        if (root.hasChild()) {
            traversal(root.getChild(), true);
        }
        // reset
        index = -1;
    }

    private int traversal(final CallTreeNode node, final boolean populate) {
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

    private void addNode(CallTreeNode node) {
        nodes.add(node);
        index++;

        final Align align = node.getAlign();
        if (align.isMeta()) {
            align.setGapMillis(0);
            align.setGapNanos(0);
            align.setDepth(node.getDepth());
            align.setExecutionMillis(0);
            align.setExecutionNanos(0);
        } else {
            final long gapNanos = getGapNanos();
            final long executionNanos = getExecutionTimeNanos();
            align.setGapMillis(TimeUnit.NANOSECONDS.toMillis(gapNanos));
            align.setGapNanos(gapNanos);
            align.setDepth(node.getDepth());
            align.setExecutionMillis(TimeUnit.NANOSECONDS.toMillis(executionNanos));
            align.setExecutionNanos(executionNanos);
        }

    }

    private long getGapNanos() {
        final CallTreeNode current = getCurrent();
        if (current.isRoot()) {
            return 0;
        }

        if (current.getAlign().isAsyncFirst()) {
            final CallTreeNode parent = getAsyncParent(current);
            if (parent == null) {
                return 0;
            }
            // skip sibling.
            return getStartTimeNanos(current) - getStartTimeNanos(parent);
        }

        final CallTreeNode prev = getPrev();
        if (prev == null) {
            throw new IllegalStateException("A non-root CallTreeNode must have a previous node");
        }

        return getStartTimeNanos(current) - getLastExecuteTimeNanos(current, prev);
    }


    private long getLastExecuteTimeNanos(final CallTreeNode current, final CallTreeNode prev) {
        if (prev.getDepth() < current.getDepth()) {
            // push and not closed.
            return getStartTimeNanos(prev);
        }

        CallTreeNode node = prev;
        if (prev.getDepth() > current.getDepth()) {
            // pop prev sibling.
            node = getPrevSibling(current);
        }
        while (true) {
            if (!node.getAlign().isAsyncFirst()) {
                // not async first.
                return getEndTimeNanos(node);
            } else if (isFirstChild(node)) {
                // first child
                return getStartTimeNanos(node.getParent());
            }
            // pop prev sibling.
            node = getPrevSibling(node);
        }
    }

    private CallTreeNode getPrevSibling(final CallTreeNode node) {
        CallTreeNode sibling = node.getParent().getChild();
        while (node != sibling.getSibling()) {
            sibling = sibling.getSibling();
            if (sibling == null) {
                throw new IllegalStateException("Not found prev sibling " + node);
            }
        }

        return sibling;
    }

    private boolean isFirstChild(final CallTreeNode node) {
        return node.getParent().getChild() == node;
    }

    private CallTreeNode getAsyncParent(final CallTreeNode node) {
        final int asyncId = node.getAlign().getAsyncId();
        CallTreeNode parent = node.getParent();
        while (parent != null && !parent.isRoot()) {
            if (!parent.getAlign().isSpan() && asyncId == parent.getAlign().getSpanEventBo().getNextAsyncId()) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private long getExecutionTimeNanos() {
        final CallTreeNode current = getCurrent();
        final Align align = current.getAlign();
        if (!current.hasChild()) {
            return getElapsedNanos(align);
        }

        return Math.max(getElapsedNanos(align) - getChildrenTotalElapsedTimeNanos(current), 0);
    }

    private long getChildrenTotalElapsedTimeNanos(final CallTreeNode node) {
        final long parentStartTimeNanos = getStartTimeNanos(node);
        final long parentEndTimeNanos = getEndTimeNanos(node);
        if (parentEndTimeNanos <= parentStartTimeNanos) {
            return 0;
        }

        final List<TimeRange> ranges = new ArrayList<>();
        CallTreeNode child = node.getChild();
        while (child != null) {
            Align align = child.getAlign();
            if (!align.isSpan() && !align.isAsyncFirst()) {
                // skip span and first async event;
                final long startTimeNanos = Math.max(parentStartTimeNanos, getStartTimeNanos(child));
                final long endTimeNanos = Math.min(parentEndTimeNanos, getEndTimeNanos(child));
                if (endTimeNanos > startTimeNanos) {
                    ranges.add(new TimeRange(startTimeNanos, endTimeNanos));
                }
            }
            child = child.getSibling();
        }

        return sumMergedRanges(ranges);
    }

    private long sumMergedRanges(List<TimeRange> ranges) {
        if (ranges.isEmpty()) {
            return 0;
        }

        ranges.sort((range1, range2) -> Long.compare(range1.startTimeNanos, range2.startTimeNanos));

        long totalElapsed = 0;
        long startTimeNanos = ranges.get(0).startTimeNanos;
        long endTimeNanos = ranges.get(0).endTimeNanos;
        for (int i = 1; i < ranges.size(); i++) {
            final TimeRange range = ranges.get(i);
            if (range.startTimeNanos <= endTimeNanos) {
                endTimeNanos = Math.max(endTimeNanos, range.endTimeNanos);
            } else {
                totalElapsed += endTimeNanos - startTimeNanos;
                startTimeNanos = range.startTimeNanos;
                endTimeNanos = range.endTimeNanos;
            }
        }

        return totalElapsed + endTimeNanos - startTimeNanos;
    }

    private long getStartTimeNanos(CallTreeNode node) {
        return node.getAlign().getStartTimeNanos();
    }

    private long getEndTimeNanos(CallTreeNode node) {
        return node.getAlign().getEndTimeNanos();
    }

    private long getElapsedNanos(Align align) {
        return align.getElapsedNanos();
    }

    private static class TimeRange {
        private final long startTimeNanos;
        private final long endTimeNanos;

        private TimeRange(long startTimeNanos, long endTimeNanos) {
            this.startTimeNanos = startTimeNanos;
            this.endTimeNanos = endTimeNanos;
        }
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

    private CallTreeNode getCurrent() {
        return nodes.get(index);
    }

    private CallTreeNode getPrev() {
        if (!hasPrev()) {
            return null;
        }

        return nodes.get(index - 1);
    }

    private CallTreeNode getNext() {
        if (!hasNext()) {
            return null;
        }

        return nodes.get(index + 1);
    }

    public List<Align> values() {
        List<Align> values = new ArrayList<>(nodes.size());
        for (CallTreeNode node : nodes) {
            values.add(node.getAlign());
        }

        return values;
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (CallTreeNode node : nodes) {
            for (int i = 0; i <= node.getDepth(); i++) {
                sb.append('#');
            }
            sb.append(" : ").append(node);
            sb.append('\n');
        }
        return sb.toString();
    }
}
