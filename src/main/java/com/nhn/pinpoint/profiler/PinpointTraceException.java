package com.nhn.pinpoint.profiler;

/**
 *
 */
public class PinpointTraceException extends RuntimeException {

    public PinpointTraceException() {
    }

    public PinpointTraceException(String message) {
        super(message);
    }

    public PinpointTraceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PinpointTraceException(Throwable cause) {
        super(cause);
    }
}
