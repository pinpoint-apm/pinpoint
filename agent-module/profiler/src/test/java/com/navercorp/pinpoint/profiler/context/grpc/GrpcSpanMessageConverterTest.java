package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.AsyncId;
import com.navercorp.pinpoint.profiler.context.AsyncSpanChunk;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;
import com.navercorp.pinpoint.profiler.context.compress.GrpcSpanProcessorV2;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanAutoUriGetter;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanUriGetter;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AnnotationValueMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.SpanMessageMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.SpanMessageMapperImpl;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author intr3p1d
 */
class GrpcSpanMessageConverterTest {

    private static final Random random = new Random();
    private static final AgentId agentId = AgentId.of("agent");
    private static final String parentAgentId = "agent-parent";
    private static final short applicationServiceType = 0;
    private final SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanProcessorProtoV2 = new GrpcSpanProcessorV2();
    private final SpanUriGetter spanUriGetter = new SpanAutoUriGetter();
    private final AnnotationValueMapper annotationValueMapper = Mappers.getMapper(AnnotationValueMapper.class);
    private final SpanMessageMapper spanMessageMapper = new SpanMessageMapperImpl(annotationValueMapper, spanUriGetter);
    GrpcSpanMessageConverter converter = new GrpcSpanMessageConverter(
            agentId, applicationServiceType, spanProcessorProtoV2, spanMessageMapper
    );

    static Span newSpan() {
        Span span = mock(Span.class);
        when(span.getStartTime()).thenReturn(random.nextLong());
        when(span.getParentApplicationName()).thenReturn(parentAgentId);
        when(span.getParentApplicationType()).thenReturn((short) random.nextInt());
        when(span.getAcceptorHost()).thenReturn(randomString());
        when(span.getElapsedTime()).thenReturn(random.nextInt());
        when(span.getServiceType()).thenReturn((short) random.nextInt());
        when(span.getRemoteAddr()).thenReturn(randomString());
        when(span.getApiId()).thenReturn(random.nextInt());
        when(span.getExceptionInfo()).thenReturn(new IntStringValue(random.nextInt(), randomString()));

        TraceRoot traceRoot = newTraceRoot();
        when(span.getTraceRoot()).thenReturn(traceRoot);

        List<Annotation<?>> annotations = newAnnotations();
        when(span.getAnnotations()).thenReturn(annotations);

        List<SpanEvent> spanEventList = newSpanEvents();
        when(span.getSpanEventList()).thenReturn(spanEventList);
        return span;
    }

    static SpanChunk newSpanChunk() {
        SpanChunk spanChunk = mock(SpanChunk.class);

        TraceRoot traceRoot = newTraceRoot();
        when(spanChunk.getTraceRoot()).thenReturn(traceRoot);

        List<SpanEvent> spanEventList = newSpanEvents();
        when(spanChunk.getSpanEventList()).thenReturn(spanEventList);
        return spanChunk;
    }

    static SpanChunk newAsyncSpanChunk() {
        AsyncSpanChunk asyncSpanChunk = mock(AsyncSpanChunk.class);

        TraceRoot traceRoot = newTraceRoot();
        when(asyncSpanChunk.getTraceRoot()).thenReturn(traceRoot);

        List<SpanEvent> spanEventList = newSpanEvents();
        when(asyncSpanChunk.getSpanEventList()).thenReturn(spanEventList);

        LocalAsyncId localAsyncId = mock(LocalAsyncId.class);
        when(localAsyncId.getAsyncId()).thenReturn(random.nextInt());
        when(localAsyncId.getSequence()).thenReturn(random.nextInt());

        when(asyncSpanChunk.getLocalAsyncId()).thenReturn(localAsyncId);
        return asyncSpanChunk;
    }

