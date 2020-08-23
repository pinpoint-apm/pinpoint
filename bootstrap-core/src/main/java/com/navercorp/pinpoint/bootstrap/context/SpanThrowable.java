package com.navercorp.pinpoint.bootstrap.context;

import java.util.Arrays;

/**
 * @author IluckySi
 * @version 2.1.0
 * @since 2020/08/23
 */
public class SpanThrowable extends Throwable {

    private String message;
    private StackTraceElement[] stackTraceElements;
    private Throwable cause;

    public String getMessage() {
        return message;
    }

    public StackTraceElement[] getStackTraceElements() {
        return stackTraceElements;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStackTraceElements(StackTraceElement[] stackTraceElements) {
        this.stackTraceElements = stackTraceElements;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Override public String toString() {
        return "SpanThrowable{" +
            "message='" + message + '\'' +
            ", stackTraceElements=" + Arrays.toString(stackTraceElements) +
            ", cause=" + cause +
            '}';
    }
}
