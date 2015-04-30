package com.navercorp.pinpoint.web.calltree.span;

public class CallTreeNode {

    private CallTreeNode parent;
    private CallTreeNode child;
    private CallTreeNode sibling;
    private final SpanAlign data;

    public CallTreeNode(final CallTreeNode parent, SpanAlign data) {
        this.parent = parent;
        this.data = data;
    }

    public void setParent(final CallTreeNode parent) {
        this.parent = parent;
    }
    
    public CallTreeNode getParent() {
        return parent;
    }

    public SpanAlign getData() {
        return data;
    }

    public void setChild(final CallTreeNode child) {
        this.child = child;
    }

    public void setChild(final SpanAlign spanAlign) {
        this.child = new CallTreeNode(this, spanAlign);
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

    public void setSibling(final SpanAlign spanAlign) {
        this.sibling = new CallTreeNode(parent, spanAlign);
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

        return parent.getDepth() + 1;
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
        builder.append(", data=");
        builder.append(data);
        builder.append("}");
        return builder.toString();
    }
}