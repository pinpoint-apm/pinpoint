package com.nhn.pinpoint.bootstrap;

import com.nhn.pinpoint.exception.PinPointException;

/**
 *
 */
public class BootStrapException extends PinPointException {

    public BootStrapException(String message, Throwable cause) {
        super(message, cause);
    }

    public BootStrapException(String message) {
        super(message);
    }
}
