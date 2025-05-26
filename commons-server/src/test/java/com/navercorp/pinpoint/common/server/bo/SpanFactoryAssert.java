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
import com.navercorp.pinpoint.grpc.trace.PAcceptEvent;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PMessageEvent;
import com.navercorp.pinpoint.grpc.trace.PParentInfo;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanFactoryAssert {

    public void assertSpan(PSpan pSpan, SpanBo spanBo) {
        TransactionId transactionId = spanBo.getTransactionId();
        PTransactionId pTransactionId = pSpan.getTransactionId();
        assertTransactionId(transactionId, pTransactionId);

        Assertions.assertEquals(pSpan.getSpanId(), spanBo.getSpanId());
        Assertions.assertEquals(pSpan.getParentSpanId(), spanBo.getParentSpanId());
        Assertions.assertEquals(pSpan.getStartTime(), spanBo.getStartTime());
        Assertions.assertEquals(pSpan.getElapsed(), spanBo.getElapsed());
        Assertions.assertEquals(pSpan.getElapsed(), spanBo.getElapsed());
        if (pSpan.hasAcceptEvent()) {
            PAcceptEvent acceptEvent = pSpan.getAcceptEvent();
            Assertions.assertEquals(acceptEvent.getRpc(), spanBo.getRpc());
            Assertions.assertEquals(acceptEvent.getEndPoint(), spanBo.getEndPoint());
            Assertions.assertEquals(acceptEvent.getRemoteAddr(), spanBo.getRemoteAddr());

            PParentInfo parentInfo = acceptEvent.getParentInfo();
            Assertions.assertEquals(parentInfo.getParentApplicationName(), spanBo.getParentApplicationName());
            Assertions.assertEquals(parentInfo.getParentApplicationType(), spanBo.getParentApplicationServiceType());
            Assertions.assertEquals(parentInfo.getAcceptorHost(), spanBo.getAcceptorHost());
        }
        Assertions.assertEquals(pSpan.getServiceType(), spanBo.getServiceType());

        assertAnnotation(pSpan.getAnnotationList(), spanBo.getAnnotationBoList());

        Assertions.assertEquals(pSpan.getFlag(), spanBo.getFlag());
        Assertions.assertEquals(pSpan.getErr(), spanBo.getErrCode());

        Assertions.assertEquals(pSpan.getApiId(), spanBo.getApiId());
        Assertions.assertEquals(pSpan.getApplicationServiceType(), spanBo.getApplicationServiceType());

        List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        List<PSpanEvent> spanEventList = pSpan.getSpanEventList();
        assertSpanEventList(spanEventBoList, spanEventList);

        boolean hasException = pSpan.hasExceptionInfo();
        Assertions.assertEquals(hasException, spanBo.hasException());
        if (hasException) {
            PIntStringValue exceptionInfo = pSpan.getExceptionInfo();
            ExceptionInfo exceptionInfoBo = spanBo.getExceptionInfo();
            Assertions.assertEquals(exceptionInfo.getIntValue(), exceptionInfoBo.id());
            Assertions.assertEquals(exceptionInfo.getStringValue().getValue(), exceptionInfoBo.message());
        }

        Assertions.assertEquals(pSpan.getLoggingTransactionInfo(), spanBo.getLoggingTransactionInfo());

    }

    private void assertTransactionId(TransactionId transactionId, PTransactionId pTransactionId) {
        Assertions.assertEquals(transactionId.getAgentId(), pTransactionId.getAgentId());
        Assertions.assertEquals(transactionId.getAgentStartTime(), pTransactionId.getAgentStartTime());
        Assertions.assertEquals(transactionId.getTransactionSequence(), pTransactionId.getSequence());
    }

    public void assertAnnotation(List<PAnnotation> tAnnotationList, List<AnnotationBo> annotationBoList) {
        if (CollectionUtils.isEmpty(tAnnotationList) && CollectionUtils.isEmpty(annotationBoList)) {
            return;
        }
        Assertions.assertEquals(tAnnotationList.size(), annotationBoList.size());
        if (tAnnotationList.isEmpty()) {
            return;
        }


        for (int i = 0; i < tAnnotationList.size(); i++) {
            PAnnotation tAnnotation = tAnnotationList.get(i);
            AnnotationBo annotationBo = annotationBoList.get(i);

            Assertions.assertEquals(tAnnotation.getKey(), annotationBo.getKey());
            Assertions.assertEquals(tAnnotation.getValue().getStringValue(), annotationBo.getValue());
        }
    }

    public void assertSpanEvent(PSpanEvent pSpanEvent, int delta, SpanEventBo spanEventBo) {
        Assertions.assertEquals(pSpanEvent.getSequence(), spanEventBo.getSequence());
        Assertions.assertEquals(pSpanEvent.getStartElapsed() + delta, spanEventBo.getStartElapsed());
        Assertions.assertEquals(pSpanEvent.getEndElapsed(), spanEventBo.getEndElapsed());

        Assertions.assertEquals(pSpanEvent.getServiceType(), spanEventBo.getServiceType());
        assertAnnotation(pSpanEvent.getAnnotationList(), spanEventBo.getAnnotationBoList());

        Assertions.assertEquals(pSpanEvent.getDepth(), spanEventBo.getDepth());

        if (pSpanEvent.getNextEvent().hasMessageEvent()) {
            PMessageEvent nextEvent = pSpanEvent.getNextEvent().getMessageEvent();
            Assertions.assertEquals(nextEvent.getEndPoint(), spanEventBo.getEndPoint());
            Assertions.assertEquals(nextEvent.getNextSpanId(), spanEventBo.getNextSpanId());
            Assertions.assertEquals(nextEvent.getDestinationId(), spanEventBo.getDestinationId());
        }

        Assertions.assertEquals(pSpanEvent.getApiId(), spanEventBo.getApiId());

        boolean hasException = pSpanEvent.hasExceptionInfo();
        Assertions.assertEquals(hasException, spanEventBo.hasException());
        if (hasException) {
            PIntStringValue exceptionInfo = pSpanEvent.getExceptionInfo();
            ExceptionInfo exceptionInfoBo = spanEventBo.getExceptionInfo();
            Assertions.assertEquals(exceptionInfo.getIntValue(), exceptionInfoBo.id());
            Assertions.assertEquals(exceptionInfo.getStringValue().getValue(), exceptionInfoBo.message());
        }

        Assertions.assertEquals(pSpanEvent.getAsyncEvent(), spanEventBo.getNextAsyncId());

    }


    public void assertSpanChunk(PSpanChunk pSpanChunk, SpanChunkBo spanChunkBo) {


        TransactionId transactionId = spanChunkBo.getTransactionId();
        PTransactionId pTransactionId = pSpanChunk.getTransactionId();
        assertTransactionId(transactionId, pTransactionId);


        Assertions.assertEquals(pSpanChunk.getSpanId(), spanChunkBo.getSpanId());

        Assertions.assertEquals(pSpanChunk.getEndPoint(), spanChunkBo.getEndPoint());
        Assertions.assertEquals(pSpanChunk.getApplicationServiceType(), spanChunkBo.getApplicationServiceType());


        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        List<PSpanEvent> spanEventList = pSpanChunk.getSpanEventList();
        assertSpanEventList(spanEventBoList, spanEventList);

    }

    private void assertSpanEventList(List<SpanEventBo> spanEventBoList, List<PSpanEvent> spanEventList) {
        Assertions.assertEquals(spanEventBoList.size(), spanEventList.size());

        if (CollectionUtils.isNotEmpty(spanEventBoList)) {
            Map<Integer, SpanEventBo> spanEventBoMap = toMap(spanEventBoList);
            SpanEventBo prev = null;
            int startTime;
            for (PSpanEvent pSpanEvent : spanEventList) {
                SpanEventBo spanEventBo = spanEventBoMap.get(pSpanEvent.getSequence());
                startTime = getStartTimeDelta(prev);
                prev = spanEventBo;
                Assertions.assertNotNull(spanEventBo);
                assertSpanEvent(pSpanEvent, startTime, spanEventBo);
            }
        }
    }

    private Map<Integer, SpanEventBo> toMap(List<SpanEventBo> spanEventBoList) {
        Map<Integer, SpanEventBo> spanEventBoMap = new HashMap<>();
        for (SpanEventBo spanEventBo : spanEventBoList) {
            spanEventBoMap.put((int) spanEventBo.getSequence(), spanEventBo);
        }
        return spanEventBoMap;
    }

    private int getStartTimeDelta(SpanEventBo prevSpanEvent) {
        if (prevSpanEvent == null) {
            return 0;
        } else {
            return prevSpanEvent.getStartElapsed();
        }
    }
}
