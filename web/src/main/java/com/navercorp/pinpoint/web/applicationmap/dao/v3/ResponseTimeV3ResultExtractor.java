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

package com.navercorp.pinpoint.web.applicationmap.dao.v3;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidAgentRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.SlotCode;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.ResponseTime;
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
import java.util.function.Predicate;


/**
 * @author emeroad
 */
public class ResponseTimeV3ResultExtractor implements ResultsExtractor<List<ResponseTime>> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;
    private final ServiceTypeRegistryService registry;

    private final RowKeyDecoder<UidAgentRowKey> rowKeyDecoder;

    private final TimeWindowFunction timeWindowFunction;
    private final Predicate<UidAgentRowKey> rowFilter;

    public ResponseTimeV3ResultExtractor(HbaseColumnFamily table,
                                         ServiceTypeRegistryService registry,
                                         RowKeyDecoder<UidAgentRowKey> rowKeyDecoder,
                                         TimeWindowFunction timeWindowFunction,
                                         Predicate<UidAgentRowKey> rowFilter) {
        this.table = Objects.requireNonNull(table, "table");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");

        this.rowFilter = Objects.requireNonNull(rowFilter, "rowFilter");
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

        ResponseTime.Builder responseTimeBuilder = null;
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, table.getName())) {
                byte[] row = CellUtil.cloneRow(cell);
                UidAgentRowKey uidRowKey = rowKeyDecoder.decodeRowKey(row);
                if (!rowFilter.test(uidRowKey)) {
                    continue;
                }
                if (responseTimeBuilder == null) {
                    responseTimeBuilder = createResponseTimeBuilder(map, uidRowKey);
                }
                recordColumn(responseTimeBuilder, cell, uidRowKey);
            }

            if (logger.isTraceEnabled()) {
                String columnFamily = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                logger.trace("unknown column family:{}", columnFamily);
            }
        }
    }

    void recordColumn(ResponseTime.Builder responseTimeBuilder, Cell cell, UidAgentRowKey rowKey) {

        final byte[] qArray = cell.getQualifierArray();
        final int qOffset = cell.getQualifierOffset();
        SlotCode slotCode = SlotCode.valueOf(qArray[qOffset]);

        // agentId should be added as data.
        long count = CellUtils.valueToLong(cell);

        String agentId = rowKey.getAgentId();
        responseTimeBuilder.addResponseTimeByCode(agentId, slotCode, count);
    }

    private ResponseTime.Builder createResponseTimeBuilder(Map<Key, ResponseTime.Builder> map, UidAgentRowKey rowKey) {
        final long timestamp = timeWindowFunction.refineTimestamp(rowKey.getTimestamp());
        final ServiceType serviceType = registry.findServiceType(rowKey.getServiceType());

        final Key key = new Key(rowKey.getServiceUid(), rowKey.getApplicationName(), rowKey.getServiceType(), timestamp);

        return map.computeIfAbsent(key, k -> ResponseTime.newBuilder(rowKey.getApplicationName(), serviceType, timestamp));
    }

    private record Key(
            int serviceUid,
            String applicationName,
            int serviceTypeCode,
            long timestamp
    ) {}

}
