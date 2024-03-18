package com.navercorp.pinpoint.profiler.monitor.metric.uri;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class URIKey {
    private final String uri;
    private final long timestamp;

    public URIKey(String uri, long timestamp) {
        this.uri = uri;
        this.timestamp = timestamp;
    }

    public String getUri() {
        return uri;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URIKey uriKey = (URIKey) o;

        if (timestamp != uriKey.timestamp) return false;
        return Objects.equals(uri, uriKey.uri);
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "URIKey{" +
                "uri='" + uri + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
