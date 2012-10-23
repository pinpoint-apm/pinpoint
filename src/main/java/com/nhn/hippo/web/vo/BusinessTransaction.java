package com.nhn.hippo.web.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;

public class BusinessTransaction {
    private final List<Trace> traces = new ArrayList<Trace>();
    private final String name;

    private int calls = 0;
    private long totalTime = 0;
    private long maxTime = 0;
    private long minTime = 0;

    public BusinessTransaction(SpanBo span) {
        this.name = span.getName();

        List<AnnotationBo> annotations = span.getAnnotationBoList();
        long begin = 0;
        long end = 0;
        for (AnnotationBo a : annotations) {
            if (a.getKey().equals("SR") || a.getKey().equals("CS")) {
                begin = a.getTimestamp();
            }
            if (a.getKey().equals("SS") || a.getKey().equals("CR")) {
                end = a.getTimestamp();
            }
        }
        long elapsed = end - begin;
        totalTime = maxTime = minTime = elapsed;
        
        this.traces.add(new Trace(new UUID(span.getMostTraceId(), span.getLeastTraceId()).toString(), elapsed, span.getTimestamp()));
        calls++;
    }

    public void add(SpanBo span) {
        List<AnnotationBo> annotations = span.getAnnotationBoList();
        long begin = 0;
        long end = 0;
        for (AnnotationBo a : annotations) {
            if (a.getKey().equals("SR") || a.getKey().equals("CS")) {
                begin = a.getTimestamp();
            }
            if (a.getKey().equals("SS") || a.getKey().equals("CR")) {
                end = a.getTimestamp();
            }
        }
        long elapsed = end - begin;
        totalTime += elapsed;
        if (maxTime < elapsed)
            maxTime = elapsed;
        if (minTime > elapsed)
            minTime = elapsed;
        
        this.traces.add(new Trace(new UUID(span.getMostTraceId(), span.getLeastTraceId()).toString(), elapsed, span.getTimestamp()));
        
        if (span.getParentSpanId() == -1) {
            calls++;
        }
    }

    public String getName() {
        return name;
    }

    public List<Trace> getTraces() {
        return traces;
    }

    public int getCalls() {
        return calls;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public long getMinTime() {
        return minTime;
    }
}
