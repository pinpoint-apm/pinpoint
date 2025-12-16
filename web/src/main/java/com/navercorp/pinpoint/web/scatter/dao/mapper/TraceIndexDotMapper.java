/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.scatter.dao.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.RowTypeHint;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdV1;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexRowKeyUtils;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexValue;
import com.navercorp.pinpoint.web.scatter.vo.Dot;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

// TraceIndexScatterMapper version 2
public class TraceIndexDotMapper implements RowMapper<List<Dot>>, RowTypeHint {
    private final HbaseColumnFamily index = HbaseTables.TRACE_INDEX;
    private final Predicate<byte[]> rowPredicate;

    public TraceIndexDotMapper(Predicate<byte[]> rowPredicate) {
        this.rowPredicate = rowPredicate;
    }

    @Override
    public List<Dot> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        if (rowPredicate != null && !rowPredicate.test(result.getRow())) {
            return Collections.emptyList();
        }

        long acceptedTime = TraceIndexRowKeyUtils.extractAcceptTime(result.getRow(), 0);
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingColumn(cell, index.getName(), index.getName())) {
                return List.of(createDot(acceptedTime, cell));
            }
        }
        return Collections.emptyList();
    }

    private Dot createDot(long acceptedTime, Cell cell) {
        TraceIndexValue.Index index = TraceIndexValue.Index.decode(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        return new Dot(TransactionIdV1.EMPTY_ID, acceptedTime, index.elapsed(), index.errorCode(), index.agentId());
    }

    @Override
    public Class<?> rowType() {
        return List.class;
    }
}
