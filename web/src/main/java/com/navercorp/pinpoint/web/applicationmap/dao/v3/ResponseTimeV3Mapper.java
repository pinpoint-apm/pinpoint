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
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;


/**
 * @author emeroad
 */
public class ResponseTimeV3Mapper implements RowMapper<ResponseTime> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;
    private final ServiceTypeRegistryService registry;

    private final RowKeyDecoder<UidLinkRowKey> rowKeyDecoder;

    private final TimeWindowFunction timeWindowFunction;

    public ResponseTimeV3Mapper(HbaseColumnFamily table,
                                ServiceTypeRegistryService registry,
                                RowKeyDecoder<UidLinkRowKey> rowKeyDecoder,
                                TimeWindowFunction timeWindowFunction) {
        this.table = Objects.requireNonNull(table, "table");
        this.registry = Objects.requireNonNull(registry, "registry");

        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
    }

    @Override
    public ResponseTime mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        RowKey uidRowKey = rowKeyDecoder.decodeRowKey(result.getRow());

        ResponseTime.Builder responseTimeBuilder = createResponseTime(uidRowKey);
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, table.getName())) {
                recordColumn(responseTimeBuilder, cell);
            }

            if (logger.isTraceEnabled()) {
                String columnFamily = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                logger.trace("unknown column family:{}", columnFamily);
            }
        }
        return responseTimeBuilder.build();
    }


    void recordColumn(ResponseTime.Builder responseTime, Cell cell) {

        final byte[] qArray = cell.getQualifierArray();
        final int qOffset = cell.getQualifierOffset();
        byte slotCode = qArray[qOffset];
        int offset = BytesUtils.BYTE_LENGTH;
        // agentId should be added as data.
        String agentId = Bytes.toString(qArray, qOffset + offset, cell.getQualifierLength() - offset);
        long count = CellUtils.valueToLong(cell);
        responseTime.addResponseTimeByCode(agentId, slotCode, count);
    }

    private ResponseTime.Builder createResponseTime(RowKey rawRowKey) {
        UidLinkRowKey uidRowKey = (UidLinkRowKey) rawRowKey;
        final long timestamp = timeWindowFunction.refineTimestamp(uidRowKey.getTimestamp());
        ServiceType serviceType = registry.findServiceType(uidRowKey.getServiceType());
        return ResponseTime.newBuilder(uidRowKey.getApplicationName(), serviceType, timestamp);
    }

}
