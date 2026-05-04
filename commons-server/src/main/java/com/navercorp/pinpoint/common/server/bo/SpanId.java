package com.navercorp.pinpoint.common.server.bo;

public class SpanId {
    /**
     * Sentinel for an unspecified spanId. As parentSpanId, denotes a root span.
     */
    public static final long NULL = -1;

    public static final String NULL_STRING = "" + NULL;
}
