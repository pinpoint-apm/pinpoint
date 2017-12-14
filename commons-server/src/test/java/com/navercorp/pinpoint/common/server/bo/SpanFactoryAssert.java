package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanFactoryAssert {

    public void assertSpan(TSpan tSpan, SpanBo spanBo) {
        Assert.assertEquals(tSpan.getAgentId(), spanBo.getAgentId());
        Assert.assertEquals(tSpan.getApplicationName(), spanBo.getApplicationId());
        Assert.assertEquals(tSpan.getAgentStartTime(), spanBo.getAgentStartTime());

        TransactionId transactionId = spanBo.getTransactionId();
        ByteBuffer byteBuffer = TransactionIdUtils.formatByteBuffer(transactionId.getAgentId(), transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
        Assert.assertEquals(ByteBuffer.wrap(tSpan.getTransactionId()), byteBuffer);

        Assert.assertEquals(tSpan.getSpanId(), spanBo.getSpanId());
        Assert.assertEquals(tSpan.getParentSpanId(), spanBo.getParentSpanId());
        Assert.assertEquals(tSpan.getStartTime(), spanBo.getStartTime());
        Assert.assertEquals(tSpan.getElapsed(), spanBo.getElapsed());
        Assert.assertEquals(tSpan.getElapsed(), spanBo.getElapsed());
        Assert.assertEquals(tSpan.getRpc(), spanBo.getRpc());

        Assert.assertEquals(tSpan.getServiceType(), spanBo.getServiceType());
        Assert.assertEquals(tSpan.getEndPoint(), spanBo.getEndPoint());
        Assert.assertEquals(tSpan.getRemoteAddr(), spanBo.getRemoteAddr());

        assertAnnotation(tSpan.getAnnotations(), spanBo.getAnnotationBoList());

        Assert.assertEquals(tSpan.getFlag(), spanBo.getFlag());
        Assert.assertEquals(tSpan.getErr(), spanBo.getErrCode());

        Assert.assertEquals(tSpan.getParentApplicationName(), spanBo.getParentApplicationId());
        Assert.assertEquals(tSpan.getParentApplicationType(), spanBo.getParentApplicationServiceType());
        Assert.assertEquals(tSpan.getAcceptorHost(), spanBo.getAcceptorHost());

        Assert.assertEquals(tSpan.getApiId(), spanBo.getApiId());
        Assert.assertEquals(tSpan.getApplicationServiceType(), spanBo.getApplicationServiceType());

        List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        List<TSpanEvent> spanEventList = tSpan.getSpanEventList();
        assertSpanEventList(spanEventBoList, spanEventList);


        boolean hasException = tSpan.getExceptionInfo() != null;
        Assert.assertEquals(hasException, spanBo.hasException());
        if (hasException) {
            Assert.assertEquals(tSpan.getExceptionInfo().getIntValue(), spanBo.getExceptionId());
            Assert.assertEquals(tSpan.getExceptionInfo().getStringValue(), spanBo.getExceptionMessage());
        }

        Assert.assertEquals(tSpan.getLoggingTransactionInfo(), spanBo.getLoggingTransactionInfo());

    }

    public void assertAnnotation(List<TAnnotation> tAnnotationList, List<AnnotationBo> annotationBoList) {
        if (CollectionUtils.isEmpty(tAnnotationList) && CollectionUtils.isEmpty(annotationBoList)) {
            return;
        }
        Assert.assertEquals(tAnnotationList.size(), annotationBoList.size());
        if (tAnnotationList.isEmpty()) {
            return;
        }


        for (int i = 0; i < tAnnotationList.size(); i++) {
            TAnnotation tAnnotation = tAnnotationList.get(i);
            AnnotationBo annotationBo = annotationBoList.get(i);

            Assert.assertEquals(tAnnotation.getKey(), annotationBo.getKey());
            Assert.assertEquals(tAnnotation.getValue().getStringValue(), annotationBo.getValue());
        }
    }

    public void assertSpanEvent(TSpanEvent tSpanEvent, SpanEventBo spanEventBo) {
        Assert.assertEquals(tSpanEvent.getSequence(), spanEventBo.getSequence());
        Assert.assertEquals(tSpanEvent.getStartElapsed(), spanEventBo.getStartElapsed());
        Assert.assertEquals(tSpanEvent.getEndElapsed(), spanEventBo.getEndElapsed());

        Assert.assertEquals(tSpanEvent.getRpc(), spanEventBo.getRpc());
        Assert.assertEquals(tSpanEvent.getServiceType(), spanEventBo.getServiceType());
        Assert.assertEquals(tSpanEvent.getEndPoint(), spanEventBo.getEndPoint());

        assertAnnotation(tSpanEvent.getAnnotations(), spanEventBo.getAnnotationBoList());

        Assert.assertEquals(tSpanEvent.getDepth(), spanEventBo.getDepth());
        Assert.assertEquals(tSpanEvent.getNextSpanId(), spanEventBo.getNextSpanId());
        Assert.assertEquals(tSpanEvent.getDestinationId(), spanEventBo.getDestinationId());

        Assert.assertEquals(tSpanEvent.getApiId(), spanEventBo.getApiId());

        boolean hasException = tSpanEvent.getExceptionInfo() != null;
        Assert.assertEquals(hasException, spanEventBo.hasException());
        if (hasException) {
            Assert.assertEquals(tSpanEvent.getExceptionInfo().getIntValue(), spanEventBo.getExceptionId());
            Assert.assertEquals(tSpanEvent.getExceptionInfo().getStringValue(), spanEventBo.getExceptionMessage());
        }

        Assert.assertEquals(tSpanEvent.getAsyncId(), spanEventBo.getAsyncId());
        Assert.assertEquals(tSpanEvent.getNextAsyncId(), spanEventBo.getNextAsyncId());
        Assert.assertEquals(tSpanEvent.getAsyncSequence(), spanEventBo.getAsyncSequence());
    }


    public void assertSpanChunk(TSpanChunk tSpanChunk, SpanChunkBo spanChunkBo) {
        Assert.assertEquals(tSpanChunk.getAgentId(), spanChunkBo.getAgentId());
        Assert.assertEquals(tSpanChunk.getApplicationName(), spanChunkBo.getApplicationId());
        Assert.assertEquals(tSpanChunk.getAgentStartTime(), spanChunkBo.getAgentStartTime());


        TransactionId transactionId = spanChunkBo.getTransactionId();
        ByteBuffer byteBuffer = TransactionIdUtils.formatByteBuffer(transactionId.getAgentId(), transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
        Assert.assertEquals(ByteBuffer.wrap(tSpanChunk.getTransactionId()), byteBuffer);

        Assert.assertEquals(tSpanChunk.getSpanId(), spanChunkBo.getSpanId());

        Assert.assertEquals(tSpanChunk.getEndPoint(), spanChunkBo.getEndPoint());
        Assert.assertEquals(tSpanChunk.getApplicationServiceType(), spanChunkBo.getApplicationServiceType());


        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        List<TSpanEvent> spanEventList = tSpanChunk.getSpanEventList();
        assertSpanEventList(spanEventBoList, spanEventList);

    }

    private void assertSpanEventList(List<SpanEventBo> spanEventBoList, List<TSpanEvent> spanEventList) {
        Assert.assertEquals(CollectionUtils.isEmpty(spanEventBoList), CollectionUtils.isEmpty(spanEventList));
        if (CollectionUtils.isNotEmpty(spanEventBoList)) {
            Map<Long, SpanEventBo> spanEventBoMap = new HashMap<Long, SpanEventBo>();
            for (int i = 0; i < spanEventBoList.size(); i++) {
                SpanEventBo spanEventBo = spanEventBoList.get(i);
                spanEventBoMap.put((long)spanEventBo.getSequence(), spanEventBo);
            }

            for (int i = 0; i < spanEventList.size(); i++) {
                TSpanEvent tSpanEvent = spanEventList.get(i);
                SpanEventBo spanEventBo = spanEventBoMap.get((long) tSpanEvent.getSequence());
                Assert.assertNotNull(spanEventBo);
                assertSpanEvent(tSpanEvent, spanEventBo);
            }
        }
    }
}
