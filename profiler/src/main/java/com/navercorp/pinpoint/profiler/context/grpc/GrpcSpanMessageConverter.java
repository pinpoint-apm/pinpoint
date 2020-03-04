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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.grpc.trace.PAcceptEvent;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PLocalAsyncId;
import com.navercorp.pinpoint.grpc.trace.PMessageEvent;
import com.navercorp.pinpoint.grpc.trace.PNextEvent;
import com.navercorp.pinpoint.grpc.trace.PParentInfo;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.AsyncId;
import com.navercorp.pinpoint.profiler.context.AsyncSpanChunk;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.io.SpanVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Not thread safe
 *
 * @author Woonduk Kang(emeroad)
 */
public class GrpcSpanMessageConverter implements MessageConverter<GeneratedMessageV3> {

    private final String agentId;
    private final short applicationServiceType;

    private final SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanProcessor;
    // WARNING not thread safe
    private final GrpcAnnotationValueMapper grpcAnnotationValueMapper = new GrpcAnnotationValueMapper();

    private final PSpanEvent.Builder pSpanEventBuilder = PSpanEvent.newBuilder();

    private final PAnnotation.Builder pAnnotationBuilder = PAnnotation.newBuilder();

    public GrpcSpanMessageConverter(String agentId, short applicationServiceType,
                                    SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanProcessor) {
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.applicationServiceType = applicationServiceType;
        this.spanProcessor = Assert.requireNonNull(spanProcessor, "spanProcessor");

    }

    @Override
    public GeneratedMessageV3 toMessage(Object message) {
        if (message instanceof SpanChunk) {
            final SpanChunk spanChunk = (SpanChunk) message;
            final PSpanChunk pSpanChunk = buildPSpanChunk(spanChunk);
            return pSpanChunk;
        }
        if (message instanceof Span) {
            final Span span = (Span) message;
            return buildPSpan(span);
        }
        return null;
    }


    @VisibleForTesting
    PSpan buildPSpan(Span span) {
        final PSpan.Builder pSpan = PSpan.newBuilder();

        pSpan.setVersion(SpanVersion.TRACE_V2);

        pSpan.setApplicationServiceType(applicationServiceType);

        final TraceRoot traceRoot = span.getTraceRoot();
        final TraceId traceId = traceRoot.getTraceId();
        final PTransactionId transactionId = newTransactionId(traceId);
        pSpan.setTransactionId(transactionId);
        pSpan.setSpanId(traceId.getSpanId());
        pSpan.setParentSpanId(traceId.getParentSpanId());

        pSpan.setStartTime(span.getStartTime());
        pSpan.setElapsed(span.getElapsedTime());
        pSpan.setServiceType(span.getServiceType());

        PAcceptEvent pAcceptEvent = newAcceptEvent(span);

        pSpan.setAcceptEvent(pAcceptEvent);

        pSpan.setFlag(traceId.getFlags());
        Shared shared = span.getTraceRoot().getShared();
        pSpan.setErr(shared.getErrorCode());

        pSpan.setApiId(span.getApiId());

        final IntStringValue exceptionInfo = span.getExceptionInfo();
        if (exceptionInfo != null) {
            PIntStringValue pIntStringValue = buildPIntStringValue(exceptionInfo);
            pSpan.setExceptionInfo(pIntStringValue);
        }

        pSpan.setLoggingTransactionInfo(shared.getLoggingInfo());

        final List<Annotation> annotations = span.getAnnotations();
        if (CollectionUtils.hasLength(annotations)) {
            final List<PAnnotation> tAnnotations = buildPAnnotation(annotations);
            pSpan.addAllAnnotation(tAnnotations);
        }
        this.spanProcessor.preProcess(span, pSpan);
        final List<SpanEvent> spanEventList = span.getSpanEventList();
        if (CollectionUtils.hasLength(spanEventList)) {
            final List<PSpanEvent> pSpanEvents = buildPSpanEventList(spanEventList);
            pSpan.addAllSpanEvent(pSpanEvents);
        }
        this.spanProcessor.postProcess(span, pSpan);
        return pSpan.build();

    }


    private PTransactionId newTransactionId(TraceId traceId) {
        if (isCompressedType(traceId)) {
            final PTransactionId.Builder builder = PTransactionId.newBuilder();
            builder.setAgentStartTime(traceId.getAgentStartTime());
            builder.setSequence(traceId.getTransactionSequence());
            return builder.build();
        } else {
            final PTransactionId.Builder builder = PTransactionId.newBuilder();
            builder.setAgentId(traceId.getAgentId());
            builder.setAgentStartTime(traceId.getAgentStartTime());
            builder.setSequence(traceId.getTransactionSequence());
            return builder.build();
        }
    }

