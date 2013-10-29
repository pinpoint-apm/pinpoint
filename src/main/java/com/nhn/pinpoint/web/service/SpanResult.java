package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.calltree.span.SpanAligner2;

import java.util.List;

/**
 *
 */
public class SpanResult {
    private int completeType;
    private List<SpanAlign> spanAlign;
    private Object completeTypeString;

    public SpanResult(int completeType, List<SpanAlign> spanAlign) {
        if (spanAlign == null) {
            throw new NullPointerException("spanAlign must not be null");
        }
        this.completeType = completeType;
        this.spanAlign = spanAlign;
    }



    public int getCompleteType() {
        return completeType;
    }

    public List<SpanAlign> getSpanAlign() {
        return spanAlign;
    }

    public String getCompleteTypeString() {
        switch (completeType) {
            case SpanAligner2.BEST_MATCH:
                return "Complete";
            case SpanAligner2.START_TIME_MATCH:
                return "Progress";
            case SpanAligner2.FAIL_MATCH:
                return "Error";
        }
        return "Error";
    }
}
