package com.profiler.bootstrap;

/**
 *
 */
public class BootStrapException extends RuntimeException {

    public BootStrapException(String message, Throwable cause) {
        super(message, cause);
    }

    public BootStrapException(String message) {
        super(message);
    }
}
