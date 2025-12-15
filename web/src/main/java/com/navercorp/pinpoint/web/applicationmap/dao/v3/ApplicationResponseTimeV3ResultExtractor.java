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
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidAppRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.SlotCode;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Predicate;


/**
 * @author emeroad
 */
public class ApplicationResponseTimeV3ResultExtractor implements ResultsExtractor<ApplicationResponse> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;

    private final ServiceTypeRegistryService registry;

    private final RowKeyDecoder<UidAppRowKey> rowKeyDecoder;

    private final TimeWindowFunction timeWindowFunction;
    private final Predicate<UidAppRowKey> rowFilter;


    public ApplicationResponseTimeV3ResultExtractor(HbaseColumnFamily table,
                                                    ServiceTypeRegistryService registry,
                                                    RowKeyDecoder<UidAppRowKey> rowKeyDecoder,
                                                    TimeWindowFunction timeWindowFunction,
                                                    Predicate<UidAppRowKey> rowFilter) {
        this.table = Objects.requireNonNull(table, "table");
        this.registry = Objects.requireNonNull(registry, "registry");

        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
        this.rowFilter = Objects.requireNonNull(rowFilter, "rowFilter");
    }

    public ApplicationResponse extractData(ResultScanner results) throws Exception {
        ApplicationResponse.Builder builder = null;
        for (Result result : results) {
            builder = this.mapRow(builder, result);
        }
        if (builder == null) {
            return null;
        }
        return builder.build();
    }


    private ApplicationResponse.Builder mapRow(ApplicationResponse.Builder builder, Result result) {
        if (result.isEmpty()) {
            return null;
        }
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, table.getName())) {
                byte[] row = CellUtil.cloneRow(cell);
                UidAppRowKey uidRowKey = rowKeyDecoder.decodeRowKey(row);
                if (!rowFilter.test(uidRowKey)) {
                    continue;
                }
                final long timestamp = timeWindowFunction.refineTimestamp(uidRowKey.getTimestamp());

                if (builder == null) {
                    ServiceType serviceType = registry.findServiceType(uidRowKey.getServiceType());
                    Application application = new Application(uidRowKey.getApplicationName(), serviceType);
                    builder = ApplicationResponse.newBuilder(application);
                }
                recordColumn(builder, timestamp, cell);
            }

            if (logger.isTraceEnabled()) {
                String columnFamily = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                logger.trace("unknown column family:{}", columnFamily);
            }
        }
        return builder;
    }

    void recordColumn(ApplicationResponse.Builder responseTimeBuilder, long timestamp, Cell cell) {

        final byte[] qArray = cell.getQualifierArray();
        final int qOffset = cell.getQualifierOffset();
        final SlotCode slotCode = SlotCode.valueOf(qArray[qOffset]);

        // agentId should be added as data.

        long count = CellUtils.valueToLong(cell);

        responseTimeBuilder.addResponseTimeBySlotCode("-UNSUPPORTED-", timestamp, slotCode, count);
    }

}
