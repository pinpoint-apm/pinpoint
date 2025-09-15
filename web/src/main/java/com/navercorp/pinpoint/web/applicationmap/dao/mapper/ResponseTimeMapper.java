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

package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
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
public class ResponseTimeMapper implements RowMapper<ResponseTime> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;
    private final ServiceTypeRegistryService registry;

    private final int saltKeySize;

    private final TimeWindowFunction timeWindowFunction;

    public ResponseTimeMapper(HbaseColumnFamily table,
                              ServiceTypeRegistryService registry,
                              RowKeyDistributorByHashPrefix rowKeyDistributor,
                              TimeWindowFunction timeWindowFunction) {
        this.table = Objects.requireNonNull(table, "table");
        this.registry = Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.saltKeySize = rowKeyDistributor.getSaltKeySize();
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
    }

    @Override
    public ResponseTime mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        RowKey linkRowKey = readRowKey(saltKeySize, result.getRow());

        ResponseTime.Builder responseTimeBuilder = createResponseTime(linkRowKey);
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

    private RowKey readRowKey(int saltKeySize, byte[] rowKey) {
        return LinkRowKey.read(saltKeySize, rowKey);
    }

    void recordColumn(ResponseTime.Builder responseTime, Cell cell) {

        final byte[] qArray = cell.getQualifierArray();
        final int qOffset = cell.getQualifierOffset();
        short slotNumber = Bytes.toShort(qArray, qOffset);

        // agentId should be added as data.
        String agentId = Bytes.toString(qArray, qOffset + BytesUtils.SHORT_BYTE_LENGTH, cell.getQualifierLength() - BytesUtils.SHORT_BYTE_LENGTH);
        long count = CellUtils.valueToLong(cell);
        responseTime.addResponseTime(agentId, slotNumber, count);
    }

    private ResponseTime.Builder createResponseTime(RowKey rawRowKey) {
        LinkRowKey rowKey = (LinkRowKey) rawRowKey;
        String applicationName = rowKey.getApplicationName();
        short serviceTypeCode = rowKey.getServiceType();
        final long timestamp = timeWindowFunction.refineTimestamp(rowKey.getTimestamp());
        ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        return ResponseTime.newBuilder(applicationName, serviceType, timestamp);
    }

}
