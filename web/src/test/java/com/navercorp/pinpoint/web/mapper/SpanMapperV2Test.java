package com.navercorp.pinpoint.web.mapper;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoderV0;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncoderV0;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncodingContext;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanMapperV2Test {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SpanDecoderV0 decoder = new SpanDecoderV0();

    @Test
    public void test() {

        SpanBo span = new SpanBo();
        span.setServiceType((short) 1000);
        span.setExceptionInfo(1, "spanException");

        SpanEventBo firstSpanEventBo = new SpanEventBo();
        firstSpanEventBo.setExceptionInfo(2, "first");
        firstSpanEventBo.setEndElapsed(100);

        AnnotationBo annotationBo = newAnnotation(200, "annotation");
        firstSpanEventBo.setAnnotationBoList(Lists.<AnnotationBo>newArrayList(annotationBo));
        firstSpanEventBo.setServiceType((short) 1003);
        firstSpanEventBo.setSequence((short) 0);

        span.addSpanEvent(firstSpanEventBo);

        //// next
        SpanEventBo nextSpanEventBo = new SpanEventBo();
        nextSpanEventBo.setEndElapsed(200);
        nextSpanEventBo.setServiceType((short) 2003);
        nextSpanEventBo.setSequence((short) 1);


        span.addSpanEvent(nextSpanEventBo);

        SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<>(span);
        SpanEncoder encoder = new SpanEncoderV0();
        ByteBuffer byteBuffer = encoder.encodeSpanColumnValue(encodingContext);

        Buffer buffer = new OffsetFixedBuffer(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.remaining());

        SpanBo readSpan = new SpanBo();
        SpanDecodingContext decodingContext = new SpanDecodingContext();
        decoder.readSpanValue(buffer, readSpan, new SpanEventBo(), decodingContext);

        Assert.assertEquals(readSpan.getSpanEventBoList().size(), 2);


        // span
        Assert.assertEquals(readSpan.getServiceType(), 1000);
        Assert.assertEquals(readSpan.hasException(), true);
        Assert.assertEquals(readSpan.getExceptionId(), 1);
        Assert.assertEquals(readSpan.getExceptionMessage(), "spanException");

        List<SpanEventBo> spanEventBoList = readSpan.getSpanEventBoList();
        SpanEventBo readFirst = spanEventBoList.get(0);
        SpanEventBo readNext = spanEventBoList.get(1);

        Assert.assertEquals(readFirst.getEndElapsed(), 100);
        Assert.assertEquals(readNext.getEndElapsed(), 200);

        Assert.assertEquals(readFirst.getExceptionId(), 2);
        Assert.assertEquals(readNext.hasException(), false);

        Assert.assertEquals(readFirst.getServiceType(), 1003);
        Assert.assertEquals(readNext.getServiceType(), 2003);

        Assert.assertEquals(readFirst.getSequence(), 0);
        Assert.assertEquals(readNext.getSequence(), 1);

    }

    private AnnotationBo newAnnotation(int key, Object value) {
        AnnotationBo annotationBo = new AnnotationBo();
        annotationBo.setKey(key);
        annotationBo.setValue(value);
        return annotationBo;
    }

}