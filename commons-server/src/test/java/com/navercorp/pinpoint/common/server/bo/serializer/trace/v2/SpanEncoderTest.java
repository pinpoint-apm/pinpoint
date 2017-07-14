package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.bo.RandomTSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEncoderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int REPEAT_COUNT = 10;

    private final RandomTSpan randomTSpan = new RandomTSpan();
    private final SpanFactory spanFactory = new SpanFactory();

    private SpanEncoder spanEncoder = new SpanEncoderV0();
    private SpanDecoder spanDecoder = new SpanDecoderV0();


    @Test
    public void testEncodeSpanColumnValue_simpleSpan() throws Exception {

        SpanBo spanBo = randomSpan();
        assertSpan(spanBo);

    }


    @Test
    public void testEncodeSpanColumnValue_simpleSpan_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testEncodeSpanColumnValue_simpleSpan();
        }
    }


    @Test
    public void testEncodeSpanColumnValue_complexSpan() throws Exception {

        SpanBo spanBo = randomComplexSpan();
        assertSpan(spanBo);

    }

    @Test
    public void testEncodeSpanColumnValue_complexSpan_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testEncodeSpanColumnValue_complexSpan();
        }
    }

    @Test
    public void testEncodeSpanColumnValue_simpleSpanChunk() throws Exception {

        SpanChunkBo spanChunkBo = randomSpanChunk();
        assertSpanChunk(spanChunkBo);

    }

    @Test
    public void testEncodeSpanColumnValue_simpleSpanChunk_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testEncodeSpanColumnValue_simpleSpanChunk();
        }
    }


    @Test
    public void testEncodeSpanColumnValue_complexSpanChunk() throws Exception {

        SpanChunkBo spanChunkBo = randomComplexSpanChunk();
        assertSpanChunk(spanChunkBo);

    }

    @Test
    public void testEncodeSpanColumnValue_complexSpanChunk_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testEncodeSpanColumnValue_complexSpanChunk();
        }
    }

    private long getCollectorAcceptTime() {
        long currentTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        long randomSeed = RandomUtils.nextLong(0, TimeUnit.DAYS.toMillis(60));
        return currentTime - randomSeed;
    }

    private OffsetFixedBuffer wrapBuffer(ByteBuffer byteBuffer) {
        return new OffsetFixedBuffer(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.remaining());
    }

    private SpanBo randomSpan() {
        TSpan tSpan = randomTSpan.randomTSpan();
        return spanFactory.buildSpanBo(tSpan);
    }

    public SpanBo randomComplexSpan() {
        TSpan tSpan = randomTSpan.randomTSpan();
        TSpanEvent tSpanEvent1 = randomTSpan.randomTSpanEvent((short) 1);
        TSpanEvent tSpanEvent2 = randomTSpan.randomTSpanEvent((short) 2);
        TSpanEvent tSpanEvent3 = randomTSpan.randomTSpanEvent((short) 3);
        TSpanEvent tSpanEvent4 = randomTSpan.randomTSpanEvent((short) 5);

        tSpan.setSpanEventList(Lists.newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));
        return spanFactory.buildSpanBo(tSpan);
    }

    private SpanChunkBo randomSpanChunk() {
        TSpanChunk tSpanChunk = randomTSpan.randomTSpanChunk();
        return spanFactory.buildSpanChunkBo(tSpanChunk);
    }

    public SpanChunkBo randomComplexSpanChunk() {
        TSpanChunk tSpanChunk = randomTSpan.randomTSpanChunk();
        TSpanEvent tSpanEvent1 = randomTSpan.randomTSpanEvent((short) 1);
        TSpanEvent tSpanEvent2 = randomTSpan.randomTSpanEvent((short) 2);
        TSpanEvent tSpanEvent3 = randomTSpan.randomTSpanEvent((short) 3);
        TSpanEvent tSpanEvent4 = randomTSpan.randomTSpanEvent((short) 5);

        tSpanChunk.setSpanEventList(Lists.newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));
        return spanFactory.buildSpanChunkBo(tSpanChunk);
    }


    private void assertSpan(SpanBo spanBo) {
        spanBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<SpanBo>(spanBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext();
        decodingContext.setTransactionId(spanBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanBo.getCollectorAcceptTime());

        SpanBo decode = (SpanBo) spanDecoder.decode(qualifier, column, decodingContext);

        logger.debug("span dump \noriginal spanBo:{} \ndecode spanBo:{} ", spanBo, decode);

        List<String> notSerializedField = Lists.newArrayList("parentApplicationId", "parentApplicationServiceType");
        List<String> excludeField = Lists.newArrayList("annotationBoList", "spanEventBoList");
        notSerializedField.addAll(excludeField);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(decode, spanBo, notSerializedField));

        logger.debug("{} {}", spanBo.getAnnotationBoList(), decode.getAnnotationBoList());
        Assert.assertTrue("annotation", EqualsBuilder.reflectionEquals(spanBo.getAnnotationBoList(), decode.getAnnotationBoList()));

        List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        List<SpanEventBo> decodedSpanEventBoList = decode.getSpanEventBoList();
        Assert.assertTrue(EqualsBuilder.reflectionEquals(spanEventBoList, decodedSpanEventBoList));
    }

    private void assertSpanChunk(SpanChunkBo spanChunkBo) {
        spanChunkBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanChunkBo> encodingContext = new SpanEncodingContext<SpanChunkBo>(spanChunkBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanChunkQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanChunkColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext();
        decodingContext.setTransactionId(spanChunkBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanChunkBo.getCollectorAcceptTime());

        SpanChunkBo decode = (SpanChunkBo) spanDecoder.decode(qualifier, column, decodingContext);

        logger.debug("spanChunk dump \noriginal spanChunkBo:{} \ndecode spanChunkBo:{} ", spanChunkBo, decode);

        List<String> notSerializedField = Lists.newArrayList("endPoint", "serviceType", "applicationServiceType");
        List<String> excludeField = Lists.newArrayList("spanEventBoList");
        notSerializedField.addAll(excludeField);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(decode, spanChunkBo, notSerializedField));


        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        List<SpanEventBo> decodedSpanEventBoList = decode.getSpanEventBoList();
        Assert.assertTrue(EqualsBuilder.reflectionEquals(spanEventBoList, decodedSpanEventBoList));

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