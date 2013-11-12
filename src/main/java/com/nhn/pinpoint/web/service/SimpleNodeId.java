package com.nhn.pinpoint.web.service;

/**
 * @author emeroad
 */
public class SimpleNodeId implements NodeId{
    private String key;

    public SimpleNodeId(String key) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleNodeId that = (SimpleNodeId) o;

        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleNodeId{");
        sb.append("key='").append(key).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
