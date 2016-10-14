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
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.AnnotationBoDecoder;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.SpanDecoder;

import com.navercorp.pinpoint.common.server.bo.serializer.trace.v1.SpanDecodingContext;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.navercorp.pinpoint.web.mapper.SpanMapperV2.*;

/**
 * @author emeroad
 */
@Component
public class SpanMapper implements RowMapper<List<SpanBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AnnotationBoDecoder annotationBoDecoder = new AnnotationBoDecoder();

    private final SpanDecoder spanDecoder = new SpanDecoder();

    private final RowKeyDecoder<TransactionId> rowKeyDecoder;

    @Autowired
    public SpanMapper(@Qualifier("traceRowKeyDecoderV1") RowKeyDecoder<TransactionId> rowKeyDecoder) {
        if (rowKeyDecoder == null) {
            throw new NullPointerException("rowKeyDecoder must not be null");
        }

        this.rowKeyDecoder = rowKeyDecoder;
    }

    @Override
    public List<SpanBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        byte[] rowKey = result.getRow();
        final TransactionId transactionId = this.rowKeyDecoder.decodeRowKey(rowKey);

        final Cell[] rawCells = result.rawCells();

        Map<AgentKey, SpanBo> spanMap = new LinkedHashMap<>();
        ListMultimap<AgentKey, SpanEventBo> spanEventBoListMap = ArrayListMultimap.create();
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

                AgentKey agentKey = newAgentKey(spanBo);
                spanMap.put(agentKey, spanBo);
            } else if (CellUtil.matchingFamily(cell, HBaseTables.TRACES_CF_TERMINALSPAN)) {

                final Buffer qualifier = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                final Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                SpanEventBo spanEventBo = spanDecoder.decodeSpanEventBo(qualifier, valueBuffer, decodingContext);

                AgentKey agentKey = newAgentKey(decodingContext);
                spanEventBoListMap.put(agentKey, spanEventBo);

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

        for (Map.Entry<AgentKey, SpanEventBo> spanBoEntry : spanEventBoListMap.entries()) {
            final AgentKey agentKey = spanBoEntry.getKey();
            SpanBo spanBo = spanMap.get(agentKey);
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

    private AgentKey newAgentKey(SpanBo spanBo) {
        return new AgentKey(spanBo.getApplicationId(), spanBo.getAgentId(), spanBo.getAgentStartTime(), spanBo.getSpanId());
    }

    private AgentKey newAgentKey(SpanDecodingContext decodingContext) {
        return new AgentKey(decodingContext.getApplicationId(), decodingContext.getAgentId(), decodingContext.getAgentStartTime(), decodingContext.getSpanId());
    }

    private void addAnnotation(List<SpanBo> spanList, ListMultimap<Long, AnnotationBo> annotationMap) {
        for (SpanBo spanBo : spanList) {
            long spanId = spanBo.getSpanId();
            List<AnnotationBo> anoList = annotationMap.get(spanId);
            spanBo.setAnnotationBoList(anoList);
        }
    }

}
