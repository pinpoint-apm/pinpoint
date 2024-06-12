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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanFactoryAssert {

    public void assertSpan(TSpan tSpan, SpanBo spanBo) {
        Assertions.assertEquals(tSpan.getAgentId(), spanBo.getAgentId().value());
        Assertions.assertEquals(tSpan.getApplicationName(), spanBo.getApplicationName());
        Assertions.assertEquals(tSpan.getAgentStartTime(), spanBo.getAgentStartTime());

        TransactionId transactionId = spanBo.getTransactionId();
        ByteBuffer byteBuffer = TransactionIdUtils.formatByteBuffer(transactionId.getAgentId(), transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
        Assertions.assertEquals(ByteBuffer.wrap(tSpan.getTransactionId()), byteBuffer);

        Assertions.assertEquals(tSpan.getSpanId(), spanBo.getSpanId());
        Assertions.assertEquals(tSpan.getParentSpanId(), spanBo.getParentSpanId());
        Assertions.assertEquals(tSpan.getStartTime(), spanBo.getStartTime());
        Assertions.assertEquals(tSpan.getElapsed(), spanBo.getElapsed());
        Assertions.assertEquals(tSpan.getElapsed(), spanBo.getElapsed());
        Assertions.assertEquals(tSpan.getRpc(), spanBo.getRpc());

        Assertions.assertEquals(tSpan.getServiceType(), spanBo.getServiceType());
        Assertions.assertEquals(tSpan.getEndPoint(), spanBo.getEndPoint());
        Assertions.assertEquals(tSpan.getRemoteAddr(), spanBo.getRemoteAddr());

        assertAnnotation(tSpan.getAnnotations(), spanBo.getAnnotationBoList());

        Assertions.assertEquals(tSpan.getFlag(), spanBo.getFlag());
        Assertions.assertEquals(tSpan.getErr(), spanBo.getErrCode());

        Assertions.assertEquals(tSpan.getParentApplicationName(), spanBo.getParentApplicationName());
        Assertions.assertEquals(tSpan.getParentApplicationType(), spanBo.getParentApplicationServiceType());
        Assertions.assertEquals(tSpan.getAcceptorHost(), spanBo.getAcceptorHost());

        Assertions.assertEquals(tSpan.getApiId(), spanBo.getApiId());
        Assertions.assertEquals(tSpan.getApplicationServiceType(), spanBo.getApplicationServiceType());

        List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        List<TSpanEvent> spanEventList = tSpan.getSpanEventList();
        assertSpanEventList(spanEventBoList, spanEventList);


        boolean hasException = tSpan.getExceptionInfo() != null;
        Assertions.assertEquals(hasException, spanBo.hasException());
        if (hasException) {
            Assertions.assertEquals(tSpan.getExceptionInfo().getIntValue(), spanBo.getExceptionId());
            Assertions.assertEquals(tSpan.getExceptionInfo().getStringValue(), spanBo.getExceptionMessage());
        }

        Assertions.assertEquals(tSpan.getLoggingTransactionInfo(), spanBo.getLoggingTransactionInfo());

    }

    public void assertAnnotation(List<TAnnotation> tAnnotationList, List<AnnotationBo> annotationBoList) {
        if (CollectionUtils.isEmpty(tAnnotationList) && CollectionUtils.isEmpty(annotationBoList)) {
            return;
        }
        Assertions.assertEquals(tAnnotationList.size(), annotationBoList.size());
        if (tAnnotationList.isEmpty()) {
            return;
        }


        for (int i = 0; i < tAnnotationList.size(); i++) {
            TAnnotation tAnnotation = tAnnotationList.get(i);
            AnnotationBo annotationBo = annotationBoList.get(i);

            Assertions.assertEquals(tAnnotation.getKey(), annotationBo.getKey());
            Assertions.assertEquals(tAnnotation.getValue().getStringValue(), annotationBo.getValue());
        }
    }

    public void assertSpanEvent(TSpanEvent tSpanEvent, SpanEventBo spanEventBo) {
        Assertions.assertEquals(tSpanEvent.getSequence(), spanEventBo.getSequence());
        Assertions.assertEquals(tSpanEvent.getStartElapsed(), spanEventBo.getStartElapsed());
        Assertions.assertEquals(tSpanEvent.getEndElapsed(), spanEventBo.getEndElapsed());

        Assertions.assertEquals(tSpanEvent.getServiceType(), spanEventBo.getServiceType());
        Assertions.assertEquals(tSpanEvent.getEndPoint(), spanEventBo.getEndPoint());

        assertAnnotation(tSpanEvent.getAnnotations(), spanEventBo.getAnnotationBoList());

        Assertions.assertEquals(tSpanEvent.getDepth(), spanEventBo.getDepth());
        Assertions.assertEquals(tSpanEvent.getNextSpanId(), spanEventBo.getNextSpanId());
        Assertions.assertEquals(tSpanEvent.getDestinationId(), spanEventBo.getDestinationId());

        Assertions.assertEquals(tSpanEvent.getApiId(), spanEventBo.getApiId());

        boolean hasException = tSpanEvent.getExceptionInfo() != null;
        Assertions.assertEquals(hasException, spanEventBo.hasException());
        if (hasException) {
            Assertions.assertEquals(tSpanEvent.getExceptionInfo().getIntValue(), spanEventBo.getExceptionId());
            Assertions.assertEquals(tSpanEvent.getExceptionInfo().getStringValue(), spanEventBo.getExceptionMessage());
        }

        Assertions.assertEquals(tSpanEvent.getNextAsyncId(), spanEventBo.getNextAsyncId());

    }


    public void assertSpanChunk(TSpanChunk tSpanChunk, SpanChunkBo spanChunkBo) {
        Assertions.assertEquals(tSpanChunk.getAgentId(), spanChunkBo.getAgentId().value());
        Assertions.assertEquals(tSpanChunk.getApplicationName(), spanChunkBo.getApplicationName());
        Assertions.assertEquals(tSpanChunk.getAgentStartTime(), spanChunkBo.getAgentStartTime());


        TransactionId transactionId = spanChunkBo.getTransactionId();
        ByteBuffer byteBuffer = TransactionIdUtils.formatByteBuffer(transactionId.getAgentId(), transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
        Assertions.assertEquals(ByteBuffer.wrap(tSpanChunk.getTransactionId()), byteBuffer);

        Assertions.assertEquals(tSpanChunk.getSpanId(), spanChunkBo.getSpanId());

        Assertions.assertEquals(tSpanChunk.getEndPoint(), spanChunkBo.getEndPoint());
        Assertions.assertEquals(tSpanChunk.getApplicationServiceType(), spanChunkBo.getApplicationServiceType());


        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        List<TSpanEvent> spanEventList = tSpanChunk.getSpanEventList();
        assertSpanEventList(spanEventBoList, spanEventList);

    }

    private void assertSpanEventList(List<SpanEventBo> spanEventBoList, List<TSpanEvent> spanEventList) {
        Assertions.assertEquals(CollectionUtils.isEmpty(spanEventBoList), CollectionUtils.isEmpty(spanEventList));
        if (CollectionUtils.isNotEmpty(spanEventBoList)) {
            Map<Integer, SpanEventBo> spanEventBoMap = new HashMap<>();
            for (SpanEventBo spanEventBo : spanEventBoList) {
                spanEventBoMap.put((int) spanEventBo.getSequence(), spanEventBo);
            }

            for (TSpanEvent tSpanEvent : spanEventList) {
                SpanEventBo spanEventBo = spanEventBoMap.get((int) tSpanEvent.getSequence());
                Assertions.assertNotNull(spanEventBo);
                assertSpanEvent(tSpanEvent, spanEventBo);
            }
        }
    }
}
