package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.bo.RandomTSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import com.navercorp.pinpoint.thrift.dto.TSpan;
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
    public void encodeSpanColumnValue() throws Exception {


        SpanBo spanBo = randomSpan();
        spanBo.setCollectorAcceptTime(getCollectorAcceptTime());

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<>(spanBo);
        Buffer qualifier = wrapBuffer(spanEncoder.encodeSpanQualifier(encodingContext));
        Buffer column = wrapBuffer(spanEncoder.encodeSpanColumnValue(encodingContext));

        SpanDecodingContext decodingContext = new SpanDecodingContext();
        decodingContext.setTransactionId(spanBo.getTransactionId());
        decodingContext.setCollectorAcceptedTime(spanBo.getCollectorAcceptTime());

        SpanBo decode = (SpanBo) spanDecoder.decode(qualifier, column, decodingContext);

        logger.debug("span dump \noriginal spanBo:{} \ndecode spanBo:{} ", spanBo, decode);

        List<String> excludeField = Lists.newArrayList("parentApplicationId", "parentApplicationServiceType", "annotationBoList");
        Assert.assertTrue(EqualsBuilder.reflectionEquals(decode, spanBo, excludeField));

        logger.debug("{} {}", spanBo.getAnnotationBoList(), decode.getAnnotationBoList());
        Assert.assertTrue("annotation", EqualsBuilder.reflectionEquals(spanBo.getAnnotationBoList(), decode.getAnnotationBoList()));

    }

    private long getCollectorAcceptTime() {
        long currentTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        long randomSeed = RandomUtils.nextLong(0, TimeUnit.DAYS.toMillis(60));
        return currentTime - randomSeed;
    }

    @Test
    public void encodeSpanColumnValue_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            encodeSpanColumnValue();
        }
    }

    private OffsetFixedBuffer wrapBuffer(ByteBuffer byteBuffer) {
        return new OffsetFixedBuffer(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.remaining());
    }

    public SpanBo randomSpan() {
        TSpan tSpan = randomTSpan.randomTSpan();
        return spanFactory.buildSpanBo(tSpan);
    }

}