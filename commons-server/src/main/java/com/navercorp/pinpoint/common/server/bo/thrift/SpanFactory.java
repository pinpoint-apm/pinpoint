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


import com.google.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AnnotationComparator;
import com.navercorp.pinpoint.common.server.bo.AnnotationFactory;
import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventComparator;
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.EmptyAcceptedTimeService;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TLocalAsyncId;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SpanEventFilter spanEventFilter = new EmptySpanEventFilter();

    private AcceptedTimeService acceptedTimeService = new EmptyAcceptedTimeService();

    private final AnnotationFactory<TAnnotation> annotationFactory = new AnnotationFactory<>(new ThriftAnnotationHandler());

    // TODO
    private final boolean fastAsyncIdGen;

    public SpanFactory() {
        this(fastAsyncIdGen());
    }

    private static boolean fastAsyncIdGen() {
        final String fastAsyncIdGen = System.getProperty("collector.spanfactory.fastasyncidgen", "true");
        return Boolean.parseBoolean(fastAsyncIdGen);
    }

    public SpanFactory(boolean fastAsyncIdGen) {
        this.fastAsyncIdGen = fastAsyncIdGen;
    }

    @Autowired(required = false)
    public void setSpanEventFilter(SpanEventFilter spanEventFilter) {
        this.spanEventFilter = spanEventFilter;
    }

    @Autowired(required = false)
    public void setAcceptedTimeService(AcceptedTimeService acceptedTimeService) {
        this.acceptedTimeService = acceptedTimeService;
    }

    public SpanBo buildSpanBo(TSpan tSpan) {

        final SpanBo spanBo = newSpanBo(tSpan);

        List<TSpanEvent> spanEventList = tSpan.getSpanEventList();
        List<SpanEventBo> spanEventBoList = buildSpanEventBoList(spanEventList);
        spanBo.addSpanEventBoList(spanEventBoList);

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        spanBo.setCollectorAcceptTime(acceptedTime);

        return spanBo;
    }

    // for test
    SpanBo newSpanBo(TSpan tSpan) {
        final SpanBo spanBo = new SpanBo();
        spanBo.setAgentId(tSpan.getAgentId());
        spanBo.setApplicationId(tSpan.getApplicationName());
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

        spanBo.setParentApplicationId(tSpan.getParentApplicationName());
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

        spanEvent.setRpc(tSpanEvent.getRpc());
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

    public SpanChunkBo buildSpanChunkBo(TSpanChunk tSpanChunk) {
        final SpanChunkBo spanChunkBo = newSpanChunkBo(tSpanChunk);
        final LocalAsyncIdBo localAsyncIdBo = getLocalAsyncId(tSpanChunk);
        if (localAsyncIdBo != null) {
            spanChunkBo.setLocalAsyncId(localAsyncIdBo);
        }

        List<TSpanEvent> spanEventList = tSpanChunk.getSpanEventList();
        List<SpanEventBo> spanEventBoList = buildSpanEventBoList(spanEventList);
        spanChunkBo.addSpanEventBoList(spanEventBoList);


        long acceptedTime = acceptedTimeService.getAcceptedTime();
        spanChunkBo.setCollectorAcceptTime(acceptedTime);

        return spanChunkBo;
    }

    private LocalAsyncIdBo getLocalAsyncId(TSpanChunk tSpanChunk) {
        final TLocalAsyncId localAsyncId = tSpanChunk.getLocalAsyncId();
        if (localAsyncId != null) {
            return new LocalAsyncIdBo(localAsyncId.getAsyncId(), localAsyncId.getSequence());
        } else {
            return extractLocalAsyncId(tSpanChunk);
        }
    }

    // for compatibility
    // https://github.com/naver/pinpoint/issues/5156
    private LocalAsyncIdBo extractLocalAsyncId(TSpanChunk tSpanChunk) {
        List<TSpanEvent> tSpanEventList = tSpanChunk.getSpanEventList();
        if (CollectionUtils.isEmpty(tSpanEventList)) {
            return null;
        }
        if (fastAsyncIdGen) {
            return fastLocalAsyncIdBo(tSpanEventList);
        } else {
            return fullScanLocalAsyncIdBo(tSpanChunk);
        }
    }

    @VisibleForTesting
    LocalAsyncIdBo fullScanLocalAsyncIdBo(TSpanChunk tSpanChunk) {
        int asyncId = -1;
        int asyncSequence = -1;
        boolean first = true;
        boolean asyncIdNotSame = false;
        for (TSpanEvent tSpanEvent : tSpanChunk.getSpanEventList()) {
            if (first) {
                first = false;
                if (isSetAsyncId(tSpanEvent)) {
                    asyncId = tSpanEvent.getAsyncId();
                    asyncSequence = tSpanEvent.getAsyncSequence();
                }
            } else {
                if (isSetAsyncId(tSpanEvent)) {
                    if (asyncId != tSpanEvent.getAsyncId()) {
                        asyncIdNotSame = true;
                        break;
                    }
                    if (asyncSequence != tSpanEvent.getAsyncSequence()) {
                        asyncIdNotSame = true;
                        break;
                    }
                }
            }
        }
        if (asyncIdNotSame) {
            logger.warn("AsyncId consistency is broken. tSpanChunk:{}", tSpanChunk);
            return null;
        }
        if (asyncId != -1 && asyncSequence != -1) {
            return new LocalAsyncIdBo(asyncId, asyncSequence);
        }
        // non async
        return null;
    }

    @VisibleForTesting
    LocalAsyncIdBo fastLocalAsyncIdBo(List<TSpanEvent> tSpanEventList) {
        final TSpanEvent first = tSpanEventList.get(0);
        if (isSetAsyncId(first)) {
            final int asyncId = first.getAsyncId();
            final short asyncSequence = first.getAsyncSequence();
            return new LocalAsyncIdBo(asyncId, asyncSequence);
        }
        return null;
    }

    private boolean isSetAsyncId(TSpanEvent tSpanEvent) {
        if (!tSpanEvent.isSetAsyncId()) {
            return false;
        }
        if (!tSpanEvent.isSetAsyncSequence()) {
            logger.warn("AsyncId & AsyncSequence consistency is broken. {}", tSpanEvent);
            return false;
        }
        return true;
    }

    // for test
    SpanChunkBo newSpanChunkBo(TSpanChunk tSpanChunk) {
        final SpanChunkBo spanChunkBo = new SpanChunkBo();
        spanChunkBo.setAgentId(tSpanChunk.getAgentId());
        spanChunkBo.setApplicationId(tSpanChunk.getApplicationName());
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
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(transactionIdBytes);
        String transactionAgentId = transactionId.getAgentId();
        if (transactionAgentId != null) {
            return transactionId;
        }
        return new TransactionId(spanAgentId, transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
    }


    private List<SpanEventBo> buildSpanEventBoList(List<TSpanEvent> spanEventList) {
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
        if (tSpanEvent == null) {
            throw new NullPointerException("tSpanEvent");
        }

        final SpanEventBo spanEvent = new SpanEventBo();
        bind(spanEvent, tSpanEvent);
        return spanEvent;
    }

    private AnnotationBo newAnnotationBo(TAnnotation tAnnotation) {
        if (tAnnotation == null) {
            throw new NullPointerException("annotation");
        }
        AnnotationBo annotationBo = annotationFactory.buildAnnotation(tAnnotation);
        return annotationBo;
    }

}
