package com.profiler.interceptor.bci;

public class NotFoundInstrumentException extends InstrumentException {

    public NotFoundInstrumentException() {
    }

    public NotFoundInstrumentException(String message) {
        super(message);
    }

    public NotFoundInstrumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundInstrumentException(Throwable cause) {
        super(cause);
    }
}
