/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.mapper.TimestampFilter;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class ApplicationStatMapper implements RowMapper<List<JoinStatBo>> {

    public final static Comparator<JoinStatBo> REVERSE_TIMESTAMP_COMPARATOR = new Comparator<JoinStatBo>() {
        @Override
        public int compare(JoinStatBo o1, JoinStatBo o2) {
            long x = o2.getTimestamp();
            long y = o1.getTimestamp();
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };

    private final ApplicationStatHbaseOperationFactory hbaseOperationFactory;
    private final ApplicationStatDecoder decoder;
    private final TimestampFilter filter;

    public ApplicationStatMapper(ApplicationStatHbaseOperationFactory hbaseOperationFactory, ApplicationStatDecoder decoder, TimestampFilter filter) {
        this.hbaseOperationFactory = hbaseOperationFactory;
        this.decoder = decoder;
        this.filter = filter;
    }

    @Override
    public List<JoinStatBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] distributedRowKey = result.getRow();
        final String applicationId = this.hbaseOperationFactory.getApplicationId(distributedRowKey);
        final long baseTimestamp = this.hbaseOperationFactory.getBaseTimestamp(distributedRowKey);

        List<JoinStatBo> dataPoints = new ArrayList<>();

        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, HBaseTables.APPLICATION_STAT_CF_STATISTICS)) {
                Buffer qualifierBuffer = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                long timestampDelta = this.decoder.decodeQualifier(qualifierBuffer);

                ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
                decodingContext.setApplicationId(applicationId);
                decodingContext.setBaseTimestamp(baseTimestamp);
                decodingContext.setTimestampDelta(timestampDelta);
                List<JoinStatBo> candidates = this.decoder.decodeValue(valueBuffer, decodingContext);
                for (JoinStatBo candidate : candidates) {
                    long timestamp = candidate.getTimestamp();
                    if (this.filter.filter(timestamp)) {
                        continue;
                    }
                    dataPoints.add(candidate);
                }
            }
        }
        // Reverse sort as timestamp is stored in a reversed order.
        Collections.sort(dataPoints, REVERSE_TIMESTAMP_COMPARATOR);
        return dataPoints;
    }
}

