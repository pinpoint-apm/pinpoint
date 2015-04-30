package com.navercorp.pinpoint.web.calltree.span;

public class CallTreeNodeAppender {
    private static final int MIN_DEPTH = -1;
    private static final int LEVEL_DEPTH = -1;
    private static final int ROOT_DEPTH = 0;

    private CallTreeNode last;

    public CallTreeNodeAppender(CallTreeNode last) {
        this.last = last;
    }

    public void add(final int depth, final SpanAlign spanAlign) {
        if (depth < MIN_DEPTH || depth == ROOT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth + ", node=" + last);
        }

        if (depth == LEVEL_DEPTH || depth == last.getDepth()) {
            // validate
            if (last.isRoot() || last.hasSibling()) {
                throw new IllegalArgumentException("invalid depth. depth=" + depth + ", node=" + last);
            }
            last.setSibling(spanAlign);
            last = last.getSibling();
            return;
        }

        // greater
        if (depth > last.getDepth()) {
            // validate
            if (depth > last.getDepth() + 1 || last.hasChild()) {
                throw new IllegalArgumentException("invalid depth. depth=" + depth + ", node=" + last);
            }
            last.setChild(spanAlign);
            last = last.getChild();
            return;
        }

        // lesser
        if (last.getDepth() - depth <= ROOT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth + ", node=" + last);
        }

        final CallTreeNode node = findUpperLevelLastSibling(depth, last);
        node.setSibling(spanAlign);
        last = node.getSibling();
    }

    CallTreeNode findUpperLevelLastSibling(final int level, final CallTreeNode node) {
        CallTreeNode parent = node.getParent();
        while (parent.getDepth() != level) {
            parent = parent.getParent();
        }

        CallTreeNode last = parent.getSibling();
        while (last != null) {
            last = last.getSibling();
        }

        return last;
    }

}
