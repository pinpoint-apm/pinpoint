/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.compress;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcSpanMessageConverter;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanAutoUriGetter;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AnnotationValueMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AnnotationValueMapperImpl;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.SpanMessageMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.SpanMessageMapperImpl;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcSpanProcessorV2Test {

    private SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanProcessorProtoV2 = new GrpcSpanProcessorV2();

    private AnnotationValueMapper annotationValueMapper = new AnnotationValueMapperImpl();
    private SpanMessageMapper mapper = new SpanMessageMapperImpl(annotationValueMapper, new SpanAutoUriGetter());
    private GrpcSpanMessageConverter converter = new GrpcSpanMessageConverter("agentId", (short) 1, spanProcessorProtoV2, mapper);

    @Test
    public void preProcess() {
        Span span = newSpan();

        SpanEventBuilder factory = new SpanEventBuilder();
        factory.addSpanEvent();
        factory.addSpanEvent();
        factory.addSpanEvent();
        List<SpanEvent> original = factory.getSpanEventList();

        factory.shuffle();
        Assertions.assertNotEquals(factory.getSpanEventList(), span.getSpanEventList());

        span.setSpanEventList(factory.getSpanEventList());
        spanProcessorProtoV2.preProcess(span, PSpan.newBuilder());

        Assertions.assertEquals(original, span.getSpanEventList());
    }

    private Span newSpan() {
        TraceId traceId = new DefaultTraceId("agent", 1, 0);
        TraceRoot traceRoot = TraceRoot.remote(traceId, "agent", 0, 3);
        return new Span(traceRoot);
    }

    @Test
    public void postProcess() {

        Span span = newSpan();

        SpanEventBuilder factory = new SpanEventBuilder();
        factory.addSpanEvent();
        factory.addSpanEvent();
        factory.addSpanEvent();
        span.setSpanEventList(factory.getSpanEventList());

        PSpan.Builder builder = PSpan.newBuilder();
        for (SpanEvent spanEvent : span.getSpanEventList()) {
            PSpanEvent pSpanEvent = mapper.map(spanEvent);
            builder.addSpanEvent(pSpanEvent);
        }

        spanProcessorProtoV2.postProcess(span, builder);
        PSpan pSpan = builder.build();

        List<PSpanEvent> pSpanEventList = pSpan.getSpanEventList();
        List<SpanEvent> spanEventList = span.getSpanEventList();
        long keyStartTime = span.getStartTime();
        for (int i = 0; i < pSpanEventList.size(); i++) {
            PSpanEvent pSpanEvent = pSpanEventList.get(i);
            SpanEvent next = spanEventList.get(i);
            long startTime = keyStartTime + pSpanEvent.getStartElapsed();
            Assertions.assertEquals(startTime, next.getStartTime());
            keyStartTime = startTime;
        }
    }

    @Test
    public void postProcess_NPE() {
        Span span = newSpan();
        PSpan.Builder builder = PSpan.newBuilder();
        spanProcessorProtoV2.postProcess(span, builder);
    }
}