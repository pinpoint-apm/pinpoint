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

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.RowTypeHint;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author emeroad
 * @author netspider
 */
public class TraceIndexMetaScatterMapper implements RowMapper<List<DotMetaData>>, RowTypeHint {

    private final HbaseColumnFamily index = HbaseTables.APPLICATION_TRACE_INDEX_TRACE;
    private final HbaseColumnFamily meta = HbaseTables.APPLICATION_TRACE_INDEX_META;
    // @Nullable
    private final Predicate<Dot> filter;

    public TraceIndexMetaScatterMapper(Predicate<Dot> filter) {
        this.filter = filter;
    }

    @Override
    public List<DotMetaData> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        Map<TransactionId, DotMetaData.Builder> metaDataMap = new HashMap<>();
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, index.getName())) {
                Dot dot = TraceIndexScatterMapper.createDot(cell);
                DotMetaData.Builder builder = getMetaDataBuilder(metaDataMap, dot.getTransactionId());
                builder.setDot(dot);
            }
            if (CellUtil.matchingFamily(cell, meta.getName())) {
                TransactionId transactionId = SpanUtils.parseVarTransactionId(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                DotMetaData.Builder builder = getMetaDataBuilder(metaDataMap, transactionId);
                builder.read(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            }
        }
        return metaDataMap.values()
                .stream()
                .filter(dotMetaDataBuilder -> this.filter == null || this.filter.test(dotMetaDataBuilder.getDot()))
                .map(DotMetaData.Builder::build)
                .collect(Collectors.toList());
    }

    private DotMetaData.Builder getMetaDataBuilder(Map<TransactionId, DotMetaData.Builder> metaDataMap, TransactionId transactionId) {
        return metaDataMap.computeIfAbsent(transactionId, txId -> new DotMetaData.Builder());
    }

    @Override
    public Class<?> rowType() {
        return List.class;
    }
}
