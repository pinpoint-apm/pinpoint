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
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessorV1;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

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
        span.setElapsedTime(RandomUtils.nextInt(0, 100));
        span.setAcceptorHost("acceptorHost");
        span.setExceptionInfo(new IntStringValue(RandomUtils.nextInt(0, 100), "error"));
        span.setApiId(RandomUtils.nextInt(0, 100));
        span.setServiceType((short) RandomUtils.nextInt(0, 100));
        span.setRemoteAddr("remoteAddr");
        span.setParentApplicationName("pApp");
        span.setParentApplicationType((short) RandomUtils.nextInt(0, 100));

        final TraceRoot traceRoot = span.getTraceRoot();
        Shared shared = traceRoot.getShared();
        shared.setEndPoint("endPoint");
        shared.setRpcName("rpcName");
        shared.setLoggingInfo((byte) RandomUtils.nextInt(0, 10));
        shared.maskErrorCode(RandomUtils.nextInt(0, 100));
        shared.setStatusCode(RandomUtils.nextInt(0, 100));

        span.addAnnotation(new Annotation(1));
        span.setSpanEventList(Collections.singletonList(new SpanEvent()));

        final TSpan tSpan = messageConverter.buildTSpan(span);


        Assert.assertEquals(span.getStartTime(), tSpan.getStartTime());
        Assert.assertEquals(span.getElapsedTime(), tSpan.getElapsed());
        Assert.assertEquals(span.getAcceptorHost(), tSpan.getAcceptorHost());
        Assert.assertEquals(span.getExceptionInfo().getIntValue(), tSpan.getExceptionInfo().getIntValue());
        Assert.assertEquals(span.getExceptionInfo().getStringValue(), tSpan.getExceptionInfo().getStringValue());
        Assert.assertEquals(span.getApiId(), tSpan.getApiId());
        Assert.assertEquals(span.getServiceType(), tSpan.getServiceType());
        Assert.assertEquals(span.getRemoteAddr(), tSpan.getRemoteAddr());
        Assert.assertEquals(span.getParentApplicationName(), tSpan.getParentApplicationName());
        Assert.assertEquals(span.getParentApplicationType(), tSpan.getParentApplicationType());

        Assert.assertEquals(traceRoot.getTraceId().getSpanId(), tSpan.getSpanId());
        Assert.assertEquals(traceRoot.getShared().getEndPoint(), tSpan.getEndPoint());
        Assert.assertEquals(traceRoot.getShared().getRpcName(), tSpan.getRpc());
        Assert.assertEquals(traceRoot.getShared().getLoggingInfo(), tSpan.getLoggingTransactionInfo());
        Assert.assertEquals(traceRoot.getShared().getErrorCode(), tSpan.getErr());
// TODO
//        Assert.assertEquals(traceRoot.getShared().getStatusCode(),  );

        Assert.assertEquals(span.getAnnotations().size(), tSpan.getAnnotations().size());
        Assert.assertEquals(span.getSpanEventList().size(), tSpan.getSpanEventList().size());
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

        Assert.assertEquals(traceRoot.getTraceId().getSpanId(), tSpanChunk.getSpanId());
        Assert.assertEquals(traceRoot.getShared().getEndPoint(), tSpanChunk.getEndPoint());
    }


    @Test
    public void buildTSpanEvent() {
        final long startTime = System.currentTimeMillis() - 100;

        SpanEvent spanEvent = new SpanEvent();
        spanEvent.setDepth(RandomUtils.nextInt(0, 100));
        spanEvent.setStartTime(startTime + RandomUtils.nextInt(0, 100));
        spanEvent.setAfterTime(spanEvent.getStartTime() + RandomUtils.nextInt(5, 100));
        spanEvent.setDestinationId("destinationId");
        spanEvent.setSequence((short) RandomUtils.nextInt(0, 100));
        spanEvent.setNextSpanId(RandomUtils.nextInt(0, 100));

        spanEvent.setAsyncIdObject(new DefaultAsyncId(RandomUtils.nextInt(0, 100)));


        spanEvent.addAnnotation(new Annotation(1));

        TSpanEvent tSpanEvent = messageConverter.buildTSpanEvent(spanEvent);
        spanPostProcessor.postEventProcess(Collections.singletonList(spanEvent), Collections.singletonList(tSpanEvent), startTime);

        Assert.assertEquals(spanEvent.getDepth(), tSpanEvent.getDepth());
        Assert.assertEquals(spanEvent.getStartTime(), startTime + tSpanEvent.getStartElapsed());
        Assert.assertEquals(spanEvent.getAfterTime(), startTime + tSpanEvent.getStartElapsed() + tSpanEvent.getEndElapsed());
        Assert.assertEquals(spanEvent.getDestinationId(), tSpanEvent.getDestinationId());
        Assert.assertEquals(spanEvent.getSequence(), tSpanEvent.getSequence());
        Assert.assertEquals(spanEvent.getNextSpanId(), tSpanEvent.getNextSpanId());

        Assert.assertEquals(spanEvent.getAsyncIdObject().getAsyncId(), tSpanEvent.getNextAsyncId());

        Assert.assertEquals(spanEvent.getAnnotations().size(), tSpanEvent.getAnnotations().size());
    }


    @Test
    public void buildTAnnotation() {
        Annotation annotation = new Annotation(RandomUtils.nextInt(0, 100), "value");
        List<Annotation> annotations = Collections.singletonList(annotation);
        List<TAnnotation> tAnnotations = messageConverter.buildTAnnotation(annotations);

        TAnnotation tAnnotation = tAnnotations.get(0);
        Assert.assertEquals(annotation.getAnnotationKey(), tAnnotation.getKey());
        Assert.assertEquals(annotation.getValue(), tAnnotation.getValue().getStringValue());
    }


}