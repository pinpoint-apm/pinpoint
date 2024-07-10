package com.navercorp.pinpoint.common.server.response;

public enum Result {
    SUCCESS, FAIL;

    public static Result of(boolean success) {
        if (success) {
            return SUCCESS;
        }
        return FAIL;
    }
}
