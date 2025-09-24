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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.RowTypeHint;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.TraceIndexRowUtils;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 * @author netspider
 */
@Component
public class TraceIndexScatterMapper implements RowMapper<List<Dot>>, RowTypeHint {
    private final HbaseColumnFamily index;

    public TraceIndexScatterMapper() {
        this.index = HbaseTables.TRACE_INDEX;
    }

    // for ApplicationTraceIndexDao
    public TraceIndexScatterMapper(HbaseColumnFamily index) {
        this.index = index;
    }

    @Override
    public List<Dot> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        Cell[] rawCells = result.rawCells();
        List<Dot> list = new ArrayList<>(rawCells.length);
        for (Cell cell : rawCells) {
            if (CellUtil.matchingFamily(cell, index.getName())) {
                Dot dot;
                if (index == HbaseTables.APPLICATION_TRACE_INDEX_TRACE) {
                    dot = createDotV1(cell);
                } else {
                    dot = createDot(cell);
                }
                list.add(dot);
            }
        }
        return list;
    }

    static Dot createDot(Cell cell) {
        final Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        int elapsed = valueBuffer.readVInt();
        int exceptionCode = valueBuffer.readSVInt();
        String agentId = valueBuffer.readPrefixedString();

        long acceptedTime = TraceIndexRowUtils.extractAcceptTime(cell.getRowArray(), cell.getRowOffset());
        TransactionId transactionId = SpanUtils.parseVarTransactionId(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());

        return new Dot(transactionId, acceptedTime, elapsed, exceptionCode, agentId);
    }

    static Dot createDotV1(Cell cell) {
        final Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        int elapsed = valueBuffer.readVInt();
        int exceptionCode = valueBuffer.readSVInt();
        String agentId = valueBuffer.readPrefixedString();

        long acceptedTime = TraceIndexRowUtils.extractAcceptTimeV1(cell.getRowArray(), cell.getRowOffset());
        TransactionId transactionId = SpanUtils.parseVarTransactionId(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());

        return new Dot(transactionId, acceptedTime, elapsed, exceptionCode, agentId);
    }

    @Override
    public Class<?> rowType() {
        return List.class;
    }
}
