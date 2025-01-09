package com.navercorp.pinpoint.realtime.collector.controller;

public class ClusterException extends RuntimeException {
    public ClusterException(String message) {
        super(message);
    }

    public ClusterException(Throwable throwable) {
        super(throwable);
    }

    public ClusterException(String message, Throwable cause) {
        super(message, cause);
    }
}
