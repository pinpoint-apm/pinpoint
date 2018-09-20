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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.AsyncId;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.compress.SpanPostProcessor;
import com.navercorp.pinpoint.profiler.context.compress.Context;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.util.AnnotationValueMapper;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TAnnotationValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.thrift.TBase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanThriftMessageConverter implements MessageConverter<TBase<?, ?>> {

    private final String agentId;
    private final String applicationName;
    private final long agentStartTime;
    private final short applicationServiceType;
    private final TransactionIdEncoder transactionIdEncoder;
    private final SpanPostProcessor<Context> spanPostProcessor;

    public SpanThriftMessageConverter(String applicationName, String agentId, long agentStartTime, short applicationServiceType,
                                      TransactionIdEncoder transactionIdEncoder, SpanPostProcessor<Context> spanPostProcessor) {
        this.applicationName = Assert.requireNonNull(applicationName, "applicationName must not be null");
        this.agentId = Assert.requireNonNull(agentId, "agentId must not be null");
        this.agentStartTime = agentStartTime;
        this.applicationServiceType = applicationServiceType;
        this.transactionIdEncoder = Assert.requireNonNull(transactionIdEncoder, "transactionIdEncoder must not be null");
        this.spanPostProcessor = Assert.requireNonNull(spanPostProcessor, "spanPostProcessor must not be null");

    }


    @Override
    public TBase<?, ?> toMessage(Object message) {
        if (message instanceof SpanChunk) {
            final SpanChunk spanChunk = (SpanChunk) message;
            final TSpanChunk tSpanChunk = buildSpanChunk(spanChunk);
            return tSpanChunk;
        }
        if (message instanceof Span) {
            final Span span = (Span) message;

            return buildSpan(span);
        }
        return null;
    }


    private TSpan buildSpan(Span span) {
        final TSpan tSpan = new TSpan();

//        tSpan.setVersion(span.getVersion());

        tSpan.setApplicationName(applicationName);
        tSpan.setAgentId(agentId);
        tSpan.setAgentStartTime(agentStartTime);
        tSpan.setApplicationServiceType(applicationServiceType);

        final TraceRoot traceRoot = span.getTraceRoot();
        final TraceId traceId = traceRoot.getTraceId();
        final ByteBuffer transactionId = transactionIdEncoder.encodeTransactionId(traceId);
        tSpan.setTransactionId(transactionId);
        tSpan.setSpanId(traceId.getSpanId());
        tSpan.setParentSpanId(traceId.getParentSpanId());

        tSpan.setStartTime(span.getStartTime());
        tSpan.setElapsed(span.getElapsed());
        tSpan.setServiceType(span.getServiceType());

        tSpan.setRemoteAddr(span.getRemoteAddr());

        final Shared shared = traceRoot.getShared();
        tSpan.setRpc(shared.getRpcName());
        tSpan.setEndPoint(shared.getEndPoint());
        tSpan.setFlag(traceId.getFlags());
        tSpan.setErr(shared.getErrorCode());

        tSpan.setParentApplicationName(span.getParentApplicationName());
        tSpan.setParentApplicationType(span.getParentApplicationType());
        tSpan.setAcceptorHost(span.getAcceptorHost());

        tSpan.setApiId(span.getApiId());

        final IntStringValue exceptionInfo = span.getExceptionInfo();
        if (exceptionInfo != null) {
            TIntStringValue tIntStringValue = buildTIntStringValue(exceptionInfo);
            tSpan.setExceptionInfo(tIntStringValue);
        }

        tSpan.setLoggingTransactionInfo(shared.getLoggingInfo());

        final List<Annotation> annotations = span.getAnnotations();
        if (CollectionUtils.hasLength(annotations)) {
            final List<TAnnotation> tAnnotations = buildTAnnotation(annotations);
            tSpan.setAnnotations(tAnnotations);
        }

        final List<SpanEvent> spanEventList = span.getSpanEventList();
        if (CollectionUtils.hasLength(spanEventList)) {
            final Context context = spanPostProcessor.newContext(span, tSpan);
            final List<TSpanEvent> tSpanEvents = buildTSpanEventList(spanEventList, context);
            tSpan.setSpanEventList(tSpanEvents);
        }
        return tSpan;

    }

    private List<TSpanEvent> buildTSpanEventList(List<SpanEvent> spanEventList, Context context) {
        final int eventSize = spanEventList.size();
        final List<TSpanEvent> tSpanEventList = new ArrayList<TSpanEvent>(eventSize);
        for (SpanEvent spanEvent : spanEventList) {
            final TSpanEvent tSpanEvent = buildTSpanEvent(spanEvent, context);
            context.next();
            tSpanEventList.add(tSpanEvent);
        }
        return tSpanEventList;
    }

    private TSpanChunk buildSpanChunk(SpanChunk spanChunk) {
        final TSpanChunk tSpanChunk = new TSpanChunk();

        tSpanChunk.setApplicationName(applicationName);
        tSpanChunk.setAgentId(agentId);
        tSpanChunk.setAgentStartTime(agentStartTime);
        tSpanChunk.setApplicationServiceType(applicationServiceType);

        final TraceRoot traceRoot = spanChunk.getTraceRoot();
        final TraceId traceId = traceRoot.getTraceId();
        final ByteBuffer transactionId = transactionIdEncoder.encodeTransactionId(traceId);
        tSpanChunk.setTransactionId(transactionId);

        tSpanChunk.setSpanId(traceId.getSpanId());

        final Shared shared = traceRoot.getShared();
        tSpanChunk.setEndPoint(shared.getEndPoint());

        final List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (CollectionUtils.hasLength(spanEventList)) {
            final Context context = spanPostProcessor.newContext(spanChunk, tSpanChunk);
            final List<TSpanEvent> tSpanEvents = buildTSpanEventList(spanEventList, context);
            tSpanChunk.setSpanEventList(tSpanEvents);
            context.finish();
        }

        return tSpanChunk;
    }

    private TSpanEvent buildTSpanEvent(SpanEvent spanEvent, Context context) {
        final TSpanEvent tSpanEvent = new TSpanEvent();

//        if (spanEvent.getStartElapsed() != 0) {
//          tSpanEvent.setStartElapsed(spanEvent.getStartElapsed());
//        }
//        tSpanEvent.setStartElapsed(spanEvent.getStartElapsed());
        if (spanEvent.getEndElapsed() != 0) {
            tSpanEvent.setEndElapsed(spanEvent.getEndElapsed());
        }
        tSpanEvent.setSequence(spanEvent.getSequence());
        tSpanEvent.setRpc(spanEvent.getRpc());
        tSpanEvent.setServiceType(spanEvent.getServiceType());
        tSpanEvent.setEndPoint(spanEvent.getEndPoint());

        //        tSpanEvent.setAnnotations();
        if (spanEvent.getDepth() != -1) {
            tSpanEvent.setDepth(spanEvent.getDepth());
        }
        if (spanEvent.getNextSpanId() == -1) {
            tSpanEvent.setNextSpanId(spanEvent.getNextSpanId());
        }

         tSpanEvent.setDestinationId(spanEvent.getDestinationId());
         tSpanEvent.setApiId(spanEvent.getApiId());

        final IntStringValue exceptionInfo = spanEvent.getExceptionInfo();
        if (exceptionInfo != null) {
            TIntStringValue tIntStringValue = buildTIntStringValue(exceptionInfo);
            tSpanEvent.setExceptionInfo(tIntStringValue);
        }


        final AsyncId asyncIdObject = spanEvent.getAsyncIdObject();
        if (asyncIdObject != null) {
            tSpanEvent.setNextAsyncId(asyncIdObject.getAsyncId());
        }
        final LocalAsyncId localAsyncId = spanEvent.getLocalAsyncId();
        if (localAsyncId != null) {
            tSpanEvent.setAsyncId(localAsyncId.getAsyncId());
            tSpanEvent.setAsyncSequence(localAsyncId.getSequence());
        }

        final List<Annotation> annotations = spanEvent.getAnnotations();
        if (CollectionUtils.hasLength(annotations)) {
            final List<TAnnotation> tAnnotations = buildTAnnotation(annotations);
            tSpanEvent.setAnnotations(tAnnotations);
        }

        this.spanPostProcessor.postProcess(spanEvent, tSpanEvent, context);
        return tSpanEvent;
    }

    private TIntStringValue buildTIntStringValue(IntStringValue exceptionInfo) {
        TIntStringValue tIntStringValue = new TIntStringValue(exceptionInfo.getIntValue());
        final String stringValue = exceptionInfo.getStringValue();
        if (stringValue != null) {
            tIntStringValue.setStringValue(stringValue);
        }
        return tIntStringValue;
    }

    private List<TAnnotation> buildTAnnotation(List<Annotation> annotations) {
        final List<TAnnotation> tAnnotations = new ArrayList<TAnnotation>();
        for (Annotation annotation : annotations) {
            final TAnnotation tAnnotation = new TAnnotation(annotation.getAnnotationKey());
            final TAnnotationValue tAnnotationValue = AnnotationValueMapper.buildTAnnotationValue(annotation.getValue());
            if (tAnnotationValue != null) {
                tAnnotation.setValue(tAnnotationValue);
            }
        }
        return tAnnotations;
    }

    @Override
    public String toString() {
        return "SpanThriftMessageConverter{" +
                "agentId='" + agentId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", applicationServiceType=" + applicationServiceType +
                '}';
    }
}
