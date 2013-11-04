package com.nhn.pinpoint.bootstrap;

import com.nhn.pinpoint.exception.PinpointException;

/**
 * @author emeroad
 */
public class BootStrapException extends PinpointException {

    public BootStrapException(String message, Throwable cause) {
        super(message, cause);
    }

    public BootStrapException(String message) {
        super(message);
    }
}
