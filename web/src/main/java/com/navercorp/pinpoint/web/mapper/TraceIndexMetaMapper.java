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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.RowTypeHint;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.TraceIndexRowUtils;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

// TraceIndexMetaScatterMapper version 2
public class TraceIndexMetaMapper implements RowMapper<List<DotMetaData>>, RowTypeHint {

    private final HbaseColumnFamily index = HbaseTables.TRACE_INDEX;
    private final HbaseColumnFamily meta = HbaseTables.TRACE_INDEX_META;

    // @Nullable
    private final Predicate<Integer> elapsedTimeFilter;
    // @Nullable
    private final Predicate<Integer> exceptionCodeFilter;
    // @Nullable
    private final Predicate<String> agentIdFilter;

    public TraceIndexMetaMapper(Predicate<Integer> elapsedTimeFilter,
                                Predicate<Integer> exceptionCodeFilter,
                                Predicate<String> agentIdFilter) {
        this.elapsedTimeFilter = elapsedTimeFilter;
        this.exceptionCodeFilter = exceptionCodeFilter;
        this.agentIdFilter = agentIdFilter;
    }

    @Override
    public List<DotMetaData> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, DotMetaData.BuilderV2> metaDataMap = new HashMap<>();
        for (Cell cell : result.rawCells()) {
            long spanId = ByteArrayUtils.bytesToLong(cell.getRowArray(), cell.getRowOffset() + cell.getRowLength() - ByteArrayUtils.LONG_BYTE_LENGTH);
            DotMetaData.BuilderV2 builder = getMetaDataBuilder(metaDataMap, spanId);
            if (CellUtil.matchingFamily(cell, index.getName())) {
                builder.setSpanId(spanId);
                builder.setAcceptedTime(TraceIndexRowUtils.extractAcceptTime(cell.getRowArray(), cell.getRowOffset()));
                builder.readIndex(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            }
            if (CellUtil.matchingFamily(cell, meta.getName())) {
                builder.readMeta(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            }
        }
        return filterAndBuild(metaDataMap);
    }

    public List<DotMetaData> filterAndBuild(Map<?, DotMetaData.BuilderV2> metaDataMap) {
        List<DotMetaData> result = new ArrayList<>(metaDataMap.size());
        for (DotMetaData.BuilderV2 builderV2 : metaDataMap.values()) {
            if ((elapsedTimeFilter == null || elapsedTimeFilter.test(builderV2.getElapsedTime()))
                    && (exceptionCodeFilter == null || exceptionCodeFilter.test(builderV2.getExceptionCode()))
                    && (agentIdFilter == null || agentIdFilter.test(builderV2.getAgentId()))) {
                result.add(builderV2.build());
            }
        }
        return result;
    }

    private DotMetaData.BuilderV2 getMetaDataBuilder(Map<Long, DotMetaData.BuilderV2> metaDataMap, Long qualifier) {
        return metaDataMap.computeIfAbsent(qualifier, pair -> new DotMetaData.BuilderV2());
    }

    @Override
    public Class<?> rowType() {
        return List.class;
    }
}
