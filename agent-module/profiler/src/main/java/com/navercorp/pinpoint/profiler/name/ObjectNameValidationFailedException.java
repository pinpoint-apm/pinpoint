package com.navercorp.pinpoint.profiler.name;

public class ObjectNameValidationFailedException extends IllegalArgumentException {
    public ObjectNameValidationFailedException() {
    }

    public ObjectNameValidationFailedException(String s) {
        super(s);
    }

    public ObjectNameValidationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectNameValidationFailedException(Throwable cause) {
        super(cause);
    }

}
