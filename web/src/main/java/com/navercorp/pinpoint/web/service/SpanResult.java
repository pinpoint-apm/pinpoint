package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.calltree.span.SpanAligner2;

import java.util.List;

/**
 * @author emeroad
 */
public class SpanResult {
    private int completeType;
    private List<SpanAlign> spanAlignList;

    public SpanResult(int completeType, List<SpanAlign> spanAlignList) {
        if (spanAlignList == null) {
            throw new NullPointerException("spanAlignList must not be null");
        }
        this.completeType = completeType;
        this.spanAlignList = spanAlignList;
    }



    public int getCompleteType() {
        return completeType;
    }

    public List<SpanAlign> getSpanAlignList() {
        return spanAlignList;
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
