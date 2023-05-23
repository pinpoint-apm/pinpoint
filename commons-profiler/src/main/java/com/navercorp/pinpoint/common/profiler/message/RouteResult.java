package com.navercorp.pinpoint.common.profiler.message;

public enum RouteResult {
    OK(0),
    BAD_REQUEST(200),
    EMPTY_REQUEST(201),
    NOT_SUPPORTED_REQUEST(202),
    BAD_RESPONSE(210),
    EMPTY_RESPONSE(211),
    NOT_SUPPORTED_RESPONSE(212),
    TIMEOUT(220),
    NOT_FOUND(230),
    NOT_ACCEPTABLE(240),
    NOT_SUPPORTED_SERVICE(241),
    STREAM_CREATE_ERROR(250),
    UNKNOWN(-1);

    private final int code;

    RouteResult(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