    static List<SpanEvent> newSpanEvents() {
        List<SpanEvent> spanEventList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            SpanEvent spanEvent = mock(SpanEvent.class);
            when(spanEvent.getStackId()).thenReturn(random.nextInt());
            when(spanEvent.getStartTime()).thenReturn(random.nextLong());
            when(spanEvent.getElapsedTime()).thenReturn(random.nextInt());
            when(spanEvent.getSequence()).thenReturn(random.nextInt());
            when(spanEvent.getServiceType()).thenReturn((short) random.nextInt());
            when(spanEvent.getEndPoint()).thenReturn(randomString());
            when(spanEvent.getAnnotations()).thenReturn(newAnnotations());
            when(spanEvent.getDestinationId()).thenReturn(randomString());
            when(spanEvent.getApiId()).thenReturn(random.nextInt());
            when(spanEvent.getExceptionInfo()).thenReturn(new IntStringValue(random.nextInt(), randomString()));

            AsyncId asyncId = mock(AsyncId.class);
            when(asyncId.getAsyncId()).thenReturn(random.nextInt());
            when(asyncId.nextAsyncSequence()).thenReturn(random.nextInt());

            when(spanEvent.getAsyncIdObject()).thenReturn(asyncId);

            spanEventList.add(spanEvent);
        }
        return spanEventList;
    }

    static List<Annotation<?>> newAnnotations() {
        return Arrays.asList(
                Annotations.of(1, "foo"),
                Annotations.of(1, Integer.MAX_VALUE),
                Annotations.of(1, Long.MAX_VALUE),
                Annotations.of(1, Boolean.TRUE),
                Annotations.of(1, Byte.MAX_VALUE),
                Annotations.of(1, Float.MAX_VALUE),
                Annotations.of(1, Double.MAX_VALUE),
                Annotations.of(1, "foo".getBytes()),
                Annotations.of(1, Short.MAX_VALUE)
        );
    }

    static TraceRoot newTraceRoot() {
        TraceRoot traceRoot = mock(TraceRoot.class);
        final long agentStartTime = System.currentTimeMillis();
        when(traceRoot.getTraceId()).thenReturn(new DefaultTraceId(agentId, agentStartTime, 0));
        when(traceRoot.getTraceStartTime()).thenReturn(agentStartTime + 100);
        when(traceRoot.getLocalTransactionId()).thenReturn((long) 1);

        Shared shared = mock(Shared.class);
        when(shared.getUriTemplate()).thenReturn("/api/test");
        when(shared.getEndPoint()).thenReturn(randomString());
        when(shared.getLoggingInfo()).thenReturn((byte) 123);
        when(shared.getErrorCode()).thenReturn(random.nextInt());
        when(traceRoot.getShared()).thenReturn(shared);

        return traceRoot;
    }

    @Test
    void testMapSpan() {
        Span span = newSpan();
        PSpan pSpan = converter.buildPSpan(span);

        assertEquals(SpanVersion.TRACE_V2, pSpan.getVersion());
        assertEquals(applicationServiceType, pSpan.getApplicationServiceType());

        // skipped in compressed type
        // assertEquals(span.getTraceRoot().getTraceId().getAgentId(), pSpan.getTransactionId().getAgentId());
        assertEquals(span.getTraceRoot().getTraceId().getAgentStartTime(), pSpan.getTransactionId().getAgentStartTime());
        assertEquals(span.getTraceRoot().getTraceId().getAgentStartTime(), pSpan.getTransactionId().getAgentStartTime());
        assertEquals(span.getTraceRoot().getTraceId().getTransactionSequence(), pSpan.getTransactionId().getSequence());

        assertEquals(span.getTraceRoot().getTraceId().getSpanId(), pSpan.getSpanId());
        assertEquals(span.getTraceRoot().getTraceId().getParentSpanId(), pSpan.getParentSpanId());
        assertEquals(span.getStartTime(), pSpan.getStartTime());
        assertEquals(span.getElapsedTime(), pSpan.getElapsed());
        assertEquals(span.getServiceType(), pSpan.getServiceType());

        assertEquals(span.getRemoteAddr(), pSpan.getAcceptEvent().getRemoteAddr());
        assertEquals(spanUriGetter.getCollectedUri(span.getTraceRoot().getShared()), pSpan.getAcceptEvent().getRpc());
        assertEquals(span.getTraceRoot().getShared().getEndPoint(), pSpan.getAcceptEvent().getEndPoint());

        assertEquals(span.getParentApplicationName(), pSpan.getAcceptEvent().getParentInfo().getParentApplicationName());
        assertEquals(span.getParentApplicationType(), pSpan.getAcceptEvent().getParentInfo().getParentApplicationType());
        assertEquals(span.getAcceptorHost(), pSpan.getAcceptEvent().getParentInfo().getAcceptorHost());

        assertEquals(span.getTraceRoot().getTraceId().getFlags(), pSpan.getFlag());
        assertEquals(span.getTraceRoot().getShared().getErrorCode(), pSpan.getErr());
        assertEquals(span.getApiId(), pSpan.getApiId());
        assertEquals(span.getExceptionInfo().getIntValue(), pSpan.getExceptionInfo().getIntValue());
        assertEquals(span.getExceptionInfo().getStringValue(), pSpan.getExceptionInfo().getStringValue().getValue());
        assertEquals(span.getTraceRoot().getShared().getLoggingInfo(), pSpan.getLoggingTransactionInfo());

        assertEquals(span.getSpanEventList().size(), pSpan.getSpanEventList().size());
        for (int i = 0; i < span.getSpanEventList().size(); i++) {
            SpanEvent spanEvent = span.getSpanEventList().get(i);
            PSpanEvent pSpanEvent = pSpan.getSpanEvent(i);

            assertEquals(spanEvent.getElapsedTime(), pSpanEvent.getEndElapsed());
            assertEquals(spanEvent.getSequence(), pSpanEvent.getSequence());
            assertEquals(spanEvent.getServiceType(), pSpanEvent.getServiceType());
            assertEquals(spanEvent.getDepth(), pSpanEvent.getDepth());
            assertEquals(spanEvent.getApiId(), pSpanEvent.getApiId());

            assertEquals(spanEvent.getExceptionInfo().getIntValue(), pSpanEvent.getExceptionInfo().getIntValue());
            assertEquals(spanEvent.getExceptionInfo().getStringValue(), pSpanEvent.getExceptionInfo().getStringValue().getValue());

            assertEquals(spanEvent.getDepth(), pSpanEvent.getDepth());

            assertEquals(spanEvent.getEndPoint(), pSpanEvent.getNextEvent().getMessageEvent().getEndPoint());
            assertEquals(spanEvent.getNextSpanId(), pSpanEvent.getNextEvent().getMessageEvent().getNextSpanId());
            assertEquals(spanEvent.getDestinationId(), pSpanEvent.getNextEvent().getMessageEvent().getDestinationId());

            assertEquals(spanEvent.getAsyncIdObject().getAsyncId(), pSpanEvent.getAsyncEvent());
        }
    }



    @Test
    void testMapSpanChunk() {
        SpanChunk spanChunk = newSpanChunk();
        PSpanChunk pSpanChunk = converter.buildPSpanChunk(spanChunk);

        assertEquals(SpanVersion.TRACE_V2, pSpanChunk.getVersion());
        assertEquals(applicationServiceType, pSpanChunk.getApplicationServiceType());

        // skipped in compressed type
        // assertEquals(spanChunk.getTraceRoot().getTraceId().getAgentId(), pSpanChunk.getTransactionId().getAgentId());
        assertEquals(spanChunk.getTraceRoot().getTraceId().getAgentStartTime(), pSpanChunk.getTransactionId().getAgentStartTime());
        assertEquals(spanChunk.getTraceRoot().getTraceId().getAgentStartTime(), pSpanChunk.getTransactionId().getAgentStartTime());
        assertEquals(spanChunk.getTraceRoot().getTraceId().getTransactionSequence(), pSpanChunk.getTransactionId().getSequence());

        assertEquals(spanChunk.getTraceRoot().getTraceId().getSpanId(), pSpanChunk.getSpanId());

        assertEquals(spanChunk.getSpanEventList().size(), pSpanChunk.getSpanEventList().size());
        for (int i = 0; i < spanChunk.getSpanEventList().size(); i++) {
            SpanEvent spanEvent = spanChunk.getSpanEventList().get(i);
            PSpanEvent pSpanEvent = pSpanChunk.getSpanEvent(i);

            assertEquals(spanEvent.getElapsedTime(), pSpanEvent.getEndElapsed());
            assertEquals(spanEvent.getSequence(), pSpanEvent.getSequence());
            assertEquals(spanEvent.getServiceType(), pSpanEvent.getServiceType());
            assertEquals(spanEvent.getDepth(), pSpanEvent.getDepth());
            assertEquals(spanEvent.getApiId(), pSpanEvent.getApiId());

            assertEquals(spanEvent.getExceptionInfo().getIntValue(), pSpanEvent.getExceptionInfo().getIntValue());
            assertEquals(spanEvent.getExceptionInfo().getStringValue(), pSpanEvent.getExceptionInfo().getStringValue().getValue());

            assertEquals(spanEvent.getDepth(), pSpanEvent.getDepth());

            assertEquals(spanEvent.getEndPoint(), pSpanEvent.getNextEvent().getMessageEvent().getEndPoint());
            assertEquals(spanEvent.getNextSpanId(), pSpanEvent.getNextEvent().getMessageEvent().getNextSpanId());
            assertEquals(spanEvent.getDestinationId(), pSpanEvent.getNextEvent().getMessageEvent().getDestinationId());

            assertEquals(spanEvent.getAsyncIdObject().getAsyncId(), pSpanEvent.getAsyncEvent());
        }
    }

    @Test
    void testMapAsyncSpanChunk() {
        SpanChunk spanChunk = newAsyncSpanChunk();
        PSpanChunk pSpanChunk = converter.buildPSpanChunk(spanChunk);

        assertEquals(SpanVersion.TRACE_V2, pSpanChunk.getVersion());
        assertEquals(applicationServiceType, pSpanChunk.getApplicationServiceType());

        // skipped in compressed type
        // assertEquals(spanChunk.getTraceRoot().getTraceId().getAgentId(), pSpanChunk.getTransactionId().getAgentId());
        assertEquals(spanChunk.getTraceRoot().getTraceId().getAgentStartTime(), pSpanChunk.getTransactionId().getAgentStartTime());
        assertEquals(spanChunk.getTraceRoot().getTraceId().getAgentStartTime(), pSpanChunk.getTransactionId().getAgentStartTime());
        assertEquals(spanChunk.getTraceRoot().getTraceId().getTransactionSequence(), pSpanChunk.getTransactionId().getSequence());

        assertEquals(spanChunk.getTraceRoot().getTraceId().getSpanId(), pSpanChunk.getSpanId());

        assertEquals(spanChunk.getSpanEventList().size(), pSpanChunk.getSpanEventList().size());
        for (int i = 0; i < spanChunk.getSpanEventList().size(); i++) {
            SpanEvent spanEvent = spanChunk.getSpanEventList().get(i);
            PSpanEvent pSpanEvent = pSpanChunk.getSpanEvent(i);

            assertEquals(spanEvent.getElapsedTime(), pSpanEvent.getEndElapsed());
            assertEquals(spanEvent.getSequence(), pSpanEvent.getSequence());
            assertEquals(spanEvent.getServiceType(), pSpanEvent.getServiceType());
            assertEquals(spanEvent.getDepth(), pSpanEvent.getDepth());
            assertEquals(spanEvent.getApiId(), pSpanEvent.getApiId());

            assertEquals(spanEvent.getExceptionInfo().getIntValue(), pSpanEvent.getExceptionInfo().getIntValue());
            assertEquals(spanEvent.getExceptionInfo().getStringValue(), pSpanEvent.getExceptionInfo().getStringValue().getValue());

            assertEquals(spanEvent.getDepth(), pSpanEvent.getDepth());

            assertEquals(spanEvent.getEndPoint(), pSpanEvent.getNextEvent().getMessageEvent().getEndPoint());
            assertEquals(spanEvent.getNextSpanId(), pSpanEvent.getNextEvent().getMessageEvent().getNextSpanId());
            assertEquals(spanEvent.getDestinationId(), pSpanEvent.getNextEvent().getMessageEvent().getDestinationId());

            assertEquals(spanEvent.getAsyncIdObject().getAsyncId(), pSpanEvent.getAsyncEvent());
        }

        // check AsyncSpanChunk
        AsyncSpanChunk asyncSpanChunk = (AsyncSpanChunk) spanChunk;

        assertEquals(asyncSpanChunk.getLocalAsyncId().getAsyncId(), pSpanChunk.getLocalAsyncId().getAsyncId());
        assertEquals(asyncSpanChunk.getLocalAsyncId().getSequence(), pSpanChunk.getLocalAsyncId().getSequence());
    }

}