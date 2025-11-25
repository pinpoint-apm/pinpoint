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
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.grpc.BindAttribute;
import com.navercorp.pinpoint.common.server.bo.grpc.CollectorGrpcSpanFactory;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanBinder;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
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

    private final long spanAcceptedTime = System.currentTimeMillis();

    private final RandomTSpan randomTSpan = new RandomTSpan();
    private final Random random = new Random();

    private final BindAttribute attribute = new BindAttribute("agentId", "agentName", "applicationName", () -> ApplicationUid.of(1), 88, spanAcceptedTime);
    private final GrpcSpanBinder grpcSpanBinder = new GrpcSpanBinder();
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
        return grpcSpanFactory.buildSpanBo(tSpan.build(), attribute);
    }

    public SpanBo randomComplexSpan() {
        PSpan.Builder pSpan = randomTSpan.randomPSpan();
        PSpanEvent spanEvent1 = randomTSpan.randomTSpanEvent((short) 1);
        PSpanEvent spanEvent2 = randomTSpan.randomTSpanEvent((short) 2);
        PSpanEvent spanEvent3 = randomTSpan.randomTSpanEvent((short) 3);
        PSpanEvent spanEvent4 = randomTSpan.randomTSpanEvent((short) 5);

        pSpan.addAllSpanEvent(List.of(spanEvent1, spanEvent2, spanEvent3, spanEvent4));
        return grpcSpanFactory.buildSpanBo(pSpan.build(), attribute);
    }

    private SpanChunkBo randomSpanChunk() {
        PSpanChunk.Builder spanChunk = randomTSpan.randomTSpanChunk();
        return grpcSpanFactory.buildSpanChunkBo(spanChunk.build(), attribute);
    }

    public SpanChunkBo randomComplexSpanChunk() {
        PSpanChunk.Builder spanChunk = randomTSpan.randomTSpanChunk();
        PSpanEvent spanEvent1 = randomTSpan.randomTSpanEvent((short) 1);
        PSpanEvent spanEvent2 = randomTSpan.randomTSpanEvent((short) 2);
        PSpanEvent spanEvent3 = randomTSpan.randomTSpanEvent((short) 3);
        PSpanEvent spanEvent4 = randomTSpan.randomTSpanEvent((short) 5);

        spanChunk.addAllSpanEvent(List.of(spanEvent1, spanEvent2, spanEvent3, spanEvent4));
        return grpcSpanFactory.buildSpanChunkBo(spanChunk.build(), attribute);
    }


    private void assertSpan(SpanBo spanBo) {
        spanBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<>(spanBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext(spanBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanBo.getCollectorAcceptTime());

        SpanBo decode = (SpanBo) spanDecoder.decode(qualifier, column, decodingContext);

        List<String> notSerializedField = Lists.newArrayList("parentApplicationName", "parentApplicationServiceType");
        List<String> excludeField = List.of("annotationBoList", "spanEventBoList", "agentName");
        notSerializedField.addAll(excludeField);
        Assertions.assertThat(decode)
                .usingRecursiveComparison()
                .ignoringFields(notSerializedField.toArray(new String[0]))
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
        spanChunkBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanChunkBo> encodingContext = new SpanEncodingContext<>(spanChunkBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanChunkQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanChunkColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext(spanChunkBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanChunkBo.getCollectorAcceptTime());

        SpanChunkBo decode = (SpanChunkBo) spanDecoder.decode(qualifier, column, decodingContext);
        // TODO Check CI log
        // logger.debug("spanChunk dump \noriginal spanChunkBo:{} \ndecode spanChunkBo:{} ", spanChunkBo, decode);

        List<String> notSerializedField = Lists.newArrayList("endPoint", "serviceType", "applicationServiceType");
        List<String> excludeField = List.of("spanEventBoList", "localAsyncId");
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
}