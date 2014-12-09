package com.navercorp.pinpoint.profiler.context;


import java.util.List;

import com.navercorp.pinpoint.thrift.dto.TSpanChunk;

/**
 * @author emeroad
 */
public class SpanChunk extends TSpanChunk {

    public SpanChunk(List<SpanEvent> spanEventList) {
        if (spanEventList == null) {
            throw new NullPointerException("spanEventList must not be null");
        }
        setSpanEventList((List) spanEventList);
    }
}
