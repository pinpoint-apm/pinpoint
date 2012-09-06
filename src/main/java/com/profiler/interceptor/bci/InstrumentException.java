package com.profiler.interceptor.bci;

// TODO 추후 별도 계층구조가 필요하면 분화 필요.
public class InstrumentException extends Exception {

    public InstrumentException() {
    }

    public InstrumentException(String message) {
        super(message);
    }

    public InstrumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstrumentException(Throwable cause) {
        super(cause);
    }
}
