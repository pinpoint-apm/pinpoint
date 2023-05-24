package com.navercorp.pinpoint.common.profiler.message;

public class DefaultResultResponse implements ResultResponse {
    private final boolean success;
    private final String message;

    public DefaultResultResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
