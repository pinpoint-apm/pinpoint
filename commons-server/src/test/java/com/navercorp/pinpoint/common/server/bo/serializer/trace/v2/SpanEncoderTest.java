/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.RandomTSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.io.CollectorGrpcSpanFactory;
import com.navercorp.pinpoint.common.server.io.DefaultServerHeader;
import com.navercorp.pinpoint.common.server.io.GrpcSpanBinder;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.io.SpanVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEncoderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int REPEAT_COUNT = 10;

    private final long requestTime = System.currentTimeMillis();

    private final RandomTSpan randomTSpan = new RandomTSpan();
    private final Random random = new Random();

    private final ServerHeader header = new DefaultServerHeader(
            "agentId", "agentName", "applicationName", ServiceUid.DEFAULT_SERVICE_UID_NAME, () -> ServiceUid.DEFAULT, 88, 100, false);

    private final GrpcSpanBinder grpcSpanBinder = new GrpcSpanBinder();
    private final GrpcSpanBinder grpcOtelSpanBinder = new GrpcSpanBinder(TraceSourceType.OPENTELEMETRY);

    private final SpanEventFilter filter = new EmptySpanEventFilter();
    private final GrpcSpanFactory grpcSpanFactory = new CollectorGrpcSpanFactory(grpcSpanBinder, filter);

    private final SpanEncoder spanEncoder = new SpanEncoderV0();
    private final SpanDecoder spanDecoder = new SpanDecoderV0();


    @RepeatedTest(REPEAT_COUNT)
    public void testEncodeSpanColumnValue_simpleSpan() {
        SpanBo spanBo = randomSpan();
        assertSpan(spanBo);
    }


    @RepeatedTest(REPEAT_COUNT)
    public void testEncodeSpanColumnValue_complexSpan() {
        SpanBo spanBo = randomComplexSpan();
        assertSpan(spanBo);
    }


    @RepeatedTest(REPEAT_COUNT)
    public void testEncodeSpanColumnValue_simpleSpanChunk() {
        SpanChunkBo spanChunkBo = randomSpanChunk();
        assertSpanChunk(spanChunkBo);
    }


    @RepeatedTest(REPEAT_COUNT)
    public void testEncodeSpanColumnValue_complexSpanChunk() {
        SpanChunkBo spanChunkBo = randomComplexSpanChunk();
        assertSpanChunk(spanChunkBo);
    }

    @RepeatedTest(REPEAT_COUNT)
    public void testEncodeSpan_otelSource_qualifierByteAndRoundTrip() {
        SpanBo spanBo = randomComplexSpan(grpcOtelSpanBinder);

        SpanEncodingContext<SpanBo> ctx = new SpanEncodingContext<>(spanBo);
        ByteBuffer qualifier = spanEncoder.encodeSpanQualifier(ctx);
        Assertions.assertThat(qualifier.get(qualifier.position()))
                .as("OTel span qualifier first byte must be TYPE_OTEL_SPAN")
                .isEqualTo(SpanEncoder.TYPE_OTEL_SPAN);

        assertSpan(spanBo);
    }

    @RepeatedTest(REPEAT_COUNT)
    public void testEncodeSpanChunk_otelSource_qualifierByteAndRoundTrip() {
        SpanChunkBo spanChunkBo = randomComplexSpanChunk(grpcOtelSpanBinder);

        SpanEncodingContext<SpanChunkBo> ctx = new SpanEncodingContext<>(spanChunkBo);
        ByteBuffer qualifier = spanEncoder.encodeSpanChunkQualifier(ctx);
        Assertions.assertThat(qualifier.get(qualifier.position()))
                .as("OTel span chunk qualifier first byte must be TYPE_OTEL_SPAN_CHUNK")
                .isEqualTo(SpanEncoder.TYPE_OTEL_SPAN_CHUNK);

        assertSpanChunk(spanChunkBo);
    }

    @Test
    public void testEncodeSpan_pinpointSource_qualifierFirstByteIsTypeSpan() {
        SpanBo spanBo = randomSpan();
        // default traceSourceType = PINPOINT
        SpanEncodingContext<SpanBo> ctx = new SpanEncodingContext<>(spanBo);
        ByteBuffer qualifier = spanEncoder.encodeSpanQualifier(ctx);
        Assertions.assertThat(qualifier.get(qualifier.position()))
                .as("Pinpoint span qualifier first byte must be TYPE_SPAN")
                .isEqualTo(SpanEncoder.TYPE_SPAN);
    }

    @Test
    public void testEncodeSpanChunk_pinpointSource_qualifierFirstByteIsTypeSpanChunk() {
        SpanChunkBo spanChunkBo = randomSpanChunk();
        SpanEncodingContext<SpanChunkBo> ctx = new SpanEncodingContext<>(spanChunkBo);
        ByteBuffer qualifier = spanEncoder.encodeSpanChunkQualifier(ctx);
        Assertions.assertThat(qualifier.get(qualifier.position()))
                .as("Pinpoint span chunk qualifier first byte must be TYPE_SPAN_CHUNK")
                .isEqualTo(SpanEncoder.TYPE_SPAN_CHUNK);
    }

    private long getCollectorAcceptTime() {
        long currentTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        long randomSeed = random.nextLong(0, TimeUnit.DAYS.toMillis(60));
        return currentTime - randomSeed;
    }

    private Buffer wrapBuffer(ByteBuffer byteBuffer) {
        byte[] buffer = new byte[byteBuffer.remaining()];
        byteBuffer.get(buffer);
        return new FixedBuffer(buffer);
    }

    private SpanBo randomSpan() {
        PSpan.Builder tSpan = randomTSpan.randomPSpan();
        return grpcSpanFactory.buildSpanBo(tSpan.build(), header, requestTime);
    }

    public SpanBo randomComplexSpan() {
        return randomComplexSpan(grpcSpanBinder);
    }

    public SpanBo randomComplexSpan(GrpcSpanBinder grpcSpanBinder) {
        PSpan.Builder pSpan = randomTSpan.randomPSpan();
        PSpanEvent spanEvent1 = randomTSpan.randomTSpanEvent((short) 1);
        PSpanEvent spanEvent2 = randomTSpan.randomTSpanEvent((short) 2);
        PSpanEvent spanEvent3 = randomTSpan.randomTSpanEvent((short) 3);
        PSpanEvent spanEvent4 = randomTSpan.randomTSpanEvent((short) 5);

        pSpan.addAllSpanEvent(List.of(spanEvent1, spanEvent2, spanEvent3, spanEvent4));
        PSpan span = pSpan.build();
        SpanBo spanBo = grpcSpanBinder.newSpanBo(span, header, requestTime);
        spanBo.addSpanEventBoList(grpcSpanBinder.bindSpanEventBoList(span.getSpanEventList()));
        return spanBo;
    }

    private SpanChunkBo randomSpanChunk() {
        PSpanChunk.Builder spanChunk = randomTSpan.randomTSpanChunk();
        return grpcSpanFactory.buildSpanChunkBo(spanChunk.build(), header, requestTime);
    }

    public SpanChunkBo randomComplexSpanChunk() {
        return randomComplexSpanChunk(grpcSpanBinder);
    }

    public SpanChunkBo randomComplexSpanChunk(GrpcSpanBinder grpcSpanBinder) {
        PSpanChunk.Builder spanChunk = randomTSpan.randomTSpanChunk();
        PSpanEvent spanEvent1 = randomTSpan.randomTSpanEvent((short) 1);
        PSpanEvent spanEvent2 = randomTSpan.randomTSpanEvent((short) 2);
        PSpanEvent spanEvent3 = randomTSpan.randomTSpanEvent((short) 3);
        PSpanEvent spanEvent4 = randomTSpan.randomTSpanEvent((short) 5);

        spanChunk.addAllSpanEvent(List.of(spanEvent1, spanEvent2, spanEvent3, spanEvent4));
        PSpanChunk chunk = spanChunk.build();
        SpanChunkBo spanChunkBo = grpcSpanBinder.newSpanChunkBo(chunk, header, requestTime);
        spanChunkBo.addSpanEventBoList(grpcSpanBinder.bindSpanEventBoList(chunk.getSpanEventList()));
        return spanChunkBo;
    }


    private void assertSpan(SpanBo spanBo) {
        spanBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<>(spanBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext(spanBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanBo.getCollectorAcceptTime());

        SpanBo decode = (SpanBo) spanDecoder.decode(qualifier, column, decodingContext);

        List<String> excludeField = List.of("parentApplication", "annotationBoList", "spanEventBoList", "owner.agentName");
        Assertions.assertThat(decode)
                .usingRecursiveComparison()
                .ignoringFields(excludeField.toArray(new String[0]))
                .isEqualTo(spanBo);

        logger.debug("{} {}", spanBo.getAnnotationBoList(), decode.getAnnotationBoList());
        Assertions.assertThat(spanBo.getAnnotationBoList())
                .usingRecursiveComparison()
                .ignoringFields("annotation")
                .isEqualTo(decode.getAnnotationBoList());

        List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        List<SpanEventBo> decodedSpanEventBoList = decode.getSpanEventBoList();
        Assertions.assertThat(spanEventBoList)
                .usingRecursiveComparison()
                .ignoringFields("annotationBoList")
                .isEqualTo(decodedSpanEventBoList);
    }

    private void assertSpanChunk(SpanChunkBo spanChunkBo) {
        assertSpanChunk(spanChunkBo, getCollectorAcceptTime());
    }

    private void assertSpanChunk(SpanChunkBo spanChunkBo, long collectorAcceptTime) {
        spanChunkBo.setCollectorAcceptTime(collectorAcceptTime);

        SpanEncodingContext<SpanChunkBo> encodingContext = new SpanEncodingContext<>(spanChunkBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanChunkQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanChunkColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext(spanChunkBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanChunkBo.getCollectorAcceptTime());

        SpanChunkBo decode = (SpanChunkBo) spanDecoder.decode(qualifier, column, decodingContext);
        // TODO Check CI log
        // logger.debug("spanChunk dump \noriginal spanChunkBo:{} \ndecode spanChunkBo:{} ", spanChunkBo, decode);

        List<String> notSerializedField = Lists.newArrayList("endPoint", "serviceType", "applicationServiceType");
        List<String> excludeField = List.of("spanEventBoList", "localAsyncId", "owner.agentName");
        notSerializedField.addAll(excludeField);
        Assertions.assertThat(decode)
                .usingRecursiveComparison()
                .ignoringFields(notSerializedField.toArray(new String[0]))
                .isEqualTo(spanChunkBo);


        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        List<SpanEventBo> decodedSpanEventBoList = decode.getSpanEventBoList();
//        Assertions.assertEquals(spanEventBoList.size(), spanEventBoList.size());
//        Assertions.assertTrue(EqualsBuilder.reflectionEquals(spanEventBoList, decodedSpanEventBoList), "spanEventBoList");
        Assertions.assertThat(spanEventBoList)
                .usingRecursiveComparison()
                .ignoringFields("annotationBoList")
                .isEqualTo(decodedSpanEventBoList);
    }

    @Test
    public void testEncodeSpanColumnValue_spanEvent_startTimeDelta_equals() {
        SpanBo spanBo = randomComplexSpan();
        SpanEventBo spanEventBo0 = spanBo.getSpanEventBoList().get(0);
        SpanEventBo spanEventBo1 = spanBo.getSpanEventBoList().get(1);
        spanEventBo1.setStartElapsed(spanEventBo0.getStartElapsed());

        assertSpan(spanBo);
    }

    @Test
    public void testEncodeSpanColumnValue_spanEvent_depth_equals() {
        SpanBo spanBo = randomComplexSpan();
        SpanEventBo spanEventBo0 = spanBo.getSpanEventBoList().get(0);
        SpanEventBo spanEventBo1 = spanBo.getSpanEventBoList().get(1);
        spanEventBo1.setDepth(spanEventBo0.getDepth());

        assertSpan(spanBo);
    }

    @Test
    public void testEncodeSpanColumnValue_spanEvent_service_equals() {
        SpanBo spanBo = randomComplexSpan();
        SpanEventBo spanEventBo0 = spanBo.getSpanEventBoList().get(0);
        SpanEventBo spanEventBo1 = spanBo.getSpanEventBoList().get(1);
        spanEventBo1.setServiceType(spanEventBo0.getServiceType());

        assertSpan(spanBo);
    }

    @Test
    public void testEncodeSpanColumnValue_traceV3_preservesNanos() {
        SpanBo spanBo = randomComplexSpan();
        setTraceV3Time(spanBo);

        assertSpan(spanBo);
    }

    @Test
    public void testEncodeSpanChunkColumnValue_traceV3_preservesNanos() {
        SpanChunkBo spanChunkBo = randomComplexSpanChunk();
        setTraceV3Time(spanChunkBo);

        assertSpanChunk(spanChunkBo);
    }

    @Test
    public void testEncodeSpanColumnValue_traceV3_preservesNegativeSpanEventStartOffset() {
        SpanBo spanBo = randomComplexSpan();
        setTraceV3Time(spanBo);

        final SpanEventBo spanEventBo = spanBo.getSpanEventBoList().get(0);
        final long startTimeNanos = spanBo.getStartTimeNanos() - 123_456;
        spanEventBo.setTraceTime(SpanVersion.TRACE_V3, startTimeNanos, startTimeNanos + 789_123,
                spanEventBo.getStartElapsed());

        assertSpan(spanBo);
    }

    @Test
    public void testEncodeSpanChunkColumnValue_traceV3_keyTimeIndependentOfCollectorAcceptTime() {
        SpanChunkBo spanChunkBo = randomComplexSpanChunk();
        setTraceV3Time(spanChunkBo);

        final long encodeAcceptTime = getCollectorAcceptTime();
        spanChunkBo.setCollectorAcceptTime(encodeAcceptTime);

        SpanEncodingContext<SpanChunkBo> encodingContext = new SpanEncodingContext<>(spanChunkBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanChunkQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanChunkColumnValue(encodingContext));

        // Decode with a DIFFERENT collectorAcceptTime to simulate HBase cell-timestamp drift.
        // Because TRACE_V3 keyTime is stored as an absolute value, it must reconstruct identically
        // regardless of the anchor - and so must every span event time derived from it.
        final long decodeAcceptTime = encodeAcceptTime + TimeUnit.HOURS.toMillis(3);
        SpanDecodingContext decodingContext = new SpanDecodingContext(spanChunkBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(decodeAcceptTime);

        SpanChunkBo decode = (SpanChunkBo) spanDecoder.decode(qualifier, column, decodingContext);

        Assertions.assertThat(decode.getKeyTimeNanos()).isEqualTo(spanChunkBo.getKeyTimeNanos());

        List<SpanEventBo> expected = spanChunkBo.getSpanEventBoList();
        List<SpanEventBo> actual = decode.getSpanEventBoList();
        Assertions.assertThat(actual).hasSameSizeAs(expected);
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertThat(actual.get(i).getStartTimeNanos()).isEqualTo(expected.get(i).getStartTimeNanos());
            Assertions.assertThat(actual.get(i).getEndTimeNanos()).isEqualTo(expected.get(i).getEndTimeNanos());
        }
    }

    @Test
    public void testEncodeSpanColumnValue_traceV3_spanEventVersionMismatch() {
        SpanBo spanBo = randomComplexSpan();
        final long startTimeNanos = spanBo.getStartTimeNanos();
        spanBo.setTraceTime(SpanVersion.TRACE_V3, startTimeNanos,
                startTimeNanos + TimeUnit.MILLISECONDS.toNanos(spanBo.getElapsed()),
                spanBo.getElapsed());
        spanBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<>(spanBo);
        Assertions.assertThatThrownBy(() -> spanEncoder.encodeSpanColumnValue(encodingContext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("span event start time is not set");
    }

    @Test
    public void testEncodeSpanColumnValue_traceV3_spanEndTimestampRequired() {
        SpanBo spanBo = randomSpan();
        spanBo.setVersion(SpanVersion.TRACE_V3);
        spanBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<>(spanBo);
        Assertions.assertThatThrownBy(() -> spanEncoder.encodeSpanColumnValue(encodingContext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("span end time is not set");
    }

    @Test
    public void testEncodeSpanColumnValue_traceV3_spanEventStartTimestampRequired() {
        SpanBo spanBo = randomComplexSpan();
        final long startTimeNanos = spanBo.getStartTimeNanos();
        spanBo.setTraceTime(SpanVersion.TRACE_V3, startTimeNanos,
                startTimeNanos + TimeUnit.MILLISECONDS.toNanos(spanBo.getElapsed()),
                spanBo.getElapsed());
        spanBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEventBo spanEventBo = spanBo.getSpanEventBoList().get(0);
        spanEventBo.setVersion(SpanVersion.TRACE_V3);

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<>(spanBo);
        Assertions.assertThatThrownBy(() -> spanEncoder.encodeSpanColumnValue(encodingContext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("span event start time is not set");
    }

    private void setTraceV3Time(SpanBo spanBo) {
        final long startTimeNanos = spanBo.getStartTimeNanos() + 123_456;
        final long elapsedNanos = TimeUnit.MILLISECONDS.toNanos(spanBo.getElapsed()) + 789_123;

        spanBo.setTraceTime(SpanVersion.TRACE_V3, startTimeNanos, startTimeNanos + elapsedNanos,
                spanBo.getElapsed());
        setTraceV3SpanEventTime(spanBo.getSpanEventBoList(), startTimeNanos);
    }

    private void setTraceV3Time(SpanChunkBo spanChunkBo) {
        final long keyTimeNanos = spanChunkBo.getKeyTimeNanos() + 123_456;

        spanChunkBo.setTraceTime(SpanVersion.TRACE_V3, keyTimeNanos);
        setTraceV3SpanEventTime(spanChunkBo.getSpanEventBoList(), keyTimeNanos);
    }

    private void setTraceV3SpanEventTime(List<SpanEventBo> spanEventBoList, long baseTimeNanos) {
        for (int i = 0; i < spanEventBoList.size(); i++) {
            SpanEventBo spanEventBo = spanEventBoList.get(i);
            final long startTimeNanos = baseTimeNanos
                    + TimeUnit.MILLISECONDS.toNanos(spanEventBo.getStartElapsed())
                    + i + 1;
            final long elapsedNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(spanEventBo.getEndElapsed(), 0))
                    + i + 1;

            spanEventBo.setTraceTime(SpanVersion.TRACE_V3, startTimeNanos, startTimeNanos + elapsedNanos,
                    spanEventBo.getStartElapsed());
        }
    }
}