    private boolean isCompressedType(TraceId traceId) {
        // skip agentId
        return agentId.equals(traceId.getAgentId());
    }

    private PAcceptEvent newAcceptEvent(Span span) {
        PAcceptEvent.Builder builder = PAcceptEvent.newBuilder();

        builder.setRemoteAddr(span.getRemoteAddr());
        final Shared shared = span.getTraceRoot().getShared();
        builder.setRpc(shared.getRpcName());
        builder.setEndPoint(shared.getEndPoint());

        PParentInfo pParentInfo = newParentInfo(span);
        if (pParentInfo != null) {
            builder.setParentInfo(pParentInfo);
        }
        return builder.build();
    }

    private PParentInfo newParentInfo(Span span) {
        final PParentInfo.Builder builder = PParentInfo.newBuilder();
        // For the Queue service type, the acceptorHost value can be stored even without the parentApplicationName value.
        // @See com.navercorp.pinpoint.collector.service.TraceService
        boolean isChanged = false;
        final String parentApplicationName = span.getParentApplicationName();
        if (parentApplicationName != null) {
            builder.setParentApplicationName(parentApplicationName);
            isChanged = true;
        }
        final short parentApplicationType = span.getParentApplicationType();
        if (parentApplicationType != 0) {
            builder.setParentApplicationType(parentApplicationType);
            isChanged = true;
        }
        final String acceptorHost = span.getAcceptorHost();
        if (acceptorHost != null) {
            builder.setAcceptorHost(acceptorHost);
            isChanged = true;
        }
        if (isChanged) {
            return builder.build();
        } else {
            return null;
        }
    }

    private List<PSpanEvent> buildPSpanEventList(List<SpanEvent> spanEventList) {
        final int eventSize = spanEventList.size();
        final List<PSpanEvent> pSpanEventList = new ArrayList<PSpanEvent>(eventSize);
        for (SpanEvent spanEvent : spanEventList) {
            final PSpanEvent.Builder pSpanEvent = buildPSpanEvent(spanEvent);
            pSpanEventList.add(pSpanEvent.build());
        }
        return pSpanEventList;
    }

    @VisibleForTesting
    PSpanChunk buildPSpanChunk(SpanChunk spanChunk) {
        final PSpanChunk.Builder pSpanChunk = PSpanChunk.newBuilder();
        pSpanChunk.setVersion(SpanVersion.TRACE_V2);

//        tSpanChunk.setApplicationName(applicationName);
//        tSpanChunk.setAgentId(agentId);
//        tSpanChunk.setAgentStartTime(agentStartTime);
        pSpanChunk.setApplicationServiceType(applicationServiceType);

        final TraceRoot traceRoot = spanChunk.getTraceRoot();
        final TraceId traceId = traceRoot.getTraceId();

        final PTransactionId transactionId = newTransactionId(traceId);
        pSpanChunk.setTransactionId(transactionId);

        pSpanChunk.setSpanId(traceId.getSpanId());

        final Shared shared = traceRoot.getShared();
        final String endPoint = shared.getEndPoint();
        if (endPoint != null) {
            pSpanChunk.setEndPoint(endPoint);
        }

        if (spanChunk instanceof AsyncSpanChunk) {
            final AsyncSpanChunk asyncSpanChunk = (AsyncSpanChunk) spanChunk;
            final LocalAsyncId localAsyncId = asyncSpanChunk.getLocalAsyncId();
            final PLocalAsyncId.Builder pAsyncIdBuilder = PLocalAsyncId.newBuilder();
            pAsyncIdBuilder.setAsyncId(localAsyncId.getAsyncId());
            pAsyncIdBuilder.setSequence(localAsyncId.getSequence());
            final PLocalAsyncId pLocalAsyncId = pAsyncIdBuilder.build();
            pSpanChunk.setLocalAsyncId(pLocalAsyncId);
        }

        this.spanProcessor.preProcess(spanChunk, pSpanChunk);
        final List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (CollectionUtils.hasLength(spanEventList)) {
            final List<PSpanEvent> pSpanEvents = buildPSpanEventList(spanEventList);
            pSpanChunk.addAllSpanEvent(pSpanEvents);
        }
        this.spanProcessor.postProcess(spanChunk, pSpanChunk);

        return pSpanChunk.build();
    }

