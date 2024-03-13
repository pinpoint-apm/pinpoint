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

package com.navercorp.pinpoint.common.server.bo.thrift;


import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AnnotationComparator;
import com.navercorp.pinpoint.common.server.bo.AnnotationFactory;
import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventComparator;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TLocalAsyncId;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanFactory {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AnnotationFactory<TAnnotation> annotationFactory = new AnnotationFactory<>(new ThriftAnnotationHandler());

    public SpanFactory() {
    }

    public SpanBo buildSpanBo(TSpan tSpan, long acceptedTime, SpanEventFilter spanEventFilter) {

        final SpanBo spanBo = newSpanBo(tSpan);

        List<TSpanEvent> spanEventList = tSpan.getSpanEventList();
        List<SpanEventBo> spanEventBoList = buildSpanEventBoList(spanEventList, spanEventFilter);
        spanBo.addSpanEventBoList(spanEventBoList);

        spanBo.setCollectorAcceptTime(acceptedTime);

        return spanBo;
    }

    // for test
    SpanBo newSpanBo(TSpan tSpan) {
        final SpanBo spanBo = new SpanBo();
        spanBo.setAgentId(tSpan.getAgentId());
        spanBo.setApplicationName(tSpan.getApplicationName());
        spanBo.setAgentStartTime(tSpan.getAgentStartTime());

        final TransactionId transactionId = newTransactionId(tSpan.getTransactionId(), spanBo.getAgentId());
        spanBo.setTransactionId(transactionId);

        spanBo.setSpanId(tSpan.getSpanId());
        spanBo.setParentSpanId(tSpan.getParentSpanId());

        spanBo.setStartTime(tSpan.getStartTime());
        spanBo.setElapsed(tSpan.getElapsed());

        spanBo.setRpc(tSpan.getRpc());

        spanBo.setServiceType(tSpan.getServiceType());
        spanBo.setEndPoint(tSpan.getEndPoint());
        spanBo.setFlag(tSpan.getFlag());
        spanBo.setApiId(tSpan.getApiId());

        spanBo.setErrCode(tSpan.getErr());

        spanBo.setAcceptorHost(tSpan.getAcceptorHost());
        spanBo.setRemoteAddr(tSpan.getRemoteAddr());

        spanBo.setLoggingTransactionInfo(tSpan.getLoggingTransactionInfo());

        // FIXME (2015.03) Legacy - applicationServiceType added in v1.1.0
        // applicationServiceType is not saved for older versions where applicationServiceType does not exist.
        if (tSpan.isSetApplicationServiceType()) {
            spanBo.setApplicationServiceType(tSpan.getApplicationServiceType());
        } else {
            spanBo.setApplicationServiceType(tSpan.getServiceType());
        }

        spanBo.setParentApplicationName(tSpan.getParentApplicationName());
        spanBo.setParentApplicationServiceType(tSpan.getParentApplicationType());

        // FIXME span.errCode contains error of span and spanEvent
        // because exceptionInfo is the error information of span itself, exceptionInfo can be null even if errCode is not 0
        final TIntStringValue exceptionInfo = tSpan.getExceptionInfo();
        if (exceptionInfo != null) {
            spanBo.setExceptionInfo(exceptionInfo.getIntValue(), exceptionInfo.getStringValue());
        }

        List<AnnotationBo> annotationBoList = buildAnnotationList(tSpan.getAnnotations());
        spanBo.setAnnotationBoList(annotationBoList);

        return spanBo;
    }


    private void bind(SpanEventBo spanEvent, TSpanEvent tSpanEvent) {

        spanEvent.setSequence(tSpanEvent.getSequence());

        spanEvent.setStartElapsed(tSpanEvent.getStartElapsed());
        spanEvent.setEndElapsed(tSpanEvent.getEndElapsed());

        spanEvent.setServiceType(tSpanEvent.getServiceType());


        spanEvent.setDestinationId(tSpanEvent.getDestinationId());

        spanEvent.setEndPoint(tSpanEvent.getEndPoint());
        spanEvent.setApiId(tSpanEvent.getApiId());

        if (tSpanEvent.isSetDepth()) {
            spanEvent.setDepth(tSpanEvent.getDepth());
        }

        if (tSpanEvent.isSetNextSpanId()) {
            spanEvent.setNextSpanId(tSpanEvent.getNextSpanId());
        }

        List<AnnotationBo> annotationList = buildAnnotationList(tSpanEvent.getAnnotations());
        spanEvent.setAnnotationBoList(annotationList);

        final TIntStringValue exceptionInfo = tSpanEvent.getExceptionInfo();
        if (exceptionInfo != null) {
            spanEvent.setExceptionInfo(exceptionInfo.getIntValue(), exceptionInfo.getStringValue());
        }

        if (tSpanEvent.isSetNextAsyncId()) {
            spanEvent.setNextAsyncId(tSpanEvent.getNextAsyncId());
        }

        // async id
//        if (localAsyncId == null) {
//            if (tSpanEvent.isSetAsyncId()) {
//                spanEvent.setAsyncId(tSpanEvent.getAsyncId());
//            }
//            if (tSpanEvent.isSetAsyncSequence()) {
//                spanEvent.setAsyncSequence(tSpanEvent.getAsyncSequence());
//            }
//        } else {
//            spanEvent.setAsyncId(localAsyncId.getAsyncId());
//            spanEvent.setAsyncSequence((short) localAsyncId.getSequence());
//        }
    }

    public SpanChunkBo buildSpanChunkBo(TSpanChunk tSpanChunk, long acceptedTime, SpanEventFilter spanEventFilter) {
        final SpanChunkBo spanChunkBo = newSpanChunkBo(tSpanChunk);
        final LocalAsyncIdBo localAsyncIdBo = getLocalAsyncId(tSpanChunk);
        if (localAsyncIdBo != null) {
            spanChunkBo.setLocalAsyncId(localAsyncIdBo);
        }

        List<TSpanEvent> spanEventList = tSpanChunk.getSpanEventList();
        List<SpanEventBo> spanEventBoList = buildSpanEventBoList(spanEventList, spanEventFilter);
        spanChunkBo.addSpanEventBoList(spanEventBoList);
        spanChunkBo.setCollectorAcceptTime(acceptedTime);

        return spanChunkBo;
    }

    private LocalAsyncIdBo getLocalAsyncId(TSpanChunk tSpanChunk) {
        final TLocalAsyncId localAsyncId = tSpanChunk.getLocalAsyncId();
        if (localAsyncId != null) {
            return new LocalAsyncIdBo(localAsyncId.getAsyncId(), localAsyncId.getSequence());
        }
        return null;
    }



    // for test
    SpanChunkBo newSpanChunkBo(TSpanChunk tSpanChunk) {
        final SpanChunkBo spanChunkBo = new SpanChunkBo();
        spanChunkBo.setAgentId(tSpanChunk.getAgentId());
        spanChunkBo.setApplicationName(tSpanChunk.getApplicationName());
        spanChunkBo.setAgentStartTime(tSpanChunk.getAgentStartTime());
        spanChunkBo.setServiceType(tSpanChunk.getServiceType());
        if (tSpanChunk.isSetApplicationServiceType()) {
            spanChunkBo.setApplicationServiceType(tSpanChunk.getApplicationServiceType());
        } else {
            spanChunkBo.setApplicationServiceType(tSpanChunk.getServiceType());
        }

        TransactionId transactionId = newTransactionId(tSpanChunk.getTransactionId(), spanChunkBo.getAgentId());
        spanChunkBo.setTransactionId(transactionId);


        spanChunkBo.setSpanId(tSpanChunk.getSpanId());
        spanChunkBo.setEndPoint(tSpanChunk.getEndPoint());
        return spanChunkBo;
    }

    private TransactionId newTransactionId(byte[] transactionIdBytes, String spanAgentId) {
        return TransactionIdUtils.parseTransactionId(transactionIdBytes, spanAgentId);
    }


    private List<SpanEventBo> buildSpanEventBoList(List<TSpanEvent> spanEventList, SpanEventFilter spanEventFilter) {
        if (CollectionUtils.isEmpty(spanEventList)) {
            return new ArrayList<>();
        }
        List<SpanEventBo> spanEventBoList = new ArrayList<>(spanEventList.size());
        for (TSpanEvent tSpanEvent : spanEventList) {
            final SpanEventBo spanEventBo = buildSpanEventBo(tSpanEvent);
            if (!spanEventFilter.filter(spanEventBo)) {
                continue;
            }
            spanEventBoList.add(spanEventBo);
        }

        spanEventBoList.sort(SpanEventComparator.INSTANCE);
        return spanEventBoList;
    }

    private List<AnnotationBo> buildAnnotationList(List<TAnnotation> tAnnotationList) {
        if (tAnnotationList == null) {
            return new ArrayList<>();
        }
        List<AnnotationBo> boList = new ArrayList<>(tAnnotationList.size());
        for (TAnnotation tAnnotation : tAnnotationList) {
            final AnnotationBo annotationBo = newAnnotationBo(tAnnotation);
            boList.add(annotationBo);
        }

        boList.sort(AnnotationComparator.INSTANCE);
        return boList;
    }

    // for test
    public SpanEventBo buildSpanEventBo(TSpanEvent tSpanEvent) {
        Objects.requireNonNull(tSpanEvent, "tSpanEvent");

        final SpanEventBo spanEvent = new SpanEventBo();
        bind(spanEvent, tSpanEvent);
        return spanEvent;
    }

    private AnnotationBo newAnnotationBo(TAnnotation tAnnotation) {
        Objects.requireNonNull(tAnnotation, "tAnnotation");

        return annotationFactory.buildAnnotation(tAnnotation);
    }

}
