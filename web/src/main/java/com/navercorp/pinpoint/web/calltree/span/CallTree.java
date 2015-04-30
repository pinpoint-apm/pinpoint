package com.navercorp.pinpoint.web.calltree.span;

public class CallTree {
    private static final int MIN_DEPTH = -1;
    private static final int LEVEL_DEPTH = -1;
    private static final int ROOT_DEPTH = 0;

    private CallTreeNode root;
    private CallTreeNode prev;

    public CallTree(final SpanAlign spanAlign) {
        this.root = new CallTreeNode(null, spanAlign);
        this.prev = this.root;
    }

    public CallTreeNode getRoot() {
        return root;
    }

    public CallTreeCursor getCursor() {
        return new CallTreeCursor(root);
    }

    public void add(final int depth, final SpanAlign spanAlign) {
        if (depth < MIN_DEPTH || depth == ROOT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth + ", node=" + prev);
        }

        if (depth == LEVEL_DEPTH || depth == prev.getDepth()) {
            // validate
            if (prev.isRoot()) {
                throw new IllegalArgumentException("invalid depth. depth=" + depth + ", node=" + prev);
            }

            CallTreeNode sibling = findLastSibling(prev);
            sibling.setSibling(spanAlign);
            prev = sibling.getSibling();
            return;
        }

        // greater
        if (depth > prev.getDepth()) {
            // validate
            if (depth > prev.getDepth() + 1) {
                throw new IllegalArgumentException("invalid depth. depth=" + depth + ", node=" + prev);
            }

            if (!prev.hasChild()) {
                prev.setChild(spanAlign);
                prev = prev.getChild();
                return;
            }

            CallTreeNode sibling = findLastSibling(prev.getChild());
            sibling.setSibling(spanAlign);
            prev = sibling.getSibling();
            return;
        }

        // lesser
        if (prev.getDepth() - depth <= ROOT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth + ", node=" + prev);
        }

        final CallTreeNode node = findUpperLevelLastSibling(depth, prev);
        node.setSibling(spanAlign);
        prev = node.getSibling();
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

    public void add(final CallTree tree) {
        final CallTreeNode node = tree.getRoot();
        if (!prev.hasChild()) {
            node.setParent(prev);
            prev.setChild(node);
            return;
        }

        CallTreeNode sibling = findLastSibling(prev.getChild());
        node.setParent(sibling.getParent());
        sibling.setSibling(node);
    }
}