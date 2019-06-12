/*
 * Copyright 2017 NAVER Corp.
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
public class LinkedCallTree implements CallTree {
    private CallTreeNode root;

    public LinkedCallTree(final Align align) {
        this.root = new CallTreeNode(null, align);
    }

    public void update(final CallTree callTree) {
        final CallTreeNode updateNode = callTree.getRoot();
        this.root.setChild(updateNode.getChild());
        updateNode.setParent(this.root.getParent());
        this.root.setAlign(updateNode.getAlign());
    }

    public void remove() {
        final CallTreeNode parent = this.root.getParent();
        CallTreeNode prev = null;
        CallTreeNode node = parent.getChild();
        while (node != null) {
            if(node == this.root) {
                CallTreeNode next = node.getSibling();
                if (prev == null) {
                    // first sibling
                    if (next == null) {
                        // only one.
                        parent.setChild((CallTreeNode) null);
                        this.root.setParent(null);
                    } else {
                        // copy to next
                        parent.setChild(next);
                        this.root.setSibling((CallTreeNode) null);
                        this.root.setParent(null);
                    }
                } else {
                    if (next == null) {
                        // last sibling
                        prev.setSibling((CallTreeNode) null);
                        this.root.setParent(null);
                    } else {
                        // copy to next
                        prev.setSibling(next);
                        this.root.setSibling((CallTreeNode) null);
                        this.root.setParent(null);
                    }
                }
                return;
            }
            // searching
            prev = node;
            node = node.getSibling();
        }
    }

    @Override
    public CallTreeNode getRoot() {
        return root;
    }

    @Override
    public CallTreeIterator iterator() {
        return new CallTreeIterator(root);
    }

    @Override
    public boolean isEmpty() {
        return root.getAlign() == null;
    }

    @Override
    public void add(CallTree tree) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int parentDepth, CallTree tree) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int depth, Align align) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort() {
    }
}