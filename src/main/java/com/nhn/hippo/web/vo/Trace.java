package com.nhn.hippo.web.vo;

public class Trace {

    private final String traceId;
    private final long executionTime;
    private final long startTime;

    public Trace(String traceId, long executionTime, long startTime) {
        this.traceId = traceId;
        this.executionTime = executionTime;
        this.startTime = startTime;
    }

    public String getTraceId() {
        return traceId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public long getStartTime() {
        return startTime;
    }
}
