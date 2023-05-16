package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class SuccessResponse implements Response {
    private final String result;
    private final String message;

    public static Response ok() {
        return new SuccessResponse("SUCCESS");
    }

    public static Response ok(String message) {
        return new SuccessResponse("SUCCESS", message);
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
