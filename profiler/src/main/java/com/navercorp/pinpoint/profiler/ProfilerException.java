package com.nhn.pinpoint.profiler;

public class ProfilerException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -4734390009820991000L;

    public ProfilerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfilerException(String message) {
        super(message);
    }

    public ProfilerException(Throwable cause) {
        super(cause);
    }
}
