package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.thrift.dto.TAgentKey;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class SpanEvent extends TSpanEvent implements Thriftable {

    private final Span span;

    public SpanEvent(Span span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }

    public void addAnnotation(Annotation annotation) {
        this.addToAnnotations(annotation);
    }


    public void markStartTime() {
//        spanEvent.setStartElapsed((int) (startTime - parentSpanStartTime));
        final int startElapsed = (int)(System.currentTimeMillis() - span.getStartTime());
        this.setStartElapsed(startElapsed);
    }

    public long getStartTime() {
        return span.getStartTime() + getStartElapsed();
    }

    public void markAfterTime() {
        if (!isSetStartElapsed()) {
            throw new PinpointTraceException("startTime is not set");
        }
//        spanEvent.setEndElapsed((int) (endTime - startTime));
        final int endElapsed = (int)(System.currentTimeMillis() - getStartTime());
        this.setEndElapsed(endElapsed);
    }

    public long getAfterTime() {
        if (!isSetStartElapsed()) {
            throw new PinpointTraceException("startTime is not set");
        }
        return span.getStartTime() + getStartElapsed() + getEndElapsed();
    }


    public TSpanEvent toThrift() {

        // Span내부의 SpanEvent로 들어가지 않을 경우
        final TAgentKey tAgentKey = DefaultAgent.getInstance().getTAgentKey();
        this.setAgentKey(tAgentKey);

        // span 데이터 셋은 child일때만 한다.
        this.setParentServiceType(span.getServiceType()); // added
        this.setParentEndPoint(span.getEndPoint()); // added

        this.setTraceAgentId(span.getTraceAgentId());
        this.setTraceAgentStartTime(span.getTraceAgentStartTime());
        this.setTraceTransactionSequence(span.getTraceTransactionSequence());
        this.setSpanId(span.getSpanId());

        return this;
    }


}
