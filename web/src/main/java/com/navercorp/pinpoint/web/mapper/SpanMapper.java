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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.AnnotationBoDecoder;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.SpanDecoder;

import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.SpanDecodingContext;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
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

    public static final int AGENT_NAME_MAX_LEN = PinpointConstants.AGENT_NAME_MAX_LEN;
    public static final int DISTRIBUTE_HASH_SIZE = 1;

    private final AnnotationBoDecoder annotationBoDecoder = new AnnotationBoDecoder();

    private final SpanDecoder spanDecoder = new SpanDecoder();

    @Override
    public List<SpanBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        byte[] rowKey = result.getRow();
        final TransactionId transactionId = newTransactionId(rowKey, DISTRIBUTE_HASH_SIZE);

        final Cell[] rawCells = result.rawCells();

        Map<Long, SpanBo> spanMap = new LinkedHashMap<>();
        ListMultimap<Long, SpanEventBo> spanEventBoListMap = ArrayListMultimap.create();
        ListMultimap<Long, AnnotationBo> annotationBoListMap = ArrayListMultimap.create();

        final SpanDecodingContext decodingContext = new SpanDecodingContext();
        decodingContext.setTransactionId(transactionId);

        for (Cell cell : rawCells) {
            decodingContext.setCollectorAcceptedTime(cell.getTimestamp());

            // only if family name is "span"
            if (CellUtil.matchingFamily(cell, HBaseTables.TRACES_CF_SPAN)) {

                Buffer qualifierBuffer = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                final SpanBo spanBo = spanDecoder.decodeSpanBo(qualifierBuffer, valueBuffer, decodingContext);
                spanMap.put(spanBo.getSpanId(), spanBo);
            } else if (CellUtil.matchingFamily(cell, HBaseTables.TRACES_CF_TERMINALSPAN)) {

                final Buffer qualifier = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                final Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                SpanEventBo spanEventBo = spanDecoder.decodeSpanEventBo(qualifier, valueBuffer, decodingContext);
                long spanId = decodingContext.getSpanId();
                spanEventBoListMap.put(spanId, spanEventBo);

            } else if (CellUtil.matchingFamily(cell, HBaseTables.TRACES_CF_ANNOTATION)) {

                final Buffer qualifier = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                final Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                List<AnnotationBo> annotationBoList = annotationBoDecoder.decode(qualifier, valueBuffer, decodingContext);
                if (CollectionUtils.isNotEmpty(annotationBoList)) {
                    long spanId = decodingContext.getSpanId();
                    annotationBoListMap.putAll(spanId, annotationBoList);
                }
            }
            spanDecoder.next(decodingContext);
        }
        decodingContext.finish();

        for (Map.Entry<Long, SpanEventBo> spanBoEntry : spanEventBoListMap.entries()) {
            final Long spanId = spanBoEntry.getKey();
            SpanBo spanBo = spanMap.get(spanId);
            if (spanBo != null) {
                SpanEventBo value = spanBoEntry.getValue();
                spanBo.addSpanEvent(value);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Span not exist spanId:{} spanEvent:{}", spanBoEntry.getKey(), spanBoEntry.getValue());
                }
            }
        }

        List<SpanBo> spanList = Lists.newArrayList(spanMap.values());
        if (!annotationBoListMap.isEmpty()) {
            addAnnotation(spanList, annotationBoListMap);
        }


        return spanList;

    }

    private TransactionId newTransactionId(byte[] rowKey, int offset) {

        String agentId = BytesUtils.toStringAndRightTrim(rowKey, offset, AGENT_NAME_MAX_LEN);
        long agentStartTime = BytesUtils.bytesToLong(rowKey, offset + AGENT_NAME_MAX_LEN);
        long transactionSequence = BytesUtils.bytesToLong(rowKey, offset + BytesUtils.LONG_BYTE_LENGTH + AGENT_NAME_MAX_LEN);

        return new TransactionId(agentId, agentStartTime, transactionSequence);
    }




    private void addAnnotation(List<SpanBo> spanList, ListMultimap<Long, AnnotationBo> annotationMap) {
        for (SpanBo bo : spanList) {
            long spanID = bo.getSpanId();
            List<AnnotationBo> anoList = annotationMap.get(spanID);
            bo.setAnnotationBoList(anoList);
        }
    }



}
