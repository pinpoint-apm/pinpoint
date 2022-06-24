/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.DefaultAsyncId;
import com.navercorp.pinpoint.profiler.context.DefaultSpanChunk;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessorV1;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.util.RandomExUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanThriftMessageConverterTest {

    private static final String APPLICATION_NAME = "app";
    private static final String AGENT_ID = "agent";
    private static final long AGENT_START_TIME = System.currentTimeMillis();

    private final TransactionIdEncoder transactionIdEncoder = new DefaultTransactionIdEncoder(AGENT_ID, AGENT_START_TIME);

    private SpanProcessorV1 spanPostProcessor = new SpanProcessorV1();

    private final SpanThriftMessageConverter messageConverter = new SpanThriftMessageConverter(
            APPLICATION_NAME,
            AGENT_ID,
            AGENT_START_TIME,
            ServiceType.STAND_ALONE.getCode(),
            transactionIdEncoder,
            spanPostProcessor
    );


    private Span newSpan() {
        final TraceId traceId = new DefaultTraceId(AGENT_ID, AGENT_START_TIME, 1L);
        final TraceRoot traceRoot = new DefaultTraceRoot(traceId, AGENT_ID, AGENT_START_TIME, 100L);
        return new Span(traceRoot);
    }


    @Test
    public void buildTSpan() {
        final Span span = newSpan();

        span.setStartTime(System.currentTimeMillis());
        span.setElapsedTime(RandomExUtils.nextInt(0, 100));
        span.setAcceptorHost("acceptorHost");
        span.setExceptionInfo(new IntStringValue(RandomExUtils.nextInt(0, 100), "error"));
        span.setApiId(RandomExUtils.nextInt(0, 100));
        span.setServiceType((short) RandomExUtils.nextInt(0, 100));
        span.setRemoteAddr("remoteAddr");
        span.setParentApplicationName("pApp");
        span.setParentApplicationType((short) RandomExUtils.nextInt(0, 100));

        final TraceRoot traceRoot = span.getTraceRoot();
        Shared shared = traceRoot.getShared();
        shared.setEndPoint("endPoint");
        shared.setRpcName("rpcName");
        shared.setLoggingInfo((byte) RandomExUtils.nextInt(0, 10));
        shared.maskErrorCode(RandomExUtils.nextInt(0, 100));
        shared.setStatusCode(RandomExUtils.nextInt(0, 100));

        span.addAnnotation(Annotations.of(1));
        span.setSpanEventList(Collections.singletonList(new SpanEvent()));

        final TSpan tSpan = messageConverter.buildTSpan(span);


        Assertions.assertEquals(span.getStartTime(), tSpan.getStartTime());
        Assertions.assertEquals(span.getElapsedTime(), tSpan.getElapsed());
        Assertions.assertEquals(span.getAcceptorHost(), tSpan.getAcceptorHost());
        Assertions.assertEquals(span.getExceptionInfo().getIntValue(), tSpan.getExceptionInfo().getIntValue());
        Assertions.assertEquals(span.getExceptionInfo().getStringValue(), tSpan.getExceptionInfo().getStringValue());
        Assertions.assertEquals(span.getApiId(), tSpan.getApiId());
        Assertions.assertEquals(span.getServiceType(), tSpan.getServiceType());
        Assertions.assertEquals(span.getRemoteAddr(), tSpan.getRemoteAddr());
        Assertions.assertEquals(span.getParentApplicationName(), tSpan.getParentApplicationName());
        Assertions.assertEquals(span.getParentApplicationType(), tSpan.getParentApplicationType());

        Assertions.assertEquals(traceRoot.getTraceId().getSpanId(), tSpan.getSpanId());
        Assertions.assertEquals(traceRoot.getShared().getEndPoint(), tSpan.getEndPoint());
        Assertions.assertEquals(traceRoot.getShared().getRpcName(), tSpan.getRpc());
        Assertions.assertEquals(traceRoot.getShared().getLoggingInfo(), tSpan.getLoggingTransactionInfo());
        Assertions.assertEquals(traceRoot.getShared().getErrorCode(), tSpan.getErr());
// TODO
//        Assertions.assertEquals(traceRoot.getShared().getStatusCode(),  );

        Assertions.assertEquals(span.getAnnotations().size(), tSpan.getAnnotations().size());
        Assertions.assertEquals(span.getSpanEventList().size(), tSpan.getSpanEventList().size());
    }


    private SpanChunk newSpanChunk() {
        final TraceId traceId = new DefaultTraceId(AGENT_ID, AGENT_START_TIME, 1L);
        final TraceRoot traceRoot = new DefaultTraceRoot(traceId, AGENT_ID, AGENT_START_TIME, 100L);
        return new DefaultSpanChunk(traceRoot, Arrays.asList(new SpanEvent()));
    }


    @Test
    public void buildTSpanChunk() {
        final SpanChunk spanChunk = newSpanChunk();
        TraceRoot traceRoot = spanChunk.getTraceRoot();

        TSpanChunk tSpanChunk = messageConverter.buildTSpanChunk(spanChunk);

        Assertions.assertEquals(traceRoot.getTraceId().getSpanId(), tSpanChunk.getSpanId());
        Assertions.assertEquals(traceRoot.getShared().getEndPoint(), tSpanChunk.getEndPoint());
    }


    @Test
    public void buildTSpanEvent() {
        final long startTime = System.currentTimeMillis() - 100;

        SpanEvent spanEvent = new SpanEvent();
        spanEvent.setDepth(RandomExUtils.nextInt(0, 100));
        spanEvent.setStartTime(startTime + RandomExUtils.nextInt(0, 100));
        spanEvent.setAfterTime(spanEvent.getStartTime() + RandomExUtils.nextInt(5, 100));
        spanEvent.setDestinationId("destinationId");
        spanEvent.setSequence(RandomExUtils.nextInt(0, 100));
        spanEvent.setNextSpanId(RandomExUtils.nextInt(0, 100));

        spanEvent.setAsyncIdObject(new DefaultAsyncId(RandomExUtils.nextInt(0, 100)));


        spanEvent.addAnnotation(Annotations.of(1));

        TSpanEvent tSpanEvent = messageConverter.buildTSpanEvent(spanEvent);
        spanPostProcessor.postEventProcess(Collections.singletonList(spanEvent), Collections.singletonList(tSpanEvent), startTime);

        Assertions.assertEquals(spanEvent.getDepth(), tSpanEvent.getDepth());
        Assertions.assertEquals(spanEvent.getStartTime(), startTime + tSpanEvent.getStartElapsed());
        Assertions.assertEquals(spanEvent.getAfterTime(), startTime + tSpanEvent.getStartElapsed() + tSpanEvent.getEndElapsed());
        Assertions.assertEquals(spanEvent.getDestinationId(), tSpanEvent.getDestinationId());
        Assertions.assertEquals(spanEvent.getSequence(), tSpanEvent.getSequence());
        Assertions.assertEquals(spanEvent.getNextSpanId(), tSpanEvent.getNextSpanId());

        Assertions.assertEquals(spanEvent.getAsyncIdObject().getAsyncId(), tSpanEvent.getNextAsyncId());

        Assertions.assertEquals(spanEvent.getAnnotations().size(), tSpanEvent.getAnnotations().size());
    }


    @Test
    public void buildTAnnotation() {
        Annotation<?> annotation = Annotations.of(RandomExUtils.nextInt(0, 100), "value");
        List<? extends Annotation<?>> annotations = Collections.singletonList(annotation);
        List<TAnnotation> tAnnotations = messageConverter.buildTAnnotation(annotations);

        TAnnotation tAnnotation = tAnnotations.get(0);
        Assertions.assertEquals(annotation.getKey(), tAnnotation.getKey());
        Assertions.assertEquals(annotation.getValue(), tAnnotation.getValue().getStringValue());
    }


}