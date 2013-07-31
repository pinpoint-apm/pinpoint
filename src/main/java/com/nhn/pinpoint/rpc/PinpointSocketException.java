package com.nhn.pinpoint.rpc;

/**
 *
 */
public class PinpointSocketException extends RuntimeException {
    public PinpointSocketException() {
    }

    public PinpointSocketException(String message) {
        super(message);
    }

    public PinpointSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public PinpointSocketException(Throwable cause) {
        super(cause);
    }
}
