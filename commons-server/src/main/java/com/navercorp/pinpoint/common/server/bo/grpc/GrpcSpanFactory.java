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

package com.navercorp.pinpoint.common.server.bo.grpc;



import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AnnotationComparator;
import com.navercorp.pinpoint.common.server.bo.AnnotationFactory;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventComparator;
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.EmptyAcceptedTimeService;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.trace.PAcceptEvent;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PLocalAsyncId;
import com.navercorp.pinpoint.grpc.trace.PMessageEvent;
import com.navercorp.pinpoint.grpc.trace.PNextEvent;
import com.navercorp.pinpoint.grpc.trace.PParentInfo;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
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
public class GrpcSpanFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SpanEventFilter spanEventFilter = new EmptySpanEventFilter();

    private AcceptedTimeService acceptedTimeService = new EmptyAcceptedTimeService();

    private static final AnnotationFactory<PAnnotation> annotationFactory = new AnnotationFactory<>(new GrpcAnnotationHandler());

    private static final int TRACEFORMAT_V2 = 1;
    public GrpcSpanFactory() {
    }

    @Autowired(required = false)
    public void setSpanEventFilter(SpanEventFilter spanEventFilter) {
        this.spanEventFilter = spanEventFilter;
    }

    @Autowired(required = false)
    public void setAcceptedTimeService(AcceptedTimeService acceptedTimeService) {
        this.acceptedTimeService = acceptedTimeService;
    }

    public SpanBo buildSpanBo(PSpan pSpan, AgentHeaderFactory.Header header) {
        checkVersion(pSpan.getVersion());

        final SpanBo spanBo = newSpanBo(pSpan, header);

        List<PSpanEvent> spanEventList = pSpan.getSpanEventList();
        List<SpanEventBo> spanEventBoList = buildSpanEventBoList(spanEventList);
        spanBo.addSpanEventBoList(spanEventBoList);

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        spanBo.setCollectorAcceptTime(acceptedTime);

        return spanBo;
    }

    private void checkVersion(int version) {
        if (version != TRACEFORMAT_V2) {
            throw new IllegalStateException("unsupported version:" + version);
        }
    }

    // for test
    SpanBo newSpanBo(PSpan pSpan, AgentHeaderFactory.Header header) {
        final SpanBo spanBo = new SpanBo();
        spanBo.setAgentId(header.getAgentId());
        spanBo.setApplicationId(header.getApplicationName());
        spanBo.setAgentStartTime(header.getAgentStartTime());

        final TransactionId transactionId = newTransactionId(pSpan.getTransactionId(), spanBo);
        spanBo.setTransactionId(transactionId);

        spanBo.setSpanId(pSpan.getSpanId());
        spanBo.setParentSpanId(pSpan.getParentSpanId());

        spanBo.setStartTime(pSpan.getStartTime());
        spanBo.setElapsed(pSpan.getElapsed());

        spanBo.setServiceType((short) pSpan.getServiceType());

        spanBo.setFlag((short) pSpan.getFlag());
        spanBo.setApiId(pSpan.getApiId());

        spanBo.setErrCode(pSpan.getErr());


        spanBo.setLoggingTransactionInfo((byte) pSpan.getLoggingTransactionInfo());

        spanBo.setApplicationServiceType((short) pSpan.getApplicationServiceType());

        final PAcceptEvent acceptEvent = pSpan.getAcceptEvent();
        if (acceptEvent != PAcceptEvent.getDefaultInstance()) {
            spanBo.setRpc(acceptEvent.getRpc());
            spanBo.setRemoteAddr(acceptEvent.getRemoteAddr());
            spanBo.setEndPoint(acceptEvent.getEndPoint());

            final PParentInfo parentInfo = acceptEvent.getParentInfo();
            if (parentInfo != PParentInfo.getDefaultInstance()) {
                spanBo.setAcceptorHost(parentInfo.getAcceptorHost());
                spanBo.setParentApplicationId(parentInfo.getParentApplicationName());
                spanBo.setParentApplicationServiceType((short) parentInfo.getParentApplicationType());
            }
        }

        // FIXME span.errCode contains error of span and spanEvent
        // because exceptionInfo is the error information of span itself, exceptionInfo can be null even if errCode is not 0
        final PIntStringValue exceptionInfo = pSpan.getExceptionInfo();
        if (exceptionInfo != PIntStringValue.getDefaultInstance()) {
            spanBo.setExceptionInfo(exceptionInfo.getIntValue(), exceptionInfo.getStringValue());
        }

        List<AnnotationBo> annotationBoList = buildAnnotationList(pSpan.getAnnotationList());
        spanBo.setAnnotationBoList(annotationBoList);

        return spanBo;
    }


    private void bind(SpanEventBo spanEvent, PSpanEvent pSpanEvent, SpanEventBo prevSpanEvent) {

        spanEvent.setSequence((short) pSpanEvent.getSequence());

        if (prevSpanEvent == null) {
            int startElapsed = pSpanEvent.getStartElapsed();
            spanEvent.setStartElapsed(startElapsed);
        } else {
            int startElapsed = pSpanEvent.getStartElapsed() + prevSpanEvent.getStartElapsed();
            spanEvent.setStartElapsed(startElapsed);
        }
        spanEvent.setEndElapsed(pSpanEvent.getEndElapsed());

        spanEvent.setServiceType((short) pSpanEvent.getServiceType());

        spanEvent.setApiId(pSpanEvent.getApiId());

        // v2 spec
        final int depth = pSpanEvent.getDepth();
        if (depth != 0) {
            spanEvent.setDepth(depth);
        } else {
            spanEvent.setDepth(prevSpanEvent.getDepth());
        }

        final PNextEvent nextEvent = pSpanEvent.getNextEvent();
        if (nextEvent != PNextEvent.getDefaultInstance()) {
            final PNextEvent.FieldCase fieldCase = nextEvent.getFieldCase();
            if (fieldCase == PNextEvent.FieldCase.ASYNCEVENT) {
                final int asyncEvent = nextEvent.getAsyncEvent();
                spanEvent.setNextAsyncId(asyncEvent);
            } else if (fieldCase == PNextEvent.FieldCase.MESSAGEEVENT) {
                final PMessageEvent messageEvent = nextEvent.getMessageEvent();
                spanEvent.setDestinationId(messageEvent.getDestinationId());
                spanEvent.setEndPoint(messageEvent.getEndPoint());
            }
        }

        List<AnnotationBo> annotationList = buildAnnotationList(pSpanEvent.getAnnotationList());
        spanEvent.setAnnotationBoList(annotationList);

        final PIntStringValue exceptionInfo = pSpanEvent.getExceptionInfo();
        if (exceptionInfo != PIntStringValue.getDefaultInstance()) {
            spanEvent.setExceptionInfo(exceptionInfo.getIntValue(), exceptionInfo.getStringValue());
        }

    }

    public SpanChunkBo buildSpanChunkBo(PSpanChunk pSpanChunk, AgentHeaderFactory.Header header) {
        checkVersion(pSpanChunk.getVersion());

        final SpanChunkBo spanChunkBo = newSpanChunkBo(pSpanChunk, header);
        final PLocalAsyncId pLocalAsyncId = pSpanChunk.getLocalAsyncId();
        if (pLocalAsyncId != PLocalAsyncId.getDefaultInstance()) {
            LocalAsyncIdBo localAsyncIdBo = new LocalAsyncIdBo(pLocalAsyncId.getAsyncId(), pLocalAsyncId.getSequence());
            spanChunkBo.setLocalAsyncId(localAsyncIdBo);
        }

        List<PSpanEvent> spanEventList = pSpanChunk.getSpanEventList();
        List<SpanEventBo> spanEventBoList = buildSpanEventBoList(spanEventList);
        spanChunkBo.addSpanEventBoList(spanEventBoList);


        long acceptedTime = acceptedTimeService.getAcceptedTime();
        spanChunkBo.setCollectorAcceptTime(acceptedTime);

        return spanChunkBo;
    }


    // for test
    SpanChunkBo newSpanChunkBo(PSpanChunk pSpanChunk, AgentHeaderFactory.Header header) {
        final SpanChunkBo spanChunkBo = new SpanChunkBo();
        spanChunkBo.setAgentId(header.getAgentId());
        spanChunkBo.setApplicationId(header.getApplicationName());
        spanChunkBo.setAgentStartTime(header.getAgentStartTime());

        spanChunkBo.setApplicationServiceType((short)pSpanChunk.getApplicationServiceType());

        TransactionId transactionId = newTransactionId(pSpanChunk.getTransactionId(), spanChunkBo);
        spanChunkBo.setTransactionId(transactionId);


        spanChunkBo.setSpanId(pSpanChunk.getSpanId());
        spanChunkBo.setEndPoint(pSpanChunk.getEndPoint());
        return spanChunkBo;
    }

    private TransactionId newTransactionId(ByteString transactionIdByteString, BasicSpan basicSpan) {
        byte[] transactionIdBytes = transactionIdByteString.toByteArray();
        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(transactionIdBytes);
        String transactionAgentId = transactionId.getAgentId();
        if (transactionAgentId != null) {
            return transactionId;
        }
        String spanAgentId = basicSpan.getAgentId();
        return new TransactionId(spanAgentId, transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
    }


    private List<SpanEventBo> buildSpanEventBoList(List<PSpanEvent> spanEventList) {
        if (CollectionUtils.isEmpty(spanEventList)) {
            return new ArrayList<>();
        }
        List<SpanEventBo> spanEventBoList = new ArrayList<>(spanEventList.size());
        SpanEventBo prevSpanEvent = null;
        for (PSpanEvent pSpanEvent : spanEventList) {
            final SpanEventBo spanEventBo = buildSpanEventBo(pSpanEvent, prevSpanEvent);
            if (!spanEventFilter.filter(spanEventBo)) {
                continue;
            }
            spanEventBoList.add(spanEventBo);
            prevSpanEvent = spanEventBo;
        }

        spanEventBoList.sort(SpanEventComparator.INSTANCE);
        return spanEventBoList;
    }

    private List<AnnotationBo> buildAnnotationList(List<PAnnotation> pAnnotationList) {
        if (pAnnotationList == null) {
            return new ArrayList<>();
        }
        List<AnnotationBo> boList = new ArrayList<>(pAnnotationList.size());
        for (PAnnotation tAnnotation : pAnnotationList) {
            final AnnotationBo annotationBo = newAnnotationBo(tAnnotation);
            boList.add(annotationBo);
        }

        boList.sort(AnnotationComparator.INSTANCE);
        return boList;
    }

    // for test
    public SpanEventBo buildSpanEventBo(PSpanEvent pSpanEvent, SpanEventBo prevSpanEvent) {
        if (pSpanEvent == null) {
            throw new NullPointerException("pSpanEvent must not be null");
        }

        final SpanEventBo spanEvent = new SpanEventBo();
        bind(spanEvent, pSpanEvent, prevSpanEvent);
        return spanEvent;
    }

    private AnnotationBo newAnnotationBo(PAnnotation pAnnotation) {
        if (pAnnotation == null) {
            throw new NullPointerException("annotation must not be null");
        }
        AnnotationBo annotationBo = annotationFactory.buildAnnotation(pAnnotation);
        return annotationBo;
    }


}
