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
 * @author jaehong.kim
 */
public class CallTreeNode {

    private CallTreeNode parent;
    private CallTreeNode child;
    private CallTreeNode sibling;
    private Align align;

    public CallTreeNode(final CallTreeNode parent, Align align) {
        this.parent = parent;
        this.align = align;
    }

    public void setParent(final CallTreeNode parent) {
        this.parent = parent;
    }

    public CallTreeNode getParent() {
        return parent;
    }

    public void setAlign(final Align align) {
        this.align = align;
    }

    public Align getAlign() {
        return align;
    }

    public void setChild(final CallTreeNode child) {
        this.child = child;
    }

    public void setChild(final Align align) {
        this.child = new CallTreeNode(this, align);
    }

    public CallTreeNode getChild() {
        return this.child;
    }

    public boolean hasChild() {
        return this.child != null;
    }

    public void setSibling(final CallTreeNode sibling) {
        this.sibling = sibling;
    }

    public void setSibling(final Align align) {
        this.sibling = new CallTreeNode(parent, align);
    }

    public CallTreeNode getSibling() {
        return sibling;
    }

    public boolean hasSibling() {
        return this.sibling != null;
    }

    public int getDepth() {
        if (isRoot()) {
            return 0;
        }

        // change logic from recursive to loop, because of avoid call-stack-overflow.
        int depth = 1;
        CallTreeNode node = parent.getParent();
        while (node != null) {
            depth++;
            node = node.getParent();
        }

        return depth;
    }

    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{depth=");
        builder.append(getDepth());
        builder.append(", child=");
        builder.append(child != null ? true : false);
        builder.append(", sibling=");
        builder.append(sibling != null ? true : false);
        builder.append(", align=");
        builder.append(align);
        builder.append("}");
        return builder.toString();
    }
}