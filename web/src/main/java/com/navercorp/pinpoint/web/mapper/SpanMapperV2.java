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

import com.google.common.annotations.Beta;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventComparator;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoderV0;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecodingContext;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
@Beta
@Component
public class SpanMapperV2 implements RowMapper<List<SpanBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int AGENT_NAME_MAX_LEN = PinpointConstants.AGENT_NAME_MAX_LEN;
    public static final int DISTRIBUTE_HASH_SIZE = 1;

    private final SpanDecoder spanDecoder = new SpanDecoderV0();


    @Override
    public List<SpanBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }


        byte[] rowKey = result.getRow();
        final TransactionId transactionId = newTransactionId(rowKey, DISTRIBUTE_HASH_SIZE);

        final Cell[] rawCells = result.rawCells();

        ListMultimap<Long, SpanBo> spanMap = LinkedListMultimap.create();
        List<SpanChunkBo> spanChunkList = new ArrayList<>();

        final SpanDecodingContext decodingContext = new SpanDecodingContext();
        decodingContext.setTransactionId(transactionId);

        for (Cell cell : rawCells) {
            SpanDecoder spanDecoder = null;
            // only if family name is "span"
            if (CellUtil.matchingFamily(cell, HBaseTables.TRACE_V2_CF_SPAN)) {

                decodingContext.setCollectorAcceptedTime(cell.getTimestamp());

                final Buffer qualifier = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                final Buffer columnValue = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                spanDecoder = resolveDecoder(columnValue);
                final Object decodeObject = spanDecoder.decode(qualifier, columnValue, decodingContext);
                if (decodeObject instanceof SpanBo) {
                    SpanBo spanBo = (SpanBo) decodeObject;
                    if (logger.isDebugEnabled()) {
                        logger.debug("spanBo:{}", spanBo);
                    }
                    spanMap.put(spanBo.getSpanId(), spanBo);
                } else if (decodeObject instanceof SpanChunkBo) {
                    SpanChunkBo spanChunkBo = (SpanChunkBo) decodeObject;
                    if (logger.isDebugEnabled()) {
                        logger.debug("spanChunkBo:{}", spanChunkBo);
                    }
                    spanChunkList.add(spanChunkBo);
                }

            } else {
                logger.warn("Unknown ColumnFamily :{}", Bytes.toStringBinary(CellUtil.cloneFamily(cell)));
            }
            nextCell(spanDecoder, decodingContext);
        }
        decodingContext.finish();


        return buildSpanBoList(spanMap, spanChunkList);

    }

    private void nextCell(SpanDecoder spanDecoder, SpanDecodingContext decodingContext) {
        if (spanDecoder != null) {
            spanDecoder.next(decodingContext);
        } else {
            decodingContext.next();
        }
    }

    private SpanDecoder resolveDecoder(Buffer columnValue) {
        final byte version = columnValue.getByte(0);
        if (version == 0) {
            return this.spanDecoder;
        } else {
            throw new IllegalStateException("unsupported version");
        }
    }

    private TransactionId newTransactionId(byte[] rowKey, int offset) {

        String agentId = BytesUtils.toStringAndRightTrim(rowKey, offset, AGENT_NAME_MAX_LEN);
        long agentStartTime = BytesUtils.bytesToLong(rowKey, offset + AGENT_NAME_MAX_LEN);
        long transactionSequence = BytesUtils.bytesToLong(rowKey, offset + BytesUtils.LONG_BYTE_LENGTH + AGENT_NAME_MAX_LEN);

        return new TransactionId(agentId, agentStartTime, transactionSequence);
    }

    private List<SpanBo> buildSpanBoList(ListMultimap<Long, SpanBo> spanMap, List<SpanChunkBo> spanChunkList) {
        List<SpanBo> spanBoList = bindSpanChunk(spanMap, spanChunkList);
        bindAgentInfo(spanBoList);
        return spanBoList;
    }


    private void bindAgentInfo(List<SpanBo> spanBoList) {
        // TODO workaround. fix class dependency
        for (SpanBo spanBo : spanBoList) {
            List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
            Collections.sort(spanEventBoList, SpanEventComparator.INSTANCE);

            for (SpanEventBo spanEventBo : spanEventBoList) {
                spanEventBo.setAgentId(spanBo.getAgentId());
                spanEventBo.setApplicationId(spanBo.getApplicationId());
                spanEventBo.setAgentStartTime(spanBo.getAgentStartTime());

                spanEventBo.setTraceAgentId(spanBo.getTraceAgentId());
                spanEventBo.setTraceAgentStartTime(spanBo.getTraceAgentStartTime());
                spanEventBo.setTraceTransactionSequence(spanBo.getTraceTransactionSequence());
            }
        }
    }

    private List<SpanBo> bindSpanChunk(ListMultimap<Long, SpanBo> spanMap, List<SpanChunkBo> spanChunkList) {
        for (SpanChunkBo spanChunkBo : spanChunkList) {
            final Long spanId = spanChunkBo.getSpanId();
            List<SpanBo> matchedSpanBoList = spanMap.get(spanId);
            if (matchedSpanBoList != null) {
                final int spanIdCollisionSize = matchedSpanBoList.size();
                if (spanIdCollisionSize > 1) {
                    // exceptional case dump
                    logger.warn("spanIdCollision {}", matchedSpanBoList);
                }

                int agentLevelCollisionCount = 0;
                for (SpanBo spanBo : matchedSpanBoList) {
                    if (StringUtils.equals(spanBo.getAgentId(), spanChunkBo.getAgentId())) {
                        spanBo.addSpanEventBoList(spanChunkBo.getSpanEventBoList());
                        agentLevelCollisionCount++;
                    }
                }
                if (agentLevelCollisionCount > 1) {
                    // exceptional case dump
                    logger.warn("agentLevelCollision {}", matchedSpanBoList);
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Span not exist spanId:{} spanChunk:{}", spanId, spanChunkBo);
                }
            }
        }
        return Lists.newArrayList(spanMap.values());
    }
}
