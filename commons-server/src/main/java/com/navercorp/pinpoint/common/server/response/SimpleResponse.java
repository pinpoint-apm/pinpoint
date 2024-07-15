package com.navercorp.pinpoint.common.server.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class SimpleResponse implements Response {
    private final Result result;
    private final String message;

    public static Response ok() {
        return new SimpleResponse(Result.SUCCESS);
    }

    public static Response ok(String message) {
        return new SimpleResponse(Result.SUCCESS, message);
    }

    public SimpleResponse(Result result) {
        this(result, null);
    }

    public SimpleResponse(Result result, String message) {
        this.result = Objects.requireNonNull(result, "result");
        this.message = message;
    }

    @Override
    public Result getResult() {
        return result;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMessage() {
        return message;
    }
}
