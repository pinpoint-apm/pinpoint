package com.navercorp.pinpoint.web.vo;

import org.springframework.util.Assert;

/**
 * @author emeroad
 */
public class SearchOption {
    private final int callerSearchDepth;
    private final int calleeSearchDepth;

    public SearchOption(int callerSearchDepth, int calleeSearchDepth) {
        Assert.isTrue(callerSearchDepth >= 0, "negative callerSearchDepth");
        Assert.isTrue(calleeSearchDepth >= 0, "negative calleeSearchDepth");
        this.callerSearchDepth = callerSearchDepth;
        this.calleeSearchDepth = calleeSearchDepth;
    }

    public int getCallerSearchDepth() {
        return callerSearchDepth;
    }

    public int getCalleeSearchDepth() {
        return calleeSearchDepth;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchOption{");
        sb.append("callerSearchDepth=").append(callerSearchDepth);
        sb.append(", calleeSearchDepth=").append(calleeSearchDepth);
        sb.append('}');
        return sb.toString();
    }
}