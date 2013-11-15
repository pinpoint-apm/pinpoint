package com.nhn.pinpoint.web.service;

/**
 * @author emeroad
 */
public class ComplexNodeId implements NodeId {
    private final Node src;
    private final Node dest;

    public ComplexNodeId(Node src, Node dest) {
        if (src == null) {
            throw new NullPointerException("src must not be null");
        }
        if (dest == null) {
            throw new NullPointerException("dest must not be null");
        }
        this.src = src;
        this.dest = dest;
    }

    public Node getSrc() {
        return src;
    }


    public Node getDest() {
        return dest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplexNodeId that = (ComplexNodeId) o;

        if (!dest.equals(that.dest)) return false;
        if (!src.equals(that.src)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = src.hashCode();
        result = 31 * result + dest.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ComplexNodeId{");
        sb.append("src=").append(src);
        sb.append(", dest=").append(dest);
        sb.append('}');
        return sb.toString();
    }
}
