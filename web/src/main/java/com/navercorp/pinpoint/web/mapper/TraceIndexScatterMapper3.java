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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstatns;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterDataBuilder;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Taejin Koo
 */
public class TraceIndexScatterMapper3 implements RowMapper<ScatterData> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final Predicate<Dot> TRUE_PREDICATE = new Predicate<Dot>() {
        @Override
        public boolean test(Dot dot) {
            return true;
        }
    };

    private final long from;
    private final long to;
    private final int xGroupUnit;
    private final int yGroupUnit;
    private final Predicate<Dot> filter;


    public TraceIndexScatterMapper3(long from, long to, int xGroupUnit, int yGroupUnit) {
        this(from, to, xGroupUnit, yGroupUnit, TRUE_PREDICATE);
    }

    public TraceIndexScatterMapper3(long from, long to, int xGroupUnit, int yGroupUnit, Predicate<Dot> filter) {
        this.from = from;
        this.to = to;
        this.xGroupUnit = xGroupUnit;
        this.yGroupUnit = yGroupUnit;
        this.filter = Objects.requireNonNull(filter, "filter");
    }

    @Override
    public ScatterData mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            ScatterDataBuilder builder = new ScatterDataBuilder(from, to, xGroupUnit, yGroupUnit);
            return builder.build();
        }

        ScatterDataBuilder builder = new ScatterDataBuilder(from, to, xGroupUnit, yGroupUnit);

        Cell[] rawCells = result.rawCells();
        for (Cell cell : rawCells) {
            if (logger.isDebugEnabled()) {
                final byte[] row = CellUtil.cloneRow(cell);
                logger.debug("row:{} {}", Bytes.toStringBinary(row), row.length);
            }

            final Dot dot = TraceIndexScatterMapper.createDot(cell);
            if (filter.test(dot)) {
                builder.addDot(dot);
            }
        }

        return builder.build();
    }
}