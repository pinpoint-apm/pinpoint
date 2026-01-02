/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.dao.mapper;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.CachedStringAllocator;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventComparator;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoderV0;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecodingContext;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.io.SpanVersion;
import org.apache.commons.lang3.Strings;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author Taejin Koo
 */
public class SpanMapperV2 implements RowMapper<List<SpanBo>> {

    private static final int DISABLED_CACHE = -1;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SpanDecoder spanDecoder;

    private final RowKeyDecoder<ServerTraceId> rowKeyDecoder;

    private final int cacheSize;

    public SpanMapperV2(RowKeyDecoder<ServerTraceId> rowKeyDecoder) {
        this(rowKeyDecoder, new SpanDecoderV0(), DISABLED_CACHE);
    }

    public SpanMapperV2(RowKeyDecoder<ServerTraceId> rowKeyDecoder, int cacheSize) {
        this(rowKeyDecoder, new SpanDecoderV0(), cacheSize);
    }

    public SpanMapperV2(RowKeyDecoder<ServerTraceId> rowKeyDecoder, SpanDecoder spanDecoder) {
        this(rowKeyDecoder, spanDecoder, DISABLED_CACHE);
    }

    public SpanMapperV2(RowKeyDecoder<ServerTraceId> rowKeyDecoder, SpanDecoder spanDecoder, int cacheSize) {
        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");
        this.spanDecoder = Objects.requireNonNull(spanDecoder, "spanDecoder");
        this.cacheSize = cacheSize;
    }

    @Override
    public List<SpanBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }


        byte[] rowKey = result.getRow();
        final ServerTraceId transactionId = this.rowKeyDecoder.decodeRowKey(rowKey);

        final Cell[] rawCells = result.rawCells();

        ListMultimap<AgentKey, SpanBo> spanMap = LinkedListMultimap.create();
        List<SpanChunkBo> spanChunkList = new ArrayList<>();

        final SpanDecodingContext decodingContext = new SpanDecodingContext(transactionId);
        if (cacheSize > 0) {
            decodingContext.setStringAllocator(new CachedStringAllocator(cacheSize));
        }

        for (Cell cell : rawCells) {
            SpanDecoder spanDecoder = null;
            // only if family name is "span"
            if (CellUtil.matchingFamily(cell, HbaseTables.TRACE_V2_SPAN.getName())) {

                decodingContext.setCollectorAcceptedTime(cell.getTimestamp());

                final Buffer qualifier = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                final Buffer columnValue = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                spanDecoder = resolveDecoder(columnValue);
                final BasicSpan decodeObject = spanDecoder.decode(qualifier, columnValue, decodingContext);
                if (decodeObject instanceof SpanBo spanBo) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("spanBo:{}", spanBo);
                    }
                    AgentKey agentKey = newAgentKey(spanBo);
                    spanMap.put(agentKey, spanBo);
                } else if (decodeObject instanceof SpanChunkBo spanChunkBo) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("spanChunkBo:{}", spanChunkBo);
                    }
                    spanChunkList.add(spanChunkBo);
                }

            } else {
                if (logger.isWarnEnabled()) {
                    String columnFamily = Bytes.toStringBinary(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                    logger.warn("Unknown ColumnFamily :{}", columnFamily);
                }
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
        if (SpanVersion.supportedVersionRange(version)) {
            return this.spanDecoder;
        } else {
            throw new IllegalStateException("unsupported version" + version);
        }
    }


    private List<SpanBo> buildSpanBoList(ListMultimap<AgentKey, SpanBo> spanMap, List<SpanChunkBo> spanChunkList) {
        List<SpanBo> spanBoList = bindSpanChunk(spanMap, spanChunkList);
        sortSpanEvent(spanBoList);
        return spanBoList;
    }


    private void sortSpanEvent(List<SpanBo> spanBoList) {
        for (SpanBo spanBo : spanBoList) {
            List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
            spanEventBoList.sort(SpanEventComparator.INSTANCE);
        }
    }

    private List<SpanBo> bindSpanChunk(ListMultimap<AgentKey, SpanBo> spanMap, List<SpanChunkBo> spanChunkList) {
        for (SpanChunkBo spanChunkBo : spanChunkList) {
            AgentKey agentKey = newAgentKey(spanChunkBo);
            List<SpanBo> matchedSpanBoList = spanMap.get(agentKey);
            if (CollectionUtils.hasLength(matchedSpanBoList)) {
                int agentLevelCollisionCount = 0;
                final int spanIdCollisionSize = matchedSpanBoList.size();
                boolean ignoreCollision = false;
                for (SpanBo spanBo : matchedSpanBoList) {
                    if (ServiceTypeCategory.MESSAGE_BROKER.contains(spanBo.getServiceType())) {
                        ignoreCollision = true;
                    }
                    if (isChildSpanChunk(spanBo, spanChunkBo)) {
                        spanBo.addSpanChunkBo(spanChunkBo);
                        agentLevelCollisionCount++;
                    }
                }

                if (Boolean.FALSE == ignoreCollision) {
                    if (agentLevelCollisionCount > 1 || spanIdCollisionSize > 1) {
                        // exceptional case dump
                        logger.warn("Collision agentLevel={}, spanId={}, matchedSpanBoList={}", agentLevelCollisionCount, spanIdCollisionSize, matchedSpanBoList);
                    }
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Span not exist spanId:{} spanChunk:{}", agentKey, spanChunkBo);
                }
            }
        }
        return new ArrayList<>(spanMap.values());
    }

    private boolean isChildSpanChunk(SpanBo spanBo, SpanChunkBo spanChunkBo) {
        if (spanBo.getSpanId() != spanChunkBo.getSpanId()) {
            return false;
        }
        if (!Strings.CS.equals(spanBo.getAgentId(), spanChunkBo.getAgentId())) {
            return false;
        }
        if (!Strings.CS.equals(spanBo.getApplicationName(), spanChunkBo.getApplicationName())) {
            return false;
        }
        return true;
    }

    private AgentKey newAgentKey(BasicSpan basicSpan) {
        return new AgentKey(basicSpan.getApplicationName(), basicSpan.getAgentId(), basicSpan.getSpanId());
    }

    private record AgentKey(String applicationName, String agentId, long spanId) {
    }
}
