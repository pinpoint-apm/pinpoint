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

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;

/**
 * 
 * @author jaehong.kim
 *
 */
public class CallTreeIterator implements Iterator<CallTreeNode> {

    private List<CallTreeNode> nodes = new LinkedList<CallTreeNode>();
    private int index = -1;

    public CallTreeIterator(CallTreeNode root) {
        populate(root);
        index = -1;
    }

    void populate(CallTreeNode node) {
        nodes.add(node);
        index++;

        final SpanAlign spanAlign = node.getValue();
        final long gap = getGap(node);
        spanAlign.setGap(gap);
        spanAlign.setDepth(node.getDepth());

        if (node.hasChild()) {
            populate(node.getChild());
        }

        if (node.hasSibling()) {
            populate(node.getSibling());
        }
    }

    long getGap(final CallTreeNode node) {
        final long lastExecuteTime = getLastExecuteTime(node);
        SpanAlign spanAlign = getCurrent().getValue();
        if (spanAlign.isSpan()) {
            return spanAlign.getSpanBo().getStartTime() - lastExecuteTime;
        }

        if (spanAlign.getSpanEventBo().isAsync() && spanAlign.getSpanEventBo().getSequence() == 0) {
            CallTreeNode parent = getAsyncParent(getCurrent());
            if (parent == null) {
                return 0;
            }
            return spanAlign.getSpanEventBo().getStartElapsed() - parent.getValue().getSpanEventBo().getStartElapsed();
        }

        return (spanAlign.getSpanBo().getStartTime() + spanAlign.getSpanEventBo().getStartElapsed()) - lastExecuteTime;
    }

    long getLastExecuteTime(final CallTreeNode node) {
        if (node.isRoot()) {
            return node.getValue().getSpanBo().getStartTime();
        }

        CallTreeNode prev = getPrev();
        if(!node.getValue().isSpan() && prev.getValue().isAsync() && !node.getValue().isAsync()) {
            // skip sub(async) call tree.
            prev = node.getParent();
        }
        
        if (prev.getDepth() < node.getDepth()) {
            return getStartTime(prev);
        } else if (prev.getDepth() > node.getDepth()) {
            return getLastTime(getPrevSibling(node));
        } else {
            return getLastTime(prev);
        }
    }

    CallTreeNode getPrevSibling(final CallTreeNode node) {
        CallTreeNode sibling = node.getParent().getChild();
        while (node != sibling.getSibling()) {
            sibling = sibling.getSibling();
        }

        return sibling;
    }

    long getLastTime(final CallTreeNode node) {
        final SpanBo spanBo = node.getValue().getSpanBo();
        if (node.getValue().isSpan()) {
            return spanBo.getStartTime() + spanBo.getElapsed();
        } else {
            SpanEventBo spanEventBo = node.getValue().getSpanEventBo();
            return spanBo.getStartTime() + spanEventBo.getStartElapsed() + spanEventBo.getEndElapsed();
        }
    }

    long getStartTime(final CallTreeNode node) {
        final SpanBo spanBo = node.getValue().getSpanBo();
        if (node.getValue().isSpan()) {
            return spanBo.getStartTime();
        } else {
            return spanBo.getStartTime() + node.getValue().getSpanEventBo().getStartElapsed();
        }
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