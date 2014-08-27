package com.nhn.pinpoint.profiler.context;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;
import org.apache.thrift.TBase;

import com.nhn.pinpoint.profiler.DefaultAgent;

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
