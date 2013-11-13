package com.nhn.pinpoint.web.service;

/**
 * @author emeroad
 */
public class SimpleNodeId implements NodeId {
    private final Node node;

    public SimpleNodeId(Node node) {
        if (node == null) {
            throw new NullPointerException("key must not be null");
        }
        this.node = node;
    }

    public Node getKey() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleNodeId that = (SimpleNodeId) o;

        if (!node.equals(that.node)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleNodeId{");
        sb.append("node=").append(node);
        sb.append('}');
        return sb.toString();
    }
}
