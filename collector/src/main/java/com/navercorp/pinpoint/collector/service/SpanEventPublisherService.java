package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.event.SpanChunkInsertionEvent;
import com.navercorp.pinpoint.common.server.event.SpanInsertionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class SpanEventPublisherService {
    private final ApplicationEventPublisher publisher;

    public SpanEventPublisherService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishSpanInsertion(SpanBo spanBo, boolean success) {
        publisher.publishEvent(new SpanInsertionEvent(spanBo, success));
    }

    public void publishSpanChunkInsertion(SpanChunkBo spanChunkBo, boolean success) {
        publisher.publishEvent(new SpanChunkInsertionEvent(spanChunkBo, success));
    }
}
