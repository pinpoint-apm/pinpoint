package com.navercorp.pinpoint.io.request;

public class UidException extends RuntimeException {

    public UidException(String message) {
        super(message, null, false, false);
    }

    public UidException(String message, Throwable cause) {
        super(message, cause, false, false);
    }

}
