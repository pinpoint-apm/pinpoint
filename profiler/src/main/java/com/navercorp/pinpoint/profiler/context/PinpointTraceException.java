package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.exception.PinpointException;

/**
 * @author emeroad
 */
public class PinpointTraceException extends PinpointException {

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
