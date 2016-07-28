/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.mapper;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AnnotationBoDecoder;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.web.vo.TransactionId;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author emeroad
 */
@Component
public class SpanMapper implements RowMapper<List<SpanBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private AnnotationMapper annotationMapper;

    private final AnnotationBoDecoder annotationBoDecoder = new AnnotationBoDecoder();

    public AnnotationMapper getAnnotationMapper() {
        return annotationMapper;
    }

    public void setAnnotationMapper(AnnotationMapper annotationMapper) {
        this.annotationMapper = annotationMapper;
    }

    @Override
    public List<SpanBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        byte[] rowKey = result.getRow();
        final TransactionId transactionId = new TransactionId(rowKey, TransactionId.DISTRIBUTE_HASH_SIZE);

        final Cell[] rawCells = result.rawCells();
        List<SpanBo> spanList = new ArrayList<>();
        Map<Long, SpanBo> spanMap = new HashMap<>();
        LinkedHashMultimap<Long, SpanEventBo> spanEventBoListMap = LinkedHashMultimap.create();
        for (Cell cell : rawCells) {
            // only if family name is "span"
            if (CellUtil.matchingFamily(cell, HBaseTables.TRACES_CF_SPAN)) {

                SpanBo spanBo = new SpanBo();
                spanBo.setTraceAgentId(transactionId.getAgentId());
                spanBo.setTraceAgentStartTime(transactionId.getAgentStartTime());
                spanBo.setTraceTransactionSequence(transactionId.getTransactionSequence());
                spanBo.setCollectorAcceptTime(cell.getTimestamp());

                spanBo.setSpanId(Bytes.toLong(cell.getQualifierArray(), cell.getQualifierOffset()));
                readSpan(spanBo, cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                if (logger.isDebugEnabled()) {
                    logger.debug("read span :{}", spanBo);
                }
                spanList.add(spanBo);
                spanMap.put(spanBo.getSpanId(), spanBo);
            } else if (CellUtil.matchingFamily(cell, HBaseTables.TRACES_CF_TERMINALSPAN)) {
                SpanEventBo spanEventBo = new SpanEventBo();
                spanEventBo.setTraceAgentId(transactionId.getAgentId());
                spanEventBo.setTraceAgentStartTime(transactionId.getAgentStartTime());
                spanEventBo.setTraceTransactionSequence(transactionId.getTransactionSequence());

                // qualifier : spanId(long) + sequence(short) + asyncId(int)
                final Buffer qualifier = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                Long spanId = qualifier.readLong();

                short sequence = qualifier.readShort();
                int asyncId = -1;
                if (qualifier.hasRemaining()) {
                    asyncId = qualifier.readInt();
                }
                short asyncSequence = -1;
                if (qualifier.hasRemaining()) {
                    asyncSequence = qualifier.readShort();
                }
                spanEventBo.setSequence(sequence);
                spanEventBo.setAsyncId(asyncId);
                spanEventBo.setAsyncSequence(asyncSequence);

                readSpanEvent(spanEventBo, cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                if (logger.isDebugEnabled()) {
                    logger.debug("read spanEvent :{}", spanEventBo);
                }
                spanEventBoListMap.put(spanId, spanEventBo);
            }
        }
        for (Map.Entry<Long, SpanEventBo> spanBoEntry : spanEventBoListMap.entries()) {
            final Long spanId = spanBoEntry.getKey();
            SpanBo spanBo = spanMap.get(spanId);
            if (spanBo != null) {
                spanBo.addSpanEventBoList(Lists.newArrayList(spanBoEntry.getValue()));
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Span not exist spanId:{} spanEvent:{}", spanBoEntry.getKey(), spanBoEntry.getValue());
                }
            }
        }
        if (annotationMapper != null) {
            Map<Long, List<AnnotationBo>> annotationMap = annotationMapper.mapRow(result, rowNum);
            addAnnotation(spanList, annotationMap);
        }


        return spanList;

    }

    private void addAnnotation(List<SpanBo> spanList, Map<Long, List<AnnotationBo>> annotationMap) {
        for (SpanBo bo : spanList) {
            long spanID = bo.getSpanId();
            List<AnnotationBo> anoList = annotationMap.get(spanID);
            bo.setAnnotationBoList(anoList);
        }
    }

    // for test
    public int readSpanEvent(final SpanEventBo spanEvent, byte[] bytes, int offset, int length) {
        final Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);

        spanEvent.setVersion(buffer.readByte());

        spanEvent.setAgentId(buffer.readPrefixedString());
        spanEvent.setApplicationId(buffer.readPrefixedString());
        spanEvent.setAgentStartTime(buffer.readVLong());

        spanEvent.setStartElapsed(buffer.readVInt());
        spanEvent.setEndElapsed(buffer.readVInt());

        // don't need to get sequence because it can be got at Qualifier
        // this.sequence = buffer.readShort();


        spanEvent.setRpc(buffer.readPrefixedString());
        spanEvent.setServiceType(buffer.readShort());
        spanEvent.setEndPoint(buffer.readPrefixedString());
        spanEvent.setDestinationId(buffer.readPrefixedString());
        spanEvent.setApiId(buffer.readSVInt());

        spanEvent.setDepth(buffer.readSVInt());
        spanEvent.setNextSpanId(buffer.readLong());

        final boolean hasException = buffer.readBoolean();
        if (hasException) {
            spanEvent.setExceptionInfo(buffer.readSVInt(), buffer.readPrefixedString());
        }

        final List<AnnotationBo> annotationBoList = annotationBoDecoder.decode(buffer);
        spanEvent.setAnnotationBoList(annotationBoList);
        if (buffer.hasRemaining()) {
            spanEvent.setNextAsyncId(buffer.readSVInt());
        }

        return buffer.getOffset();
    }

    // for test
    public int readSpan(SpanBo span, byte[] bytes, int offset, int length) {
        final Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);

        span.setVersion(buffer.readByte());

        span.setAgentId(buffer.readPrefixedString());
        span.setAgentStartTime(buffer.readVLong());

        // this.spanID = buffer.readLong();
        span.setParentSpanId(buffer.readLong());

        span.setStartTime(buffer.readVLong());
        span.setElapsed(buffer.readVInt());

        span.setRpc(buffer.readPrefixedString());
        span.setApplicationId(buffer.readPrefixedString());
        span.setServiceType(buffer.readShort());
        span.setEndPoint(buffer.readPrefixedString());
        span.setRemoteAddr(buffer.readPrefixedString());
        span.setApiId(buffer.readSVInt());

        span.setErrCode(buffer.readSVInt());

        final boolean hasException = buffer.readBoolean();
        if (hasException) {
            int exceptionId = buffer.readSVInt();
            String exceptionMessage = buffer.readPrefixedString();
            span.setExceptionInfo(exceptionId, exceptionMessage);

        }

        span.setFlag(buffer.readShort());

        // FIXME (2015.03) Legacy - applicationServiceType added in v1.1.0
        // Defaults to span's service type for older versions where applicationServiceType does not exist.
        if (buffer.hasRemaining()) {
            final boolean hasApplicationServiceType = buffer.readBoolean();
            if (hasApplicationServiceType) {
                span.setApplicationServiceType(buffer.readShort());
            }
        }

        if (buffer.hasRemaining()) {
            span.setLoggingTransactionInfo(buffer.readByte());
        }

        if (buffer.hasRemaining()) {
            span.setAcceptorHost(buffer.readPrefixedString());
        }

        return buffer.getOffset();
    }


}
