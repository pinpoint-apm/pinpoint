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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author emeroad
 * @author netspider
 */
@Component
public class TraceIndexMetaScatterMapper implements RowMapper<List<DotMetaData>> {

    private static final HbaseColumnFamily.ApplicationTraceIndexTrace INDEX = HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE;
    private static final HbaseColumnFamily.ApplicationTraceIndexTrace META = HbaseColumnFamily.APPLICATION_TRACE_INDEX_META;

    // @Nullable
    private final Predicate<Dot> filter;

    public TraceIndexMetaScatterMapper() {
        this.filter = null;
    }

    public TraceIndexMetaScatterMapper(Predicate<Dot> filter) {
        this.filter = Objects.requireNonNull(filter, "filter");
    }

    @Override
    public List<DotMetaData> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        Map<TransactionId, DotMetaData.Builder> metaDataMap = new HashMap<>();
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, INDEX.getName())) {
                Dot dot = TraceIndexScatterMapper.createDot(cell);
                DotMetaData.Builder builder = getMetaDataBuilder(metaDataMap, dot.getTransactionId());
                builder.setDot(dot);
            }
            if (CellUtil.matchingFamily(cell, META.getName())) {
                TransactionId transactionId = TransactionIdMapper.parseVarTransactionId(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                DotMetaData.Builder builder = getMetaDataBuilder(metaDataMap, transactionId);
                builder.read(CellUtil.cloneValue(cell));
            }
        }
        return metaDataMap.values()
                .stream()
                .filter(builder -> TraceIndexScatterMapper.filter(builder.getDot(), this.filter))
                .map(DotMetaData.Builder::build)
                .collect(Collectors.toList());
    }

    private DotMetaData.Builder getMetaDataBuilder(Map<TransactionId, DotMetaData.Builder> metaDataMap, TransactionId transactionId) {
        return metaDataMap.computeIfAbsent(transactionId, txId -> new DotMetaData.Builder());
    }

}