    @VisibleForTesting
    public PSpanEvent.Builder buildPSpanEvent(SpanEvent spanEvent) {
        final PSpanEvent.Builder pSpanEvent = getSpanEventBuilder();

//        if (spanEvent.getStartElapsed() != 0) {
//          tSpanEvent.setStartElapsed(spanEvent.getStartElapsed());
//        }
//        tSpanEvent.setStartElapsed(spanEvent.getStartElapsed());
        if (spanEvent.getElapsedTime() != 0) {
            pSpanEvent.setEndElapsed(spanEvent.getElapsedTime());
        }
        pSpanEvent.setSequence(spanEvent.getSequence());
//        tSpanEvent.setRpc(spanEvent.getRpc());
        pSpanEvent.setServiceType(spanEvent.getServiceType());

        //        tSpanEvent.setAnnotations();
        if (spanEvent.getDepth() != -1) {
            pSpanEvent.setDepth(spanEvent.getDepth());
        }

        pSpanEvent.setApiId(spanEvent.getApiId());

        final IntStringValue exceptionInfo = spanEvent.getExceptionInfo();
        if (exceptionInfo != null) {
            PIntStringValue pIntStringValue = buildPIntStringValue(exceptionInfo);
            pSpanEvent.setExceptionInfo(pIntStringValue);
        }

        final PNextEvent nextEvent = buildNextEvent(spanEvent);
        if (nextEvent != null) {
            pSpanEvent.setNextEvent(nextEvent);
        }
        final AsyncId asyncIdObject = spanEvent.getAsyncIdObject();
        if (asyncIdObject != null) {
            pSpanEvent.setAsyncEvent(asyncIdObject.getAsyncId());
        }


        final List<Annotation> annotations = spanEvent.getAnnotations();
        if (CollectionUtils.hasLength(annotations)) {
            final List<PAnnotation> pAnnotations = buildPAnnotation(annotations);
            pSpanEvent.addAllAnnotation(pAnnotations);
        }

        return pSpanEvent;
    }

    private PNextEvent buildNextEvent(SpanEvent spanEvent) {

        PMessageEvent.Builder messageEventBuilder = null;
        final String endPoint = spanEvent.getEndPoint();
        if (endPoint != null) {
            messageEventBuilder = newPMessageEvent(messageEventBuilder);
            messageEventBuilder.setEndPoint(endPoint);
        }

        if (spanEvent.getNextSpanId() != -1) {
            messageEventBuilder = newPMessageEvent(messageEventBuilder);
            messageEventBuilder.setNextSpanId(spanEvent.getNextSpanId());
        }

        final String destinationId = spanEvent.getDestinationId();
        if (destinationId != null) {
            messageEventBuilder = newPMessageEvent(messageEventBuilder);
            messageEventBuilder.setDestinationId(destinationId);
        }

        if (messageEventBuilder != null) {
            PNextEvent.Builder nextEvent = PNextEvent.newBuilder();
            nextEvent.setMessageEvent(messageEventBuilder.build());
            return nextEvent.build();
        }
        return null;
    }

    private PMessageEvent.Builder newPMessageEvent(PMessageEvent.Builder builder) {
        if (builder == null) {
            return PMessageEvent.newBuilder();
        }
        return builder;
    }

    private PIntStringValue buildPIntStringValue(IntStringValue exceptionInfo) {
        PIntStringValue.Builder builder = PIntStringValue.newBuilder();
        builder.setIntValue(exceptionInfo.getIntValue());
        if (exceptionInfo.getStringValue() != null) {
            final StringValue stringValue = StringValue.of(exceptionInfo.getStringValue());
            builder.setStringValue(stringValue);
        }
        return builder.build();
    }

    @VisibleForTesting
    List<PAnnotation> buildPAnnotation(List<Annotation> annotations) {
        final List<PAnnotation> tAnnotationList = new ArrayList<PAnnotation>(annotations.size());
        for (Annotation annotation : annotations) {
            final PAnnotation.Builder builder = getAnnotationBuilder();
            builder.setKey(annotation.getAnnotationKey());
            final PAnnotationValue pAnnotationValue = grpcAnnotationValueMapper.buildPAnnotationValue(annotation.getValue());
            if (pAnnotationValue != null) {
                builder.setValue(pAnnotationValue);
            }
            PAnnotation pAnnotation = builder.build();
            tAnnotationList.add(pAnnotation);
        }
        return tAnnotationList;
    }

    private PAnnotation.Builder getAnnotationBuilder() {
        this.pAnnotationBuilder.clear();
        return pAnnotationBuilder;
    }

    private PSpanEvent.Builder getSpanEventBuilder() {
        pSpanEventBuilder.clear();
        return pSpanEventBuilder;
    }

    @Override
    public String toString() {
        return "GrpcSpanMessageConverter{" +
                "agentId='" + agentId + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", spanProcessor=" + spanProcessor +
                '}';
    }
}
