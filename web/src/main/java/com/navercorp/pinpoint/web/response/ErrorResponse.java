package com.navercorp.pinpoint.web.response;

import org.springframework.http.ResponseEntity;

import java.util.Objects;

public class ErrorResponse implements Response {
    private final String errorCode;
    private final String errorMessage;

    public static ResponseEntity<Response> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("400", message));
    }
    public static ResponseEntity<Response> serverError(String message) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("500", message));
    }

    public ErrorResponse(String errorCode, String errorMessage) {
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
