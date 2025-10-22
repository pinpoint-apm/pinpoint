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
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
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


/**
 * @author emeroad
 */
public class ApplicationResponseTimeV3ResultExtractor implements ResultsExtractor<ApplicationResponse> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;

    private final ServiceTypeRegistryService registry;

    private final RowKeyDecoder<UidLinkRowKey> rowKeyDecoder;

    private final TimeWindowFunction timeWindowFunction;


    public ApplicationResponseTimeV3ResultExtractor(HbaseColumnFamily table,
                                                    ServiceTypeRegistryService registry,
                                                    RowKeyDecoder<UidLinkRowKey> rowKeyDecoder,
                                                    TimeWindowFunction timeWindowFunction) {
        this.table = Objects.requireNonNull(table, "table");
        this.registry = Objects.requireNonNull(registry, "registry");

        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");
        this.timeWindowFunction = Objects.requireNonNull(timeWindowFunction, "timeWindowFunction");
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
        final byte[] rowKey = result.getRow();
        UidLinkRowKey uidRowKey = rowKeyDecoder.decodeRowKey(rowKey);

        final String applicationName = uidRowKey.getApplicationName();
        final int serviceTypeCode = uidRowKey.getServiceType();
        final long timestamp = timeWindowFunction.refineTimestamp(uidRowKey.getTimestamp());

        if (builder == null) {
            ServiceType serviceType = registry.findServiceType(serviceTypeCode);
            Application application = new Application(applicationName, serviceType);
            builder = ApplicationResponse.newBuilder(application);
        } else {
            Application builderApp = builder.getApplication();
            if (!builderApp.getName().equals(applicationName)) {
                throw new IllegalStateException("applicationName must match");
            }
            if (builderApp.getServiceType().getCode() != serviceTypeCode) {
                throw new IllegalStateException("serviceType must match");
            }
        }
        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingFamily(cell, table.getName())) {
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
        final byte slotCode = qArray[qOffset];

        // agentId should be added as data.

        long count = CellUtils.valueToLong(cell);

        responseTimeBuilder.addResponseTimeBySlotCode("-UNSUPPORTED-", timestamp, slotCode, count);
    }

}
