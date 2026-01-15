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
    private final Predicate<String> agentIdFilter;
    private final Predicate<Integer> elapsedTimeFilter;

    public TraceIndexMetaMapper(Predicate<byte[]> rowPredicate,
                                Predicate<Integer> exceptionCodeFilter,
                                Predicate<String> agentIdFilter,
                                Predicate<Integer> elapsedTimeFilter) {
        this.rowPredicate = rowPredicate;
        this.exceptionCodeFilter = exceptionCodeFilter;
        this.agentIdFilter = agentIdFilter;
        this.elapsedTimeFilter = elapsedTimeFilter;
    }

    @Override
    public List<DotMetaData> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        if (!test(rowPredicate, result.getRow())) {
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

    private List<DotMetaData> filterAndBuild(DotMetaData.BuilderV2 builder) {
        if (test(exceptionCodeFilter, builder.getExceptionCode()) &&
                test(agentIdFilter, builder.getAgentId()) &&
                test(elapsedTimeFilter, builder.getElapsedTime())) {
            return List.of(builder.build());
        } else {
            return Collections.emptyList();
        }
    }

    private <T> boolean test(Predicate<T> predicate, T value) {
        return predicate == null || predicate.test(value);
    }

    @Override
    public Class<?> rowType() {
        return List.class;
    }
}
