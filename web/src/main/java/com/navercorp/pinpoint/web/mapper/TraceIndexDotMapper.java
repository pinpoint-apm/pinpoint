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
import com.navercorp.pinpoint.common.profiler.util.TransactionIdV1;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.TraceIndexRowUtils;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TraceIndexScatterMapper version 2
@Component
public class TraceIndexDotMapper implements RowMapper<List<Dot>>, RowTypeHint {
    private final HbaseColumnFamily index = HbaseTables.TRACE_INDEX;

    public TraceIndexDotMapper() {
    }

    @Override
    public List<Dot> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        Cell[] rawCells = result.rawCells();
        List<Dot> list = new ArrayList<>(rawCells.length);
        long acceptedTime = TraceIndexRowUtils.extractAcceptTime(result.getRow(), 0);
        for (Cell cell : rawCells) {
            if (CellUtil.matchingFamily(cell, index.getName())) {
                Dot dot = createDot(acceptedTime, cell);
                list.add(dot);
            }
        }
        return list;
    }

    private Dot createDot(long acceptedTime, Cell cell) {
        final Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        valueBuffer.readByte(); // hasError byte
        String agentId = valueBuffer.readPrefixedString();
        int elapsed = valueBuffer.readVInt();
        int exceptionCode = valueBuffer.readSVInt();

        return new Dot(TransactionIdV1.EMPTY_ID, acceptedTime, elapsed, exceptionCode, agentId);
    }

    @Override
    public Class<?> rowType() {
        return List.class;
    }
}
