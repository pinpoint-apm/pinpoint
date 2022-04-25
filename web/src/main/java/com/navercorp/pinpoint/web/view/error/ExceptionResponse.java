package com.navercorp.pinpoint.web.view.error;

import com.navercorp.pinpoint.web.response.Response;

import java.util.Objects;

public class ExceptionResponse implements Response {
    private final InternalServerError exception;

    public ExceptionResponse(InternalServerError exception) {
        this.exception = Objects.requireNonNull(exception, "exception");
    }

    public InternalServerError getException() {
        return exception;
    }
}
