package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.exception.PinpointException;

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
