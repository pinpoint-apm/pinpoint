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

package com.navercorp.pinpoint.web.scatter.dao.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.RowTypeHint;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexRowKeyUtils;
import com.navercorp.pinpoint.web.scatter.vo.DotMetaData;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

// TraceIndexMetaScatterMapper version 2
public class TraceIndexMetaMapper implements RowMapper<List<DotMetaData>>, RowTypeHint {

    private final HbaseColumnFamily index = HbaseTables.TRACE_INDEX;
    private final HbaseColumnFamily meta = HbaseTables.TRACE_INDEX_META;

    private final Predicate<byte[]> rowPredicate;
    private final Predicate<Integer> exceptionCodeFilter;
    private final Predicate<Integer> elapsedTimeFilter;
    private final Predicate<String> agentIdFilter;

    public TraceIndexMetaMapper(Predicate<byte[]> rowPredicate,
                                Predicate<Integer> exceptionCodeFilter,
                                Predicate<Integer> elapsedTimeFilter,
                                Predicate<String> agentIdFilter) {
        this.rowPredicate = rowPredicate;
        this.exceptionCodeFilter = exceptionCodeFilter;
        this.elapsedTimeFilter = elapsedTimeFilter;
        this.agentIdFilter = agentIdFilter;
    }

    @Override
    public List<DotMetaData> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        if (rowPredicate != null && !rowPredicate.test(result.getRow())) {
            return Collections.emptyList();
        }

        DotMetaData.BuilderV2 builder = new DotMetaData.BuilderV2();
        byte[] row = result.getRow();
        builder.setAcceptedTime(TraceIndexRowKeyUtils.extractAcceptTime(row, 0));
        builder.setSpanId(TraceIndexRowKeyUtils.extractSpanId(row, 0));
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingColumn(cell, index.getName(), index.getName())) {
                builder.readIndex(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            } else if (CellUtil.matchingColumn(cell, meta.getName(), meta.getName())) {
                builder.readMeta(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            } else if (CellUtil.matchingColumn(cell, meta.getName(), HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC)) {
                builder.readMetaRpc(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            }
        }
        return filterAndBuild(builder);
    }

    public List<DotMetaData> filterAndBuild(DotMetaData.BuilderV2 builder) {
        if ((elapsedTimeFilter == null || elapsedTimeFilter.test(builder.getElapsedTime()))
                && (exceptionCodeFilter == null || exceptionCodeFilter.test(builder.getExceptionCode()))
                && (agentIdFilter == null || agentIdFilter.test(builder.getAgentId()))) {
            return List.of(builder.build());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Class<?> rowType() {
        return List.class;
    }
}
