package com.navercorp.pinpoint.web.calltree.span;

import java.util.LinkedList;
import java.util.List;

public class CallTreeCursor {
    private List<CallTreeNode> alignedList = new LinkedList<CallTreeNode>();
    private int index = 0;
    private CallTreeNode cursor;

    public CallTreeCursor(CallTreeNode cursor) {
        alien(findRoot(cursor));
        index = alignedList.indexOf(cursor);
    }

    CallTreeNode findRoot(CallTreeNode cursor) {
        CallTreeNode node = cursor;
        while (!node.isRoot()) {
            node = node.getParent();
        }

        return node;
    }

    void alien(CallTreeNode node) {
        alignedList.add(node);
        if (node.hasChild()) {
            alien(node.getChild());
        }

        if (node.hasSibling()) {
            alien(node.getSibling());
        }
    }

    public CallTreeCursor getCursor() {
        return new CallTreeCursor(get());
    }

    public CallTreeNode get() {
        return alignedList.get(index);
    }

    public CallTreeNode prev() {
        if (index <= 0) {
            return null;
        }

        return alignedList.get(index--);
    }

    public CallTreeNode next() {
        if (index >= alignedList.size()) {
            return null;
        }

        return alignedList.get(index++);
    }

    public CallTreeNode findNext() {
        if (cursor.hasChild()) {
            return cursor.getChild();
        }

        if (cursor.isRoot()) {
            return null;
        }

        if (cursor.hasSibling()) {
            return cursor.getSibling();
        }

        return findUpperLevelFirstSibling(cursor);
    }

    CallTreeNode findUpperLevelFirstSibling(final CallTreeNode node) {
        if (node.isRoot()) {
            return null;
        }

        CallTreeNode parent = node.getParent();
        while (!parent.hasSibling()) {
            if (parent.isRoot()) {
                return null;
            }
            parent = parent.getParent();
        }

        return parent;
    }

}