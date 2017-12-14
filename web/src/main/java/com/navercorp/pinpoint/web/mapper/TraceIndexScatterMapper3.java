/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;

/**
 * @author Taejin Koo
 */
public class TraceIndexScatterMapper3 implements RowMapper<ScatterData> {

    private final long from;
    private final long to;
    private final int xGroupUnit;
    private final int yGroupUnit;

    public TraceIndexScatterMapper3(long from, long to, int xGroupUnit, int yGroupUnit) {
        this.from = from;
        this.to = to;
        this.xGroupUnit = xGroupUnit;
        this.yGroupUnit = yGroupUnit;
    }

    @Override
    public ScatterData mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new ScatterData(from, to, xGroupUnit, yGroupUnit);
        }

        ScatterData scatterData = new ScatterData(from, to, xGroupUnit, yGroupUnit);

        Cell[] rawCells = result.rawCells();
        for (Cell cell : rawCells) {
            final Dot dot = createDot(cell);
            if (dot != null) {
                scatterData.addDot(dot);
            }
        }

        return scatterData;
    }

    private Dot createDot(Cell cell) {
        final Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        int elapsed = valueBuffer.readVInt();

        int exceptionCode = valueBuffer.readSVInt();
        String agentId = valueBuffer.readPrefixedString();

        long reverseAcceptedTime = BytesUtils.bytesToLong(cell.getRowArray(), cell.getRowOffset() + HBaseTables.APPLICATION_NAME_MAX_LEN + HBaseTables.APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE);
        long acceptedTime = TimeUtils.recoveryTimeMillis(reverseAcceptedTime);

        // TransactionId transactionId = new TransactionId(buffer,
        // qualifierOffset);

        // for temporary, used TransactionIdMapper
        TransactionId transactionId = TransactionIdMapper.parseVarTransactionId(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());

        return new Dot(transactionId, acceptedTime, elapsed, exceptionCode, agentId);
    }
}