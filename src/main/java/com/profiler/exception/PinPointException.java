package com.profiler.exception;

/**
 *
 */
public class PinPointException extends RuntimeException {
    public PinPointException() {
    }

    public PinPointException(String message) {
        super(message);
    }

    public PinPointException(String message, Throwable cause) {
        super(message, cause);
    }

    public PinPointException(Throwable cause) {
        super(cause);
    }
}
