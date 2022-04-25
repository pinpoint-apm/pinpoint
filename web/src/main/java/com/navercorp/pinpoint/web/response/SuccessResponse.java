package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

public class SuccessResponse implements Response {
    private final String result;
    private final String message;

    public static ResponseEntity<Response> ok() {
        return ResponseEntity.ok(new SuccessResponse("SUCCESS"));
    }

    public static ResponseEntity<Response> ok(String message) {
        return ResponseEntity.ok(new SuccessResponse("SUCCESS", message));
    }

    public SuccessResponse(String result) {
        this(result, null);
    }

    public SuccessResponse(String result, String message) {
        this.result = Objects.requireNonNull(result, "result");
        this.message = message;

    }

    public String getResult() {
        return result;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMessage() {
        return message;
    }
}
