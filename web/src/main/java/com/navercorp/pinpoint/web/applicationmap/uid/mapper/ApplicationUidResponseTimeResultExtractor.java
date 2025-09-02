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

package com.navercorp.pinpoint.web.applicationmap.uid.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.uid.ApplicationUidResponse;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;


/**
 * @author emeroad
 */
public class ApplicationUidResponseTimeResultExtractor implements ResultsExtractor<ApplicationUidResponse> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceTypeRegistryService registry;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;

    private final TimeWindowFunction timeWindowFunction;


    public ApplicationUidResponseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                     RowKeyDistributorByHashPrefix rowKeyDistributor,
                                                     TimeWindowFunction timeWindowFunction) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
    }

    public ApplicationUidResponse extractData(ResultScanner results) throws Exception {
        ApplicationUidResponse.Builder builder = null;
        for (Result result : results) {
            builder = this.mapRow(builder, result);
        }
        if (builder == null) {
            return null;
        }
        return builder.build();
    }


    private ApplicationUidResponse.Builder mapRow(ApplicationUidResponse.Builder builder, Result result) {
        if (result.isEmpty()) {
            return null;
        }
        final byte[] rowKey = getOriginalKey(result.getRow());

        final Buffer row = new FixedBuffer(rowKey);
        final long applicationUid = row.readLong();
        final int serviceTypeCode = row.readInt();
        final int serviceUid = row.readInt();
        final long timestamp = timeWindowFunction.refineTimestamp(TimeUtils.recoveryTimeMillis(row.readLong()));

        if (builder == null) {
            ServiceType serviceType = registry.findServiceType(serviceTypeCode);
            builder = ApplicationUidResponse.newBuilder(serviceUid, applicationUid, serviceType);
        } else {
            if (builder.getServiceUid() != serviceUid) {
                throw new IllegalStateException("serviceUid must match");
            }
            if (builder.getApplicationUid() != applicationUid) {
                throw new IllegalStateException("applicationUid must match");
            }
            if (builder.getServiceType().getCode() != serviceTypeCode) {
                throw new IllegalStateException("serviceTypeCode must match");
            }
        }
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, HbaseTables.MAP_SELF_V3_COUNTER.getName())) {
                recordColumn(builder, timestamp, cell);
            }

            if (logger.isTraceEnabled()) {
                String columnFamily = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                logger.trace("unknown column family:{}", columnFamily);
            }
        }
        return builder;
    }

    void recordColumn(ApplicationUidResponse.Builder responseTimeBuilder, long timestamp, Cell cell) {

        final short slotNumber = CellUtils.qualifierToShort(cell);

        // agentId should be added as data.
        long count = CellUtils.valueToLong(cell);

        responseTimeBuilder.addResponseTime(timestamp, slotNumber, count);
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributor.getOriginalKey(rowKey);
    }
}
