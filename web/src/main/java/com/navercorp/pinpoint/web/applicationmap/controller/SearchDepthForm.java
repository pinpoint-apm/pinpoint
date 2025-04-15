package com.navercorp.pinpoint.web.applicationmap.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SearchDepthForm {

    public static final int MIN = 1;
    public static final int MAX = 4;

    public static final int DEFAULT_SEARCH_DEPTH = MIN;

    @Min(MIN) @Max(MAX)
    private int callerRange = DEFAULT_SEARCH_DEPTH;

    @Min(MIN) @Max(MAX)
    private int calleeRange = DEFAULT_SEARCH_DEPTH;

    public int getCallerRange() {
        return callerRange;
    }

    public void setCallerRange(int callerRange) {
        this.callerRange = callerRange;
    }

    public int getCalleeRange() {
        return calleeRange;
    }

    public void setCalleeRange(int calleeRange) {
        this.calleeRange = calleeRange;
    }
}
