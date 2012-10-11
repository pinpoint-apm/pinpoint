package com.nhn.hippo.web.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.profiler.common.dto.thrift.Span;

public class BusinessTransaction {
    private final List<String> traces = new ArrayList<String>();
    private final String name;

    private int calls = 0;
    private int meanTime = 0;
    private int maxTime = 0;
    private int minTime = 0;

    public BusinessTransaction(Span span) {
        this.name = span.getName();
        this.traces.add(new UUID(span.getMostTraceID(), span.getLeastTraceID()).toString());
        calls++;
    }

    public void add(Span span) {
        this.traces.add(new UUID(span.getMostTraceID(), span.getLeastTraceID()).toString());
        calls++;
    }

    public String getName() {
        return name;
    }

    public List<String> getTraces() {
        return traces;
    }

    public int getCalls() {
        return calls;
    }

    public int getMeanTime() {
        return meanTime;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getMinTime() {
        return minTime;
    }
}
