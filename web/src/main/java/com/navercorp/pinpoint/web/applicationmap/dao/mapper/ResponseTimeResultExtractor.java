/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author emeroad
 */
public class ResponseTimeResultExtractor implements ResultsExtractor<List<ResponseTime>> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceTypeRegistryService registry;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;

    private final TimeWindowFunction timeWindowFunction;

    public ResponseTimeResultExtractor(ServiceTypeRegistryService registry,
                                       RowKeyDistributorByHashPrefix rowKeyDistributor,
                                       TimeWindowFunction timeWindowFunction) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
    }

    public List<ResponseTime> extractData(ResultScanner results) throws Exception {
        Map<Key, ResponseTime.Builder> rs = new HashMap<>();
        for (Result result : results) {
            this.mapRow(rs, result);
        }
        return build(rs);
    }

    private List<ResponseTime> build(Map<Key, ResponseTime.Builder> responseTimeMap) {
        Collection<ResponseTime.Builder> builders = responseTimeMap.values();
        List<ResponseTime> result = new ArrayList<>(builders.size());
        for (ResponseTime.Builder builder : builders) {
            ResponseTime responseTime = builder.build();
            result.add(responseTime);
        }
        return result;
    }

    private void mapRow(Map<Key, ResponseTime.Builder> map, Result result) {
        if (result.isEmpty()) {
            return;
        }

        final byte[] rowKey = getOriginalKey(result.getRow());

        ResponseTime.Builder responseTimeBuilder = createResponseTimeBuilder(map, rowKey);
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER.getName())) {
                recordColumn(responseTimeBuilder, cell);
            }

            if (logger.isTraceEnabled()) {
                String columnFamily = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                logger.trace("unknown column family:{}", columnFamily);
            }
        }

    }

    void recordColumn(ResponseTime.Builder responseTimeBuilder, Cell cell) {

        final byte[] qArray = cell.getQualifierArray();
        final int qOffset = cell.getQualifierOffset();
        short slotNumber = Bytes.toShort(qArray, qOffset);

        // agentId should be added as data.
        String agentId = Bytes.toString(qArray, qOffset + BytesUtils.SHORT_BYTE_LENGTH, cell.getQualifierLength() - BytesUtils.SHORT_BYTE_LENGTH);
        long count = CellUtils.valueToLong(cell);
        responseTimeBuilder.addResponseTime(agentId, slotNumber, count);
    }

    private ResponseTime.Builder createResponseTimeBuilder(Map<Key, ResponseTime.Builder> map, byte[] rowKey) {
        final Buffer row = new FixedBuffer(rowKey);
        final String applicationName = row.read2PrefixedString();
        final short serviceTypeCode = row.readShort();
        final long timestamp = timeWindowFunction.refineTimestamp(TimeUtils.recoveryTimeMillis(row.readLong()));
        final ServiceType serviceType = registry.findServiceType(serviceTypeCode);

        final Key key = new Key(applicationName, serviceTypeCode, timestamp);

        return map.computeIfAbsent(key, k -> ResponseTime.newBuilder(applicationName, serviceType, timestamp));
    }

    private record Key(
            String applicationName,
            short serviceTypeCode,
            long timestamp
    ) {}

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributor.getOriginalKey(rowKey);
    }
}
